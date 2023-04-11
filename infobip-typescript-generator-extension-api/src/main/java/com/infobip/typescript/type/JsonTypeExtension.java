package com.infobip.typescript.type;

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
import cz.habarta.typescript.generator.Settings;
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
                                                                   (TsModelTransformer) this::addTypeInformationToType));
    }

    @Override
    public List<String> resolve(String typeScript) {
        if (typeScript.contains("@Type(")) {
            return Arrays.asList("import 'reflect-metadata';",
                                 "import { Type } from 'class-transformer';");
        }

        return Collections.emptyList();
    }

    private TsModel addTypeInformationToType(TsModelTransformer.Context context, TsModel model) {
        List<TsBeanModel> beans = new ArrayList<>();

        for (TsBeanModel bean : model.getBeans()) {

            Class<?> type = bean.getOrigin();
            var interfaces = getInterfaces(type);

            if (!interfaces.isEmpty()) {
                beans.add(addTypeInformationToType(context, bean, type));
            } else {
                beans.add(bean);
            }

        }

        return model.withBeans(beans);
    }

    private TsBeanModel addTypeInformationToType(TsModelTransformer.Context context,
                                                 TsBeanModel bean,
                                                 Class<?> type) {

        return factory.create(type)
                      .filter(resolver -> resolver instanceof CompositeJsonTypeResolver<?>)
                      .map(resolver -> (CompositeJsonTypeResolver<?>) resolver)
                      .flatMap(resolver -> addTypeInformationToType(context, bean, resolver, type))
                      .or(() -> addTypeInformationToTypeInDynamicHierarchy(context, bean, type))
                      .orElse(bean);
    }

    private Optional<TsBeanModel> addTypeInformationToType(TsModelTransformer.Context context,
                                                           TsBeanModel bean,
                                                           CompositeJsonTypeResolver<?> resolver,
                                                           Class<?> type) {

        var jsonValueToJavaType = getJsonValueToJavaType(resolver);

        var isIncludedInHierarchy = jsonValueToJavaType.containsValue(type);

        if (!isIncludedInHierarchy) {
            return Optional.empty();
        }

        var value = getValue(type, jsonValueToJavaType);

        if (Objects.isNull(value)) {
            return Optional.empty();
        }

        return Optional.of(addTypeInformationToType(context, bean, resolver, new NamedType(type, value.toString())));
    }

    private <E extends Enum<E>> Map<E, Class<?>> getJsonValueToJavaType(CompositeJsonTypeResolver<E> resolver) {
        return Stream.of(resolver.getType().getEnumConstants())
                     .collect(Collectors.toMap(Function.identity(), resolver.getConverter()));
    }

    private Optional<TsBeanModel> addTypeInformationToTypeInDynamicHierarchy(TsModelTransformer.Context context,
                                                                             TsBeanModel bean,
                                                                             Class<?> type) {

        return dynamicHierarchyDeserializerProvider.get()
                                                   .map(deserializer -> addTypeInformationToTypeInDynamicHierarchy(context,
                                                                                                                   bean,
                                                                                                                   deserializer))
                                                   .map(value -> value.orElse(null))
                                                   .filter(Objects::nonNull)
                                                   .findAny();
    }

    private Optional<TsBeanModel> addTypeInformationToTypeInDynamicHierarchy(TsModelTransformer.Context context,
                                                                             TsBeanModel bean,
                                                                             DynamicHierarchyDeserializer<?> deserializer) {
        Class<?> type = bean.getOrigin();
        Map<String, Class<?>> jsonValueToJavaType = (Map<String, Class<?>>) deserializer.getJsonValueToJavaType();
        var isIncludedInHierarchy = jsonValueToJavaType.containsValue(type);

        if (!isIncludedInHierarchy) {
            return Optional.empty();
        }

        var value = jsonValueToJavaType
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(type))
            .map(Map.Entry::getKey)
            .findAny()
            .orElse(null);

        if (Objects.isNull(value)) {
            return Optional.empty();
        }

        return Optional.of(addTypeInformationToType(context,
                                                    bean,
                                                    deserializer,
                                                    new NamedType(type, value)));
    }

    private TsBeanModel addTypeInformationToType(TsModelTransformer.Context context,
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
                                                                                  Class<E> enumType,
                                                                                  NamedType namedType) {
        if (!doesFieldTypeMatch(context, tsPropertyModel, enumType)) {
            return tsPropertyModel;
        }

        E enumValue = Arrays.stream(enumType.getEnumConstants())
                            .filter(e -> e.name().equals(namedType.getName()))
                            .findFirst()
                            .orElse(null);
        return new TsPropertyModel(tsPropertyModel.name,
                                   new EnumInitializerType<>(getEnumInitializerType(tsPropertyModel, enumType),
                                                             Objects.toString(enumValue)),
                                   tsPropertyModel.decorators,
                                   tsPropertyModel.modifiers.setReadonly(),
                                   true,
                                   null,
                                   tsPropertyModel.comments);
    }

    private <E extends Enum<E>> String getEnumInitializerType(TsPropertyModel tsPropertyModel, Class<E> enumType) {
        var tsTypeName = tsPropertyModel.getTsType().toString();

        if (tsTypeName.contains(".")) {
            return tsTypeName;
        }

        return enumType.getSimpleName();
    }

    private <E extends Enum<E>> boolean doesFieldTypeMatch(TsModelTransformer.Context context,
                                                           TsPropertyModel tsPropertyModel,
                                                           Class<E> enumType) {
        TsType expected = new TsType.EnumReferenceType(context.getSymbolTable().getSymbol(enumType));

        if (tsPropertyModel.getTsType().equals(expected)) {
            return true;
        }

        var tsTypeName = removeModule(tsPropertyModel);
        var enumTypeName = enumType.getSimpleName();
        return tsTypeName.equals(enumTypeName);
    }

    private String removeModule(TsPropertyModel tsPropertyModel) {
        var name = tsPropertyModel.getTsType().toString();

        if (name.contains(".")) {
            return name.substring(name.lastIndexOf(".") + 1);
        }

        return name;
    }

    private TsBeanModel addTypeInformationToType(TsModelTransformer.Context context,
                                                 TsBeanModel tsBeanModel,
                                                 DynamicHierarchyDeserializer<?> deserializer,
                                                 NamedType namedType) {
        return tsBeanModel.withProperties(tsBeanModel.getProperties()
                                                     .stream()
                                                     .map(tsPropertyModel -> addTypeInformationToType(context,
                                                                                                      tsPropertyModel,
                                                                                                      namedType,
                                                                                                      deserializer.getJsonValuePropertyName()))
                                                     .collect(Collectors.toList())
                                         );
    }

    private TsPropertyModel addTypeInformationToType(TsModelTransformer.Context context,
                                                     TsPropertyModel tsPropertyModel,
                                                     NamedType namedType,
                                                     String jsonValuePropertyName) {
        if (!tsPropertyModel.getName().equals(jsonValuePropertyName)) {
            return tsPropertyModel;
        }

        if (tsPropertyModel.getTsType().format(new Settings()).contains("=")) {
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

    public List<Class<?>> getInterfaces(Class<?> type) {
        return Stream.of(type.getInterfaces())
                     .flatMap(interfaceType -> Stream.concat(Stream.of(interfaceType), getInterfaces(interfaceType).stream()))
                     .collect(Collectors.toList());
    }

    private Enum<?> getValue(Class<?> type, Map<? extends Enum<?>, Class<?>> jsonValueToJavaType) {
        return jsonValueToJavaType.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getValue().equals(type))
                                  .map(Map.Entry::getKey)
                                  .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                                  .findAny()
                                  .orElse(null);
    }

}
