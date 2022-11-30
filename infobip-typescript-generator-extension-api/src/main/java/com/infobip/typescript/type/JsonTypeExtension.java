package com.infobip.typescript.type;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infobip.jackson.CompositeJsonTypeResolver;
import com.infobip.jackson.JsonTypeResolverFactory;
import com.infobip.jackson.PresentPropertyJsonHierarchy;
import com.infobip.jackson.SimpleJsonHierarchy;
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

        if (type.equals(SimpleJsonHierarchy.class) || type.equals(PresentPropertyJsonHierarchy.class)) {
            return beans;
        }

        return factory.create(type)
                      .filter(resolver -> resolver instanceof CompositeJsonTypeResolver<?>)
                      .map(resolver -> (CompositeJsonTypeResolver<?>) resolver)
                      .map(resolver -> addTypeInformationToHierarchy(context, beans, resolver))
                      .orElse(beans);
    }

    private List<TsBeanModel> addTypeInformationToHierarchy(TsModelTransformer.Context context,
                                                            List<TsBeanModel> beans,
                                                            CompositeJsonTypeResolver<?> resolver) {
        List<NamedType> subtypes = findSubtypes(resolver);
        Map<Class<?>, NamedType> typeToNamedType = subtypes.stream()
                                                           .collect(Collectors.toMap(NamedType::getType,
                                                                                     Function.identity()));
        return beans.stream()
                    .map(bean -> addTypeInformationToHierarchy(context, bean, resolver,
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
                                                     .map(tsPropertyModel -> addTypeInformationToHierarchy(context,
                                                                                                           tsPropertyModel,
                                                                                                           resolver.getType(),
                                                                                                           namedType))
                                                     .collect(Collectors.toList())
        );
    }

    private <E extends Enum<E>> TsPropertyModel addTypeInformationToHierarchy(TsModelTransformer.Context context,
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
        return new TsPropertyModel(tsPropertyModel.name, new EnumInitializerType<>(type, enumType),
                                   tsPropertyModel.decorators,
                                   tsPropertyModel.modifiers.setReadonly(),
                                   true,
                                   null,
                                   tsPropertyModel.comments);
    }

    private <E extends Enum<E>> List<NamedType> findSubtypes(CompositeJsonTypeResolver<E> resolver) {
        return Stream.of(resolver.getType().getEnumConstants())
                     .filter(constant -> Objects.nonNull(resolver.getConverter().apply(constant)))
                     .map(constant -> new NamedType(resolver.getConverter().apply(constant), constant.name()))
                     .collect(Collectors.toList());
    }
}
