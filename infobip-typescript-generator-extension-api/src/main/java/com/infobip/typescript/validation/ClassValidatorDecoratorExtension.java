package com.infobip.typescript.validation;

import com.infobip.typescript.TypeScriptImportResolver;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.ModelTransformer;
import cz.habarta.typescript.generator.emitter.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.infobip.typescript.validation.CommonValidationMessages.COMMON_VALIDATION_MESSAGES_CLASS_NAME;

public class ClassValidatorDecoratorExtension extends Extension implements TypeScriptImportResolver {

    private static final Set<String> DEFAULT_VALIDATIONS;

    static {
        DEFAULT_VALIDATIONS = Stream.of("@ValidateNested(",
                                        "@IsDefined(",
                                        "@IsNotEmpty(",
                                        "@MaxLength(",
                                        "@MinLength(",
                                        "@Max(",
                                        "@Min(",
                                        "@ArrayMaxSize(",
                                        "@ArrayMinSize(")
                                    .collect(Collectors.toSet());
    }

    private final ValidationToTsDecoratorConverterResolver resolver;
    private final List<TSCustomDecorator> tsCustomDecorators;

    public ClassValidatorDecoratorExtension() {
        this(null, Collections.emptyList(), Collections.emptyList());
    }

    public ClassValidatorDecoratorExtension(String customMessageSource,
                                            List<TSCustomDecorator> tsCustomDecorators,
                                            List<Class<? extends Annotation>> customAnnotations) {

        this.resolver = new ValidationToTsDecoratorConverterResolver(customMessageSource,
                                                                     tsCustomDecorators,
                                                                     customAnnotations);
        this.tsCustomDecorators = tsCustomDecorators;
    }

    @Override
    public EmitterExtensionFeatures getFeatures() {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = true;
        return features;
    }

    @Override
    public List<TransformerDefinition> getTransformers() {
        return Collections.singletonList(
                new TransformerDefinition(ModelCompiler.TransformationPhase.BeforeEnums,
                                          (ModelTransformer) (symbolTable, model) ->
                                                  model.withBeans(model.getBeans().stream()
                                                                       .map(this::decorateClass)
                                                                       .collect(Collectors.toList())
                                                  ))
        );
    }

    @Override
    public List<String> resolve(String typeScript) {
        Stream<String> resolvedValidations = Stream.of();
        Stream<String> resolvedCustomValidations = Stream.of();
        String usedValidations = getUsedValidations(typeScript);
        List<TSCustomDecorator> usedCustomValidations = getUsedCustomDecorators(typeScript);

        if (!usedValidations.isEmpty()) {
            resolvedValidations = resolve(typeScript, usedValidations);
        }

        if (!usedCustomValidations.isEmpty()) {
            resolvedCustomValidations = resolve(usedCustomValidations);
        }

        return Stream.concat(resolvedValidations, resolvedCustomValidations).collect(Collectors.toList());
    }

    private String getUsedValidations(String typeScript) {
        return DEFAULT_VALIDATIONS.stream()
                           .filter(typeScript::contains)
                           .map(validation -> validation.substring(1, validation.length() - 1))
                           .collect(Collectors.joining(", "));
    }

    private List<TSCustomDecorator> getUsedCustomDecorators(String typeScript) {
        return tsCustomDecorators.stream()
                          .filter(decorator -> typeScript.contains(
                                  "@" + decorator.getName() + "("))
                          .collect(Collectors.toList());
    }

    @NotNull
    private Stream<String> resolve(String typeScript, String usedValidations) {
        String validationImport = "import { " + usedValidations + " } from 'class-validator';";

        if (typeScript.contains(COMMON_VALIDATION_MESSAGES_CLASS_NAME)) {
            String commonValidationMessagesImport = "import { CommonValidationMessages } from './CommonValidationMessages';";
            return Arrays.asList(validationImport, commonValidationMessagesImport).stream();
        }

        return Collections.singletonList(validationImport).stream();
    }

    @NotNull
    private Stream<String> resolve(List<TSCustomDecorator> tsCustomDecorators) {
        return tsCustomDecorators.stream()
                                 .map(decorator -> "import { " + decorator.getName() + " } from '" + convert(
                                         decorator.getTsPath()) + "';");
    }

    private String convert(Path tsPath) {
        return tsPath
                .toString()
                .replace("\\", "/");
    }

    private TsBeanModel decorateClass(TsBeanModel bean) {
        return bean.withProperties(bean.getProperties()
                                       .stream()
                                       .map(model -> getDecorators(bean, model))
                                       .collect(Collectors.toList())
        );
    }

    private TsPropertyModel getDecorators(TsBeanModel bean, TsPropertyModel model) {
        return getField(bean, model).map(field -> model.withDecorators(getDecorators(model, field)))
                                    .orElse(model);
    }

    private List<TsDecorator> getDecorators(TsPropertyModel model, Field field) {
        Stream<TsDecorator> newDecorators = Arrays.stream(field.getAnnotations())
                                                  .flatMap(annotation -> resolver.getDecorators(annotation, field));
        return Stream.concat(model.getDecorators().stream(), newDecorators).collect(Collectors.toList());
    }

    private Optional<Field> getField(TsBeanModel bean, TsPropertyModel model) {
        try {
            return Optional.of(bean.getOrigin().getDeclaredField(model.getName()));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }
}
