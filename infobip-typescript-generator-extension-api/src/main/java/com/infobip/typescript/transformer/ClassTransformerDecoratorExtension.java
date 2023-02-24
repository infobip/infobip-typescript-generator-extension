package com.infobip.typescript.transformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infobip.jackson.CompositeJsonTypeResolver;
import com.infobip.jackson.JsonTypeResolver;
import com.infobip.jackson.JsonTypeResolverFactory;
import com.infobip.jackson.PresentPropertyJsonHierarchy;
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
    private final Map<Class<?>, List<Class<?>>> parentToChildren = new ConcurrentHashMap<>();

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

        Optional<Class<?>> typeArgument = parameterizedTypeClasses.getTypeArgument();
        if (Collection.class.isAssignableFrom(type) && typeArgument.isPresent()) {
            typeToDecorate = typeArgument.get();
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
                      .orElseGet(() -> getNonHierarchyDecorators(context.getSymbolTable(), tsPropertyModel, type));
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

    private Optional<Field> getField(TsBeanModel bean, TsPropertyModel model) {
        try {
            return Optional.of(bean.getOrigin().getDeclaredField(model.getName()));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    private ParameterizedTypeClasses getParameterizedTypeClasses(Field field) {
        return new ParameterizedTypeClasses(field.getType(), getTypeArgument(field));
    }

    private Optional<Class<?>> getTypeArgument(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypeArguments.length != 0 && actualTypeArguments[0] instanceof Class) {
                return Optional.of((Class<?>) actualTypeArguments[0]);
            }
        }
        return Optional.empty();
    }
}
