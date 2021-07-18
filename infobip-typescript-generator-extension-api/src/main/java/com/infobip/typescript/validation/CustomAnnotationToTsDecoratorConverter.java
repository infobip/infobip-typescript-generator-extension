package com.infobip.typescript.validation;

import com.infobip.typescript.custom.validation.CustomValidationAnnotation;
import com.infobip.typescript.validation.exception.CanNotResolveMethod;
import cz.habarta.typescript.generator.emitter.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CustomAnnotationToTsDecoratorConverter {

    private static final String MESSAGE_METHOD = "message";

    private final ValidationMessageReferenceResolver validationMessageReferenceResolver;

    CustomAnnotationToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        this.validationMessageReferenceResolver = validationMessageReferenceResolver;
    }

    public Stream<TsDecorator> convert(Annotation annotation,
                                       CustomValidationAnnotation customValidationAnnotation) {
        String annotationName = customValidationAnnotation.getAnnotation().getSimpleName();
        Optional<String> message = extractMessage(annotation);
        Stream<TsExpression> referenceStream = getReferences(annotation, customValidationAnnotation);
        if (message.isPresent()) {
            return Stream.of(
                    new TsDecorator(new TsIdentifierReference("@" + annotationName),
                                    Stream.concat(referenceStream,
                                                  internationalization(annotation, annotationName, message.get()))
                                          .collect(Collectors.toList())));
        } else {
            return Stream.of(
                    new TsDecorator(new TsIdentifierReference("@" + annotationName),
                                    referenceStream.collect(Collectors.toList())));
        }
    }

    private Stream<TsExpression> getReferences(Annotation annotation,
                                               CustomValidationAnnotation customValidationAnnotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                     .filter(method -> customValidationAnnotation.getMethods()
                                                                 .contains(method.getName()) && !method.getName()
                                                                                                       .equals(MESSAGE_METHOD))
                     .map(method -> invoke(annotation, method))
                     .map(this::convert);
    }

    private Optional<String> extractMessage(Annotation annotation) {
        // TODO config message
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                     .filter(method -> method.getName().equals(MESSAGE_METHOD))
                     .findFirst()
                     .map(method -> invoke(annotation, method).toString());
    }

    private Stream<TsExpression> internationalization(Annotation annotation, String annotationName, String message) {
        String validationsReference = validationMessageReferenceResolver.getMessageReference(() -> message,
                                                                                             annotationName);
        return Stream.of(new TsObjectLiteral(
                new TsPropertyDefinition(MESSAGE_METHOD,
                                         new TsCallExpression(new TsIdentifierReference(validationsReference)))));
    }

    private TsExpression convert(Object obj) {
        // TODO check all supported types
        return obj instanceof Number
                ? new TsNumberLiteral((Number) obj)
                : new TsStringLiteral((String) obj);
    }

    private Object invoke(Annotation annotation, Method method, Object... args) {
        try {
            return method.invoke(annotation, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CanNotResolveMethod(annotation, method);
        }
    }
}
