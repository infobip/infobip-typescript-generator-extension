package com.infobip.typescript.type;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infobip.jackson.CompositeJsonTypeResolver;
import com.infobip.jackson.JsonTypeResolverFactory;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.typescript.TypeScriptImportResolver;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;
import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.emitter.TsPropertyModel;

public class JsonTypeExtension extends Extension implements TypeScriptImportResolver {

    private final JsonTypeResolverFactory factory = new JsonTypeResolverFactory();
    private final Supplier<Stream<DynamicHierarchyDeserializer<?>>> dynamicHierarchyDeserializerProvider;

    public JsonTypeExtension() {
        this(Stream::empty);
    }

    public JsonTypeExtension(Supplier<Stream<DynamicHierarchyDeserializer<?>>> dynamicHierarchyDeserializerProvider) {
        this.dynamicHierarchyDeserializerProvider = dynamicHierarchyDeserializerProvider;
    }

    @Override
    public EmitterExtensionFeatures getFeatures() {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = true;
        return features;
    }

    @Override
    public List<TransformerDefinition> getTransformers() {
        return Collections.singletonList(new TransformerDefinition(ModelCompiler.TransformationPhase.BeforeEnums,
                                                                   (TsModelTransformer) this::addTypeInformationToHierarchy));
    }

    @Override
    public List<String> resolve(String typeScript) {
        if (typeScript.contains("@Type(")) {
            return Arrays.asList("import 'reflect-metadata';",
                                 "import { Type } from 'class-transformer';");
        }

        return Collections.emptyList();
    }

    private TsModel addTypeInformationToHierarchy(TsModelTransformer.Context context, TsModel model) {
        List<TsBeanModel> beans = model.getBeans();

        for (TsBeanModel bean : beans) {

            Class<?> type = bean.getOrigin();
            if (Modifier.isAbstract(type.getModifiers()) || type.isInterface()) {
                beans = addTypeInformationToHierarchy(context, beans, type);
            }
        }

        return model.withBeans(beans);
    }

    private List<TsBeanModel> addTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                            List<TsBeanModel> beans,
                                                            Class<?> type) {

        return factory.create(type)
                      .filter(resolver -> resolver instanceof CompositeJsonTypeResolver<?>)
                      .map(resolver -> (CompositeJsonTypeResolver<?>) resolver)
                      .map(resolver -> addTypeInformationToHierarchy(context, beans, resolver))
                      .or(() -> addTypeInformationToDynamicHierarchy(context, beans, type))
                      .orElse(beans);
    }

    private List<TsBeanModel> addTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                            List<TsBeanModel> beans,
                                                            CompositeJsonTypeResolver<?> resolver) {
        List<NamedType> subtypes = findSubtypes(resolver);
        Map<Class<?>, NamedType> typeToNamedType = subtypes.stream()
                                                           .collect(Collectors.toMap(NamedType::getType,
                                                                                     Function.identity(),
                                                                                     (first, second) -> second));
        return beans.stream()
                    .map(bean -> addTypeInformationToHierarchy(context, bean, resolver,
                                                               typeToNamedType.get(bean.getOrigin())))
                    .collect(Collectors.toList());
    }

    private Optional<List<TsBeanModel>> addTypeInformationToDynamicHierarchy(TsModelTransformer.Context context,
                                                                             List<TsBeanModel> beans,
                                                                             Class<?> type) {

        return dynamicHierarchyDeserializerProvider.get()
                                                   .filter(deserializer -> deserializer.getHierarchyRootType().equals(type))
                                                   .findFirst()
                                                   .map(deserializer -> addTypeInformationToDynamicHierarchy(context, beans, deserializer));
    }

    private List<TsBeanModel> addTypeInformationToDynamicHierarchy(TsModelTransformer.Context context,
                                                                   List<TsBeanModel> beans,
                                                                   DynamicHierarchyDeserializer<?> deserializer) {
        List<NamedType> subtypes = findSubtypes(deserializer);
        Map<Class<?>, NamedType> typeToNamedType = subtypes.stream()
                                                           .collect(Collectors.toMap(NamedType::getType,
                                                                                     Function.identity(),
                                                                                     (first, second) -> second));
        return beans.stream()
                    .map(bean -> addTypeInformationToHierarchy(context,
                                                               bean,
                                                               deserializer,
                                                               typeToNamedType.get(bean.getOrigin())))
                    .collect(Collectors.toList());
    }

    private TsBeanModel addTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                      TsBeanModel tsBeanModel,
                                                      CompositeJsonTypeResolver<?> resolver,
                                                      NamedType namedType) {
        if (Objects.isNull(namedType)) {
            return tsBeanModel;
        }

        return tsBeanModel.withProperties(tsBeanModel.getProperties()
                                                     .stream()
                                                     .map(tsPropertyModel -> addEnumTypeInformationToHierarchy(context,
                                                                                                               tsPropertyModel,
                                                                                                               resolver.getType(),
                                                                                                               namedType))
                                                     .collect(Collectors.toList())
                                         );
    }

    private <E extends Enum<E>> TsPropertyModel addEnumTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                                                  TsPropertyModel tsPropertyModel,
                                                                                  Class<E> type,
                                                                                  NamedType namedType) {
        TsType expected = new TsType.EnumReferenceType(context.getSymbolTable().getSymbol(type));

        if (!tsPropertyModel.getTsType().equals(expected)) {
            return tsPropertyModel;
        }

        E enumType = Arrays.stream(type.getEnumConstants())
                           .filter(e -> e.name().equals(namedType.getName()))
                           .findFirst()
                           .orElse(null);
        return new TsPropertyModel(tsPropertyModel.name, new EnumInitializerType<>(type.getSimpleName(), Objects.toString(enumType)),
                                   tsPropertyModel.decorators,
                                   tsPropertyModel.modifiers.setReadonly(),
                                   true,
                                   null,
                                   tsPropertyModel.comments);
    }

    private TsBeanModel addTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                      TsBeanModel tsBeanModel,
                                                      DynamicHierarchyDeserializer<?> deserializer,
                                                      NamedType namedType) {
        if (Objects.isNull(namedType)) {
            return tsBeanModel;
        }

        return tsBeanModel.withProperties(tsBeanModel.getProperties()
                                                     .stream()
                                                     .map(tsPropertyModel -> addTypeInformationToHierarchy(context,
                                                                                                           tsPropertyModel,
                                                                                                           namedType,
                                                                                                           deserializer.getJsonValuePropertyName()))
                                                     .collect(Collectors.toList())
                                         );
    }

    private TsPropertyModel addTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                          TsPropertyModel tsPropertyModel,
                                                          NamedType namedType,
                                                          String jsonValuePropertyName) {
        if (!tsPropertyModel.getName().equals(jsonValuePropertyName)) {
            return tsPropertyModel;
        }

        if (tsPropertyModel.getTsType().toString().equals("string")) {
            return new TsPropertyModel(tsPropertyModel.name, new StringInitializerType(namedType.getName()),
                                       tsPropertyModel.decorators,
                                       tsPropertyModel.modifiers.setReadonly(),
                                       true,
                                       null,
                                       tsPropertyModel.comments);
        }

        String type = getType(tsPropertyModel);

        return new TsPropertyModel(tsPropertyModel.name, new EnumInitializerType<>(type, namedType.getName()),
                                   tsPropertyModel.decorators,
                                   tsPropertyModel.modifiers.setReadonly(),
                                   true,
                                   null,
                                   tsPropertyModel.comments);

    }

    private String getType(TsPropertyModel tsPropertyModel) {
        String type = tsPropertyModel.getTsType().toString();
        String typeWithoutLastDollarSign = type.substring(0, type.length() - 1);
        return typeWithoutLastDollarSign.substring(typeWithoutLastDollarSign.lastIndexOf('$') + 1);
    }

    private <E extends Enum<E>> List<NamedType> findSubtypes(CompositeJsonTypeResolver<E> resolver) {
        return Stream.of(resolver.getType().getEnumConstants())
                     .filter(constant -> Objects.nonNull(resolver.getConverter().apply(constant)))
                     .map(constant -> new NamedType(resolver.getConverter().apply(constant), constant.name()))
                     .collect(Collectors.toList());
    }

    private List<NamedType> findSubtypes(DynamicHierarchyDeserializer<?> deserializer) {
        return deserializer.getJsonValueToJavaType()
                           .entrySet()
                           .stream()
                           .map(entry -> new NamedType(entry.getValue(), entry.getKey()))
                           .collect(Collectors.toList());
    }

}
