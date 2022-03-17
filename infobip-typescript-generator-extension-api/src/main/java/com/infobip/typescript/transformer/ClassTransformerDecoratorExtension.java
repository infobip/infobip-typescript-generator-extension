package com.infobip.typescript.transformer;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infobip.jackson.*;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.compiler.*;
import cz.habarta.typescript.generator.emitter.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassTransformerDecoratorExtension extends Extension {

    static final TsIdentifierReference TYPE = new TsIdentifierReference("@Type");
    private final JsonTypeResolverFactory factory = new JsonTypeResolverFactory();
    private final Map<Class<?>, List<Class<?>>> parentToChildren = new ConcurrentHashMap<>();

    @Override
    public EmitterExtensionFeatures getFeatures() {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = true;
        return features;
    }

    @Override
    public List<TransformerDefinition> getTransformers() {
        return Collections.singletonList(
                new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting,
                                          (ModelTransformer) this::decorateClass));
    }

    private TsModel decorateClass(SymbolTable symbolTable, TsModel model) {
        List<TsBeanModel> newBeans = model.getBeans()
                                          .stream()
                                          .map(bean -> decorateClass(symbolTable, bean))
                                          .collect(Collectors.toList());

        Map<Class<?>, TsBeanModel> originToModel = newBeans.stream()
                                                           .collect(Collectors.toMap(TsDeclarationModel::getOrigin,
                                                                                     Function.identity()));
        List<TsBeanModel> sorted = new TsBeanModelTransformerDecoratorSorter(originToModel).sort(parentToChildren,
                                                                                                 newBeans);
        return model.withBeans(sorted);
    }

    private TsBeanModel decorateClass(SymbolTable symbolTable, TsBeanModel bean) {
        return bean.withProperties(bean.getProperties()
                                       .stream()
                                       .map(model -> getDecorators(symbolTable, bean, model))
                                       .collect(Collectors.toList())
        );
    }

    private TsPropertyModel getDecorators(SymbolTable symbolTable, TsBeanModel model, TsPropertyModel tsPropertyModel) {
        return getField(model, tsPropertyModel).map(this::getGenericTypeInfo)
                                               .map(genericTypeInfo -> tsPropertyModel.withDecorators(
                                                       getDecorators(symbolTable, model, tsPropertyModel,
                                                                     genericTypeInfo)))
                                               .orElse(tsPropertyModel);
    }

    private List<TsDecorator> getDecorators(SymbolTable symbolTable,
                                            TsBeanModel model,
                                            TsPropertyModel tsPropertyModel,
                                            GenericTypeInfo genericTypeInfo) {
        return factory.create(genericTypeInfo.getType())
                      .filter(resolver -> resolver instanceof CompositeJsonTypeResolver<?>)
                      .map(resolver -> (CompositeJsonTypeResolver<?>) resolver)
                      .map(resolver -> getDecorators(symbolTable, model, tsPropertyModel, resolver))
                      .orElseGet(() -> getNonHierarchyDecorators(tsPropertyModel, genericTypeInfo));
    }

    private List<TsDecorator> getDecorators(SymbolTable symbolTable,
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

    private List<TsDecorator> getNonHierarchyDecorators(TsPropertyModel tsPropertyModel,
                                                        GenericTypeInfo genericTypeInfo) {
        Class<?> type = genericTypeInfo.getType();
        Class<?> typeToDecorate = type;
        if (type.isArray()) {
            typeToDecorate = type.getComponentType();
        }

        final Optional<Class<?>> genericType = genericTypeInfo.getGenericType();
        if (Collection.class.isAssignableFrom(type) && genericType.isPresent()) {
            typeToDecorate = genericType.get();
        }
        return getNonHierarchyDecorators(tsPropertyModel, typeToDecorate);
    }

    private List<TsDecorator> getNonHierarchyDecorators(TsPropertyModel tsPropertyModel,
                                                        Class<?> type) {

        TsArrowFunction emptyToTypeName = new TsArrowFunction(Collections.emptyList(), new TsTypeReferenceExpression(
                new TsType.ReferenceType(new Symbol(type.getSimpleName()))));

        final Stream<TsDecorator> typeDecoratorStream = shouldNotBeDecorated(type) ?
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

    private boolean shouldNotBeDecorated(Class<?> type) {
        return isBuiltIn(type) || isTsTypeResolutionUnsupported(type);
    }

    private boolean isTsTypeResolutionUnsupported(Class<?> type) {
        return type.isEnum() || PresentPropertyJsonHierarchy.class.isAssignableFrom(type);
    }

    private boolean isBuiltIn(Class<?> type) {
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

    private Optional<Field> getField(TsBeanModel bean, TsPropertyModel model) {
        try {
            return Optional.of(bean.getOrigin().getDeclaredField(model.getName()));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    private GenericTypeInfo getGenericTypeInfo(Field field) {
        return new GenericTypeInfo(field.getType(), getGenericType(field));
    }

    private Optional<Class<?>> getGenericType(Field field) {
        final Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            final Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypeArguments.length != 0) {
                return Optional.of((Class<?>) actualTypeArguments[0]);
            }
        }
        return Optional.empty();
    }

    private static class GenericTypeInfo {

        private final Class<?> type;
        private final Optional<Class<?>> genericType;

        GenericTypeInfo(Class<?> type, Optional<Class<?>> genericType) {
            this.type = type;
            this.genericType = genericType;
        }

        Class<?> getType() {
            return type;
        }

        Optional<Class<?>> getGenericType() {
            return genericType;
        }
    }
}
