package com.infobip.typescript.type;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infobip.jackson.*;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.typescript.TypeScriptImportResolver;
import com.infobip.typescript.infrastructure.Symbols;
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

        return getAllJsonTypeResolvers(type)
            .filter(resolver -> resolver instanceof CompositeJsonTypeResolver<?>)
            .map(resolver -> (CompositeJsonTypeResolver<?>) resolver)
            .flatMap(resolver -> addTypeInformationToType(context, bean, resolver, type).stream())
            .reduce((a, b) -> a.withProperties(b.getProperties()))
            .or(() -> addTypeInformationToTypeInDynamicHierarchy(bean))
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

        var properties = addTypeInformationToType(context, bean, resolver, new NamedType(type, value.toString())).toList();

        if (properties.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(addProperties(bean, properties));
    }

    private <E extends Enum<E>> Map<E, Class<?>> getJsonValueToJavaType(CompositeJsonTypeResolver<E> resolver) {
        return Stream.of(resolver.getType().getEnumConstants())
                     .collect(Collectors.toMap(Function.identity(), resolver.getConverter()));
    }

    private Optional<TsBeanModel> addTypeInformationToTypeInDynamicHierarchy(TsBeanModel bean) {

        return dynamicHierarchyDeserializerProvider.get()
                                                   .map(deserializer -> addTypeInformationToTypeInDynamicHierarchy(bean,
                                                                                                                   deserializer))
                                                   .map(value -> value.orElse(null))
                                                   .filter(Objects::nonNull)
                                                   .findAny();
    }

    private Optional<TsBeanModel> addTypeInformationToTypeInDynamicHierarchy(TsBeanModel bean,
                                                                             DynamicHierarchyDeserializer<?> deserializer) {
        Class<?> type = bean.getOrigin();
        var jsonValueToJavaType = deserializer.getJsonValueToJavaType();
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

        return Optional.of(addTypeInformationToType(bean,
                                                    deserializer,
                                                    new NamedType(type, value)));
    }

    private Stream<TsPropertyModel> addTypeInformationToType(TsModelTransformer.Context context,
                                                             TsBeanModel tsBeanModel,
                                                             CompositeJsonTypeResolver<?> resolver,
                                                             NamedType namedType) {
        if (Objects.isNull(namedType)) {
            return Stream.empty();
        }

        return tsBeanModel.getProperties()
                          .stream()
                          .flatMap(tsPropertyModel -> addEnumTypeInformationToHierarchy(context,
                                                                                        tsPropertyModel,
                                                                                        resolver.getType(),
                                                                                        namedType).stream());
    }

    private <E extends Enum<E>> Optional<TsPropertyModel> addEnumTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                                                            TsPropertyModel tsPropertyModel,
                                                                                            Class<E> enumType,
                                                                                            NamedType namedType) {
        if (!doesFieldTypeMatch(context, tsPropertyModel, enumType)) {
            return Optional.empty();
        }

        E enumValue = Arrays.stream(enumType.getEnumConstants())
                            .filter(e -> e.name().equals(namedType.getName()))
                            .findFirst()
                            .orElse(null);
        return Optional.of(new TsPropertyModel(tsPropertyModel.name,
                                               new EnumInitializerType<>(getEnumInitializerType(tsPropertyModel, enumType),
                                                                         Objects.toString(enumValue)),
                                               tsPropertyModel.decorators,
                                               tsPropertyModel.modifiers.setReadonly(),
                                               true,
                                               null,
                                               tsPropertyModel.comments));
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
        TsType expected = new TsType.EnumReferenceType(Symbols.resolve(context.getSymbolTable(), enumType));

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

    private TsBeanModel addTypeInformationToType(TsBeanModel tsBeanModel,
                                                 DynamicHierarchyDeserializer<?> deserializer,
                                                 NamedType namedType) {
        return tsBeanModel.withProperties(tsBeanModel.getProperties()
                                                     .stream()
                                                     .map(tsPropertyModel -> addTypeInformationToType(tsPropertyModel,
                                                                                                      namedType,
                                                                                                      deserializer.getJsonValuePropertyName()))
                                                     .collect(Collectors.toList())
                                         );
    }

    private TsPropertyModel addTypeInformationToType(TsPropertyModel tsPropertyModel,
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

        if (type.contains("$")) {
            String typeWithoutLastDollarSign = type.substring(0, type.length() - 1);
            return typeWithoutLastDollarSign.substring(typeWithoutLastDollarSign.lastIndexOf('$') + 1);
        }

        return type;
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

    private Stream<JsonTypeResolver> getAllJsonTypeResolvers(Class<?> type) {
        return getInterfaces(type).stream()
                                  .filter(t -> !t.equals(SimpleJsonHierarchy.class) && !t.equals(PresentPropertyJsonHierarchy.class))
                                  .flatMap(t -> factory.create(t).stream());
    }

    private TsBeanModel addProperties(TsBeanModel bean, List<TsPropertyModel> properties) {
        var nameToProperty = properties.stream()
                                       .collect(Collectors.toMap(TsPropertyModel::getName, Function.identity()));

        return bean.withProperties(bean.getProperties().stream()
                                       .map(property -> {
                                           var overriddenProperty = nameToProperty.get(property.getName());

                                           if (Objects.nonNull(overriddenProperty)) {
                                               return overriddenProperty;
                                           }

                                           return property;
                                       }).toList());
    }

}
