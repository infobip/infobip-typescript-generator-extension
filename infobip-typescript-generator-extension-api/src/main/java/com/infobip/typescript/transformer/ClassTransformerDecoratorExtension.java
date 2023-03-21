package com.infobip.typescript.transformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infobip.jackson.CompositeJsonTypeResolver;
import com.infobip.jackson.JsonTypeResolver;
import com.infobip.jackson.JsonTypeResolverFactory;
import com.infobip.jackson.PresentPropertyJsonHierarchy;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.Symbol;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.*;

public class ClassTransformerDecoratorExtension extends Extension {

    static final TsIdentifierReference TYPE = new TsIdentifierReference("@Type");
    private final JsonTypeResolverFactory factory = new JsonTypeResolverFactory();
    private final Supplier<Stream<DynamicHierarchyDeserializer<?>>> dynamicHierarchyDeserializerProvider;
    private final Map<Class<?>, List<Class<?>>> parentToChildren = new ConcurrentHashMap<>();

    public ClassTransformerDecoratorExtension() {
        this(Stream::empty);
    }

    public ClassTransformerDecoratorExtension(Supplier<Stream<DynamicHierarchyDeserializer<?>>> dynamicHierarchyDeserializerProvider1) {
        this.dynamicHierarchyDeserializerProvider = dynamicHierarchyDeserializerProvider1;
    }

    @Override
    public EmitterExtensionFeatures getFeatures() {
        EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = true;
        return features;
    }

    @Override
    public List<TransformerDefinition> getTransformers() {
        return Collections.singletonList(
            new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting,
                                      (TsModelTransformer) this::decorateClass));
    }

    private TsModel decorateClass(TsModelTransformer.Context context, TsModel model) {
        List<TsBeanModel> newBeans = model.getBeans()
                                          .stream()
                                          .map(bean -> decorateClass(context, bean))
                                          .collect(Collectors.toList());

        Map<Class<?>, TsBeanModel> originToModel = newBeans.stream()
                                                           .collect(Collectors.toMap(TsDeclarationModel::getOrigin,
                                                                                     Function.identity()));
        List<TsBeanModel> sorted = new TsBeanModelTransformerDecoratorSorter(originToModel).sort(parentToChildren,
                                                                                                 newBeans);
        return model.withBeans(sorted);
    }

    private TsBeanModel decorateClass(TsModelTransformer.Context context, TsBeanModel bean) {
        return bean.withProperties(bean.getProperties()
                                       .stream()
                                       .map(model -> getDecorators(context, bean, model))
                                       .collect(Collectors.toList())
                                  );
    }

    private TsPropertyModel getDecorators(TsModelTransformer.Context context, TsBeanModel model, TsPropertyModel tsPropertyModel) {
        return getField(model, tsPropertyModel).map(this::getParameterizedTypeClasses)
                                               .map(parameterizedTypeClasses -> tsPropertyModel.withDecorators(
                                                   getDecorators(context, model, tsPropertyModel,
                                                                 resolveTypeToDecorate(parameterizedTypeClasses))))
                                               .orElse(tsPropertyModel);
    }

    private Class<?> resolveTypeToDecorate(ParameterizedTypeClasses parameterizedTypeClasses) {
        Class<?> type = parameterizedTypeClasses.getType();
        Class<?> typeToDecorate = type;
        if (type.isArray()) {
            typeToDecorate = type.getComponentType();
        }

        return resolveTypeRecursively(parameterizedTypeClasses, type, typeToDecorate);
    }

    private Class<?> resolveTypeRecursively(ParameterizedTypeClasses parameterizedTypeClasses, Class<?> type, Class<?> typeToDecorate) {
        Optional<Class<?>> typeArgument = parameterizedTypeClasses.getTypeArgument();

        if (Collection.class.isAssignableFrom(type) || Optional.class.isAssignableFrom(type)) {
            return typeArgument.orElse(typeToDecorate);
        }

        return typeToDecorate;
    }

    private List<TsDecorator> getDecorators(TsModelTransformer.Context context,
                                            TsBeanModel model,
                                            TsPropertyModel tsPropertyModel,
                                            Class<?> type) {
        return factory.create(type)
                      .filter(resolver -> isHierarchicalDecoratorNeeded(resolver, type))
                      .map(resolver -> (CompositeJsonTypeResolver<?>) resolver)
                      .map(resolver -> getHierarchyDecorators(context.getSymbolTable(), model, tsPropertyModel, resolver))
                      .or(() -> getDynamicHierarchyDecorators(context, model, tsPropertyModel, type))
                      .orElseGet(() -> getNonHierarchyDecorators(context.getSymbolTable(), tsPropertyModel, type));
    }

    private Optional<? extends List<TsDecorator>> getDynamicHierarchyDecorators(TsModelTransformer.Context context,
                                                                                TsBeanModel model,
                                                                                TsPropertyModel tsPropertyModel,
                                                                                Class<?> type) {
        return dynamicHierarchyDeserializerProvider.get()
                                                   .filter(deserializer -> deserializer.getHierarchyRootType().equals(type))
                                                   .findFirst()
                                                   .map(deserializer -> getHierarchyDecoratorsFromDeserializer(context.getSymbolTable(),
                                                                                                               model,
                                                                                                               tsPropertyModel,
                                                                                                               deserializer));
    }

    private boolean isHierarchicalDecoratorNeeded(JsonTypeResolver resolver, Class<?> type) {
        return resolver instanceof CompositeJsonTypeResolver<?> && isHierarchyRoot(type);
    }

    private boolean isHierarchyRoot(Class<?> type) {
        return Modifier.isAbstract(type.getModifiers()) || type.isInterface();
    }

    private List<TsDecorator> getHierarchyDecorators(SymbolTable symbolTable,
                                                     TsBeanModel model,
                                                     TsPropertyModel tsPropertyModel,
                                                     CompositeJsonTypeResolver<?> resolver) {
        TsArrowFunction emptyToObject = new TsArrowFunction(Collections.emptyList(), new TsTypeReferenceExpression(
            new TsType.ReferenceType(new Symbol("Object"))));
        TsStringLiteral property = new TsStringLiteral(resolver.getTypePropertyName());
        TsArrayLiteral subTypes = new TsArrayLiteral(getSubtypes(symbolTable, model, resolver));
        DiscriminatorValueTsObjectLiteral discriminatorValue = new DiscriminatorValueTsObjectLiteral(
            new TsPropertyDefinition("property", property),
            new TsPropertyDefinition("subTypes",
                                     subTypes));
        DiscriminatorTsObjectLiteral discriminator = new DiscriminatorTsObjectLiteral(
            new TsPropertyDefinition("discriminator", discriminatorValue));
        List<TsExpression> arguments = Stream.of(emptyToObject, discriminator).collect(Collectors.toList());
        return Stream.concat(tsPropertyModel.getDecorators().stream(),
                             Stream.of(new TsDecorator(TYPE, arguments)))
                     .collect(Collectors.toList());
    }

    private List<TsDecorator> getHierarchyDecoratorsFromDeserializer(SymbolTable symbolTable,
                                                                     TsBeanModel model,
                                                                     TsPropertyModel tsPropertyModel,
                                                                     DynamicHierarchyDeserializer<?> deserializer) {
        TsArrowFunction emptyToObject = new TsArrowFunction(Collections.emptyList(), new TsTypeReferenceExpression(
            new TsType.ReferenceType(new Symbol("Object"))));
        TsStringLiteral property = new TsStringLiteral(deserializer.getJsonValuePropertyName());
        TsArrayLiteral subTypes = new TsArrayLiteral(getSubtypes(symbolTable, model, deserializer));
        DiscriminatorValueTsObjectLiteral discriminatorValue = new DiscriminatorValueTsObjectLiteral(
            new TsPropertyDefinition("property", property),
            new TsPropertyDefinition("subTypes",
                                     subTypes));
        DiscriminatorTsObjectLiteral discriminator = new DiscriminatorTsObjectLiteral(
            new TsPropertyDefinition("discriminator", discriminatorValue));
        List<TsExpression> arguments = Stream.of(emptyToObject, discriminator).collect(Collectors.toList());
        return Stream.concat(tsPropertyModel.getDecorators().stream(),
                             Stream.of(new TsDecorator(TYPE, arguments)))
                     .collect(Collectors.toList());
    }

    private List<TsDecorator> getNonHierarchyDecorators(SymbolTable symbolTable,
                                                        TsPropertyModel tsPropertyModel,
                                                        Class<?> type) {

        TsArrowFunction emptyToTypeName = new TsArrowFunction(Collections.emptyList(), new TsTypeReferenceExpression(
            new TsType.ReferenceType(symbolTable.getSymbol(type))));

        Stream<TsDecorator> typeDecoratorStream = shouldNotBeDecorated(type) ?
            Stream.empty() :
            Stream.of(new TsDecorator(TYPE, Collections.singletonList(emptyToTypeName)));

        return Stream.concat(tsPropertyModel.getDecorators().stream(), typeDecoratorStream)
                     .collect(Collectors.toList());
    }

    private List<TsExpression> getSubtypes(SymbolTable symbolTable,
                                           TsBeanModel model,
                                           CompositeJsonTypeResolver<?> resolver) {
        List<NamedType> subtypes = findSubtypes(resolver);
        appendToParentToChildren(model.getOrigin(), Stream.of(resolver.getType()));
        appendToParentToChildren(model.getOrigin(), subtypes.stream().map(NamedType::getType));
        return subtypes.stream()
                       .map(type -> new TsObjectLiteral(
                           new TsPropertyDefinition("value",
                                                    new TsTypeReferenceExpression(
                                                        new TsType.ReferenceType(
                                                            symbolTable.getSymbol(type.getType())))),
                           new TsPropertyDefinition("name", new TsEnumLiteral(resolver.getType(), type.getName()))))
                       .collect(Collectors.toList());
    }

    private List<TsExpression> getSubtypes(SymbolTable symbolTable,
                                           TsBeanModel model,
                                           DynamicHierarchyDeserializer<?> deserializer) {
        List<NamedType> subtypes = findSubtypesFromDeserializer(deserializer);
        appendToParentToChildren(model.getOrigin(), Stream.of(deserializer.getHierarchyRootType()));
        appendToParentToChildren(model.getOrigin(), subtypes.stream().map(NamedType::getType));
        return subtypes.stream()
                       .map(type -> new TsObjectLiteral(
                           new TsPropertyDefinition("value",
                                                    new TsTypeReferenceExpression(
                                                        new TsType.ReferenceType(
                                                            symbolTable.getSymbol(type.getType())))),
                           new TsPropertyDefinition("name", new TsStringLiteral(type.getName()))))
                       .collect(Collectors.toList());
    }

    private boolean shouldNotBeDecorated(Class<?> type) {
        return isBuiltInType(type) || isTsTypeResolutionUnsupported(type);
    }

    private boolean isTsTypeResolutionUnsupported(Class<?> type) {
        return type.isPrimitive() || type.isInterface() || type.isEnum() || PresentPropertyJsonHierarchy.class.isAssignableFrom(type);
    }

    private boolean isBuiltInType(Class<?> type) {
        return Optional.ofNullable(type.getPackage()).map(Package::getName).orElse("").startsWith("java");
    }

    private void appendToParentToChildren(Class<?> key, Stream<? extends Class<?>> value) {
        List<Class<?>> current = parentToChildren.getOrDefault(key, Collections.emptyList());
        parentToChildren.put(key, Stream.concat(current.stream(), value).collect(Collectors.toList()));
    }

    private <E extends Enum<E>> List<NamedType> findSubtypes(CompositeJsonTypeResolver<E> resolver) {
        return Stream.of(resolver.getType().getEnumConstants())
                     .filter(constant -> Objects.nonNull(resolver.getConverter().apply(constant)))
                     .map(constant -> new NamedType(resolver.getConverter().apply(constant), constant.name()))
                     .collect(Collectors.toList());
    }

    private <E extends Enum<E>> List<NamedType> findSubtypesFromDeserializer(DynamicHierarchyDeserializer<?> deserializer) {
        return deserializer.getJsonValueToJavaType()
                           .entrySet()
                           .stream()
                           .map(entry -> new NamedType(entry.getValue(), entry.getKey()))
                           .collect(Collectors.toList());
    }

    private Optional<Field> getField(TsBeanModel bean, TsPropertyModel model) {
        try {
            return Optional.of(bean.getOrigin().getDeclaredField(model.getName()));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    private ParameterizedTypeClasses getParameterizedTypeClasses(Field field) {
        return new ParameterizedTypeClasses(field.getType(), getReferenceTargetType(field));
    }

    private Optional<Class<?>> getReferenceTargetType(Field field) {
        Type genericType = field.getGenericType();
        return getReferenceTargetTypeRecursively(genericType);
    }

    private Optional<Class<?>> getReferenceTargetTypeRecursively(Type type) {

        if (type instanceof Class) {
            return Optional.of((Class<?>) type);
        }

        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                var actualType = actualTypeArguments[0];
                return getReferenceTargetTypeRecursively(actualType);
            }
        }

        return Optional.empty();
    }

}
