package com.infobip.typescript.validation;

import com.infobip.typescript.CustomTSDecorator;
import com.infobip.typescript.DecoratorParameterListExtractor;
import com.infobip.typescript.validation.converter.ObjectToTSLiteralConverter;
import com.infobip.typescript.validation.exception.CanNotResolveMethod;
import com.infobip.typescript.validation.exception.TSParameterExtractorInstantiationException;
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
    private final ObjectToTSLiteralConverter objectToTSLiteralConverter;

    CustomAnnotationToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        this.validationMessageReferenceResolver = validationMessageReferenceResolver;
        this.objectToTSLiteralConverter = new ObjectToTSLiteralConverter();
    }

    public Stream<TsDecorator> convert(Annotation annotation) {
        CustomTSDecorator customTSDecorator = getCustomTSDecorator(annotation);
        String annotationName = getAnnotationName(annotation, customTSDecorator);
        Optional<String> message = extractMessage(annotation);
        Stream<TsExpression> referenceStream = getReferences(annotation, customTSDecorator);
        return message.isPresent()
                ? convert(annotation, annotationName, message.get(), referenceStream)
                : convert(annotationName, referenceStream);
    }

    private CustomTSDecorator getCustomTSDecorator(Annotation annotation) {
        return annotation.annotationType().getAnnotation(CustomTSDecorator.class);
    }

    private String getAnnotationName(Annotation annotation, CustomTSDecorator customTSDecorator) {
        return customTSDecorator.typeScriptDecorator().isEmpty()
                ? annotation.annotationType().getSimpleName()
                : customTSDecorator.typeScriptDecorator();
    }

    private Stream<TsExpression> getReferences(Annotation annotation, CustomTSDecorator customTSDecorator) {
        try {
            DecoratorParameterListExtractor decoratorParameterListExtractor = customTSDecorator.decoratorParameterListExtractor()
                                                                                               .newInstance();
            return decoratorParameterListExtractor.extract(annotation).stream()
                                                  .map(objectToTSLiteralConverter::convert);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TSParameterExtractorInstantiationException(e);
        }
    }

    private Optional<String> extractMessage(Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                     .filter(method -> method.getName().equals(MESSAGE_METHOD))
                     .findFirst()
                     .map(method -> invoke(annotation, method).toString());
    }

    private Stream<TsDecorator> convert(Annotation annotation,
                                        String annotationName,
                                        String message,
                                        Stream<TsExpression> referenceStream) {
        return Stream.of(
                new TsDecorator(new TsIdentifierReference("@" + annotationName),
                                Stream.concat(referenceStream,
                                              internationalization(annotation, annotationName, message))
                                      .collect(Collectors.toList())));
    }

    private Stream<TsDecorator> convert(String annotationName, Stream<TsExpression> referenceStream) {
        return Stream.of(
                new TsDecorator(new TsIdentifierReference("@" + annotationName),
                                referenceStream.collect(Collectors.toList())));
    }

    private Stream<TsExpression> internationalization(Annotation annotation, String annotationName, String message) {
        String validationsReference = validationMessageReferenceResolver.getMessageReference(() -> message,
                                                                                             annotationName);
        return Stream.of(new TsObjectLiteral(
                new TsPropertyDefinition(MESSAGE_METHOD,
                                         new TsCallExpression(new TsIdentifierReference(validationsReference)))));
    }

    private Object invoke(Annotation annotation, Method method, Object... args) {
        try {
            return method.invoke(annotation, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CanNotResolveMethod(annotation, method);
        }
    }
}
