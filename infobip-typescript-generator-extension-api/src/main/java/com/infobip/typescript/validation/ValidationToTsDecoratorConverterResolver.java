package com.infobip.typescript.validation;

import com.infobip.typescript.CustomTypeScriptDecorator;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;
import com.infobip.typescript.validation.exception.TSValidatorDoesNotExist;
import cz.habarta.typescript.generator.emitter.TsDecorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidationToTsDecoratorConverterResolver {

    private final CompositeBeanValidationToTsDecoratorConverter compositeBeanValidationToTsDecoratorConverter;
    private final CustomValidationToTsDecoratorConverter customValidationToTsDecoratorConverter;
    private final List<String> decoratorNames;
    private final List<Class<? extends Annotation>> customAnnotations;

    public ValidationToTsDecoratorConverterResolver(String customMessageSource,
                                                    List<TSCustomDecorator> tsCustomDecorators,
                                                    List<Class<? extends Annotation>> customAnnotations) {
        ValidationMessageReferenceResolver validationMessageReferenceResolver = new ValidationMessageReferenceResolver(
                customMessageSource);
        CompositeBeanValidationToTsDecoratorConverter converter = new CompositeBeanValidationToTsDecoratorConverter(
                validationMessageReferenceResolver);
        this.decoratorNames = tsCustomDecorators.stream().map(TSCustomDecorator::getName).collect(Collectors.toList());
        this.customAnnotations = customAnnotations;
        this.compositeBeanValidationToTsDecoratorConverter = converter;
        this.customValidationToTsDecoratorConverter = new CustomValidationToTsDecoratorConverter( converter, validationMessageReferenceResolver);
    }

    public Stream<TsDecorator> getDecorators(Annotation annotation, Field field) {
        if (isBeanValidation(annotation)) {
            return compositeBeanValidationToTsDecoratorConverter.convert(field, annotation);
        } else if (isCustomValidation(annotation)) {
            validateCustomAnnotation(annotation);
            return customValidationToTsDecoratorConverter.convert(field, annotation);
        }
        return Stream.of();
    }

    private Boolean isBeanValidation(Annotation annotation) {
        return compositeBeanValidationToTsDecoratorConverter.getBeanValidationAnnotations()
                                                            .contains(annotation.annotationType());
    }

    private boolean isCustomValidation(Annotation annotation) {
        return customAnnotations.contains(annotation.annotationType());
    }

    private void validateCustomAnnotation(Annotation annotation) {
        CustomTypeScriptDecorator customTypeScriptDecorator = getCustomTSDecoratorAnnotation(annotation);
        String annotationName = customTypeScriptDecorator.typeScriptDecorator().isEmpty()
                ? annotation.annotationType().getSimpleName()
                : customTypeScriptDecorator.typeScriptDecorator();
        if (!decoratorNames.contains(annotationName)) {
            throw new TSValidatorDoesNotExist(annotation);
        }
    }

    private CustomTypeScriptDecorator getCustomTSDecoratorAnnotation(Annotation annotation) {
        return annotation.annotationType().getAnnotation(CustomTypeScriptDecorator.class);
    }
}
