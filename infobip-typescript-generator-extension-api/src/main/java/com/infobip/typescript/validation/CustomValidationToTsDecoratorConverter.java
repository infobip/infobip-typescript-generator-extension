package com.infobip.typescript.validation;

import com.infobip.typescript.custom.validation.CustomValidationAnnotation;
import com.infobip.typescript.custom.validation.CustomValidationData;
import com.infobip.typescript.validation.exception.TSValidatorDoesNotExist;
import cz.habarta.typescript.generator.emitter.TsDecorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomValidationToTsDecoratorConverter {

    private final CustomValidationData customValidationData;
    private final CompositeBeanValidationToTsDecoratorConverter compositeBeanValidationToTsDecoratorConverter;
    private final CustomAnnotationToTsDecoratorConverter customAnnotationToTsDecoratorConverter;

    public CustomValidationToTsDecoratorConverter(CustomValidationData customValidationData,
                                                  CompositeBeanValidationToTsDecoratorConverter compositeBeanValidationToTsDecoratorConverter,
                                                  ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        this.customValidationData = customValidationData;
        this.compositeBeanValidationToTsDecoratorConverter = compositeBeanValidationToTsDecoratorConverter;
        this.customAnnotationToTsDecoratorConverter = new CustomAnnotationToTsDecoratorConverter(
                validationMessageReferenceResolver);
    }

    //TODO tu trebaju doći već custom annotacije
    public Stream<TsDecorator> convert(Field field, Annotation annotation) {
        Stream<TsDecorator> customDecorators = Stream.empty();
        Stream<TsDecorator> beanDecorators = Stream.empty();
        List<Annotation> beanAnnotations = extractBeanAnnotations(annotation);

        if (isCustomAnnotation(annotation) && !beanAnnotations.isEmpty()) {
            validate(annotation);
        }
        CustomValidationAnnotation customValidationAnnotation = customValidationData.getCustomValidationAnnotations()
                                                                                    .get(annotation.annotationType());
        customDecorators = customAnnotationToTsDecoratorConverter.convert(annotation, customValidationAnnotation);

        if (!beanAnnotations.isEmpty()) {
            beanDecorators = beanAnnotations.stream()
                                            .flatMap(beanAnnotation -> convertBeanAnnotation(field, beanAnnotation));
        }

        return Stream.concat(beanDecorators, customDecorators);
    }

    private void validate(Annotation annotation) {
        String annotationName = annotation.annotationType().getName();
        if (!customValidationData.getValidatorsNames().contains(annotationName)) {
            throw new TSValidatorDoesNotExist(annotation);
        }
    }

    private Boolean isCustomAnnotation(Annotation annotation) {
        return customValidationData.getCustomValidationAnnotations().keySet().stream()
                                   .anyMatch(annotationType -> annotationType.equals(annotation.annotationType()));
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
