package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.TsDecorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.stream.Stream;

public class ValidationToTsDecoratorConverterResolver {

    private final CompositeBeanValidationToTsDecoratorConverter compositeBeanValidationToTsDecoratorConverter;
    private final CustomValidationToTsDecoratorConverter customValidationToTsDecoratorConverter;

    public ValidationToTsDecoratorConverterResolver(CompositeBeanValidationToTsDecoratorConverter compositeBeanValidationToTsDecoratorConverter,
                                                    CustomValidationToTsDecoratorConverter customValidationToTsDecoratorConverter) {
        this.compositeBeanValidationToTsDecoratorConverter = compositeBeanValidationToTsDecoratorConverter;
        this.customValidationToTsDecoratorConverter = customValidationToTsDecoratorConverter;
    }

    public Stream<TsDecorator> getDecorators(Annotation annotation, Field field) {
        if (isBeanValidation(annotation)) {
            return compositeBeanValidationToTsDecoratorConverter.convert(field, annotation);
        }
        return customValidationToTsDecoratorConverter.convert(field, annotation);
    }

    private Boolean isBeanValidation(Annotation annotation) {
        return compositeBeanValidationToTsDecoratorConverter.getBeanValidationAnnotations()
                                                            .contains(annotation.annotationType());
    }
}
