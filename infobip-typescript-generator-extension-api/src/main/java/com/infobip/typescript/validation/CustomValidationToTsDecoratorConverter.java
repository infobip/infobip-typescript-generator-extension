package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.TsDecorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomValidationToTsDecoratorConverter {

    private final CompositeBeanValidationToTsDecoratorConverter compositeBeanValidationToTsDecoratorConverter;
    private final CustomAnnotationToTsDecoratorConverter customAnnotationToTsDecoratorConverter;

    public CustomValidationToTsDecoratorConverter(CompositeBeanValidationToTsDecoratorConverter compositeBeanValidationToTsDecoratorConverter,
                                                  ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        this.compositeBeanValidationToTsDecoratorConverter = compositeBeanValidationToTsDecoratorConverter;
        this.customAnnotationToTsDecoratorConverter = new CustomAnnotationToTsDecoratorConverter(
                validationMessageReferenceResolver);
    }

    public Stream<TsDecorator> convert(Field field, Annotation annotation) {
        Stream<TsDecorator> beanDecorators = Stream.empty();
        List<Annotation> beanAnnotations = extractBeanAnnotations(annotation);

        Stream<TsDecorator> customDecorators = customAnnotationToTsDecoratorConverter.convert(annotation);

        if (!beanAnnotations.isEmpty()) {
            beanDecorators = beanAnnotations.stream()
                                            .flatMap(beanAnnotation -> convertBeanAnnotation(field, beanAnnotation));
        }

        return Stream.concat(beanDecorators, customDecorators);
    }

    private List<Annotation> extractBeanAnnotations(Annotation customAnnotation) {
        return Arrays.stream(customAnnotation.annotationType().getAnnotations())
                     .filter(this::isBeanAnnotation)
                     .collect(Collectors.toList());
    }

    private Boolean isBeanAnnotation(Annotation annotation) {
        return compositeBeanValidationToTsDecoratorConverter.getBeanValidationAnnotations()
                                                            .contains(annotation.annotationType());
    }

    private Stream<TsDecorator> convertBeanAnnotation(Field field, Annotation annotation) {
        return compositeBeanValidationToTsDecoratorConverter.convert(field, annotation);
    }
}
