package com.infobip.typescript.validation;

import static com.infobip.typescript.validation.Localization.LOCALIZATION_METHOD;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.infobip.typescript.CustomTypeScriptDecorator;
import com.infobip.typescript.DecoratorParameter;
import com.infobip.typescript.DecoratorParameterListExtractor;
import com.infobip.typescript.validation.converter.ObjectToTSLiteralConverter;
import com.infobip.typescript.validation.exception.CanNotResolveMethod;
import com.infobip.typescript.validation.exception.TSParameterExtractorInstantiationException;
import cz.habarta.typescript.generator.emitter.TsCallExpression;
import cz.habarta.typescript.generator.emitter.TsDecorator;
import cz.habarta.typescript.generator.emitter.TsExpression;
import cz.habarta.typescript.generator.emitter.TsIdentifierReference;
import cz.habarta.typescript.generator.emitter.TsObjectLiteral;
import cz.habarta.typescript.generator.emitter.TsPropertyDefinition;
import cz.habarta.typescript.generator.emitter.TsStringLiteral;

class CustomAnnotationToTsDecoratorConverter {

    private static final String MESSAGE_METHOD = "message";

    private final ValidationMessageReferenceResolver validationMessageReferenceResolver;
    private final ObjectToTSLiteralConverter objectToTSLiteralConverter;

    CustomAnnotationToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        this.validationMessageReferenceResolver = validationMessageReferenceResolver;
        this.objectToTSLiteralConverter = new ObjectToTSLiteralConverter();
    }

    public Stream<TsDecorator> convert(Annotation annotation) {
        CustomTypeScriptDecorator customTypeScriptDecorator = getCustomTSDecorator(annotation);
        String annotationName = getAnnotationName(annotation, customTypeScriptDecorator);
        Optional<String> message = extractMessage(annotation);
        List<DecoratorParameter> parameters = getDecoratorParameters(annotation, customTypeScriptDecorator);
        Stream<TsExpression> referenceStream = getReferences(parameters);
        return message
            .map(msg -> convert(annotationName, msg, referenceStream, parameters))
            .orElseGet(() -> convert(annotationName, referenceStream));
    }

    private CustomTypeScriptDecorator getCustomTSDecorator(Annotation annotation) {
        return annotation.annotationType().getAnnotation(CustomTypeScriptDecorator.class);
    }

    private String getAnnotationName(Annotation annotation, CustomTypeScriptDecorator customTypeScriptDecorator) {
        return customTypeScriptDecorator.typeScriptDecorator().isEmpty()
            ? annotation.annotationType().getSimpleName()
            : customTypeScriptDecorator.typeScriptDecorator();
    }

    private List<DecoratorParameter> getDecoratorParameters(Annotation annotation, CustomTypeScriptDecorator customTypeScriptDecorator) {
        try {
            @SuppressWarnings("unchecked")
            DecoratorParameterListExtractor<Annotation> extractor =(DecoratorParameterListExtractor<Annotation>) customTypeScriptDecorator.decoratorParameterListExtractor()
                                                                                                                                          .newInstance();
            return extractor.extract(annotation);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TSParameterExtractorInstantiationException(e);
        }
    }

    private Stream<TsExpression> getReferences(List<DecoratorParameter> decoratorParameters) {
        return decoratorParameters.stream()
                                  .map(parameter -> objectToTSLiteralConverter.convert(parameter.getParameterValue()));
    }

    private Optional<String> extractMessage(Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                     .filter(method -> method.getName().equals(MESSAGE_METHOD))
                     .findFirst()
                     .map(method -> invoke(annotation, method).toString());
    }

    private Stream<TsDecorator> convert(String annotationName,
                                        String message,
                                        Stream<TsExpression> referenceStream,
                                        List<DecoratorParameter> decoratorParameters) {
        return Stream.of(
            new TsDecorator(new TsIdentifierReference("@" + annotationName),
                            Stream.concat(referenceStream,
                                          internationalization(message, decoratorParameters))
                                  .collect(Collectors.toList())));
    }

    private Stream<TsDecorator> convert(String annotationName, Stream<TsExpression> referenceStream) {
        return Stream.of(
            new TsDecorator(new TsIdentifierReference("@" + annotationName),
                            referenceStream.collect(Collectors.toList())));
    }

    private Stream<TsExpression> internationalization(String message, List<DecoratorParameter> parameters) {
        return Stream.of(new TsObjectLiteral(createTsPropertyDefinition(message, parameters)));
    }

    private TsPropertyDefinition createTsPropertyDefinition(String message, List<DecoratorParameter> parameters) {
        Optional<TsObjectLiteral> objectLiteral = createObjectLiteralFromParameters(message, parameters);
        return objectLiteral.isPresent()
            ? create(message, objectLiteral.get())
            : create(message);

    }

    private TsPropertyDefinition create(String message, TsObjectLiteral objectLiteral) {
        return new TsPropertyDefinition(MESSAGE_METHOD,
                                        new TsCallExpression(new TsIdentifierReference(LOCALIZATION_METHOD),
                                                             new TsStringLiteral(message),
                                                             objectLiteral));
    }

    private TsPropertyDefinition create(String message) {
        Optional<String> customMessageReference = validationMessageReferenceResolver.getCustomMessageReferenceIfExist(() -> message);
        return customMessageReference.map(s -> new TsPropertyDefinition(MESSAGE_METHOD, new TsCallExpression(new TsIdentifierReference(s))))
                                     .orElseGet(() -> new TsPropertyDefinition(MESSAGE_METHOD, new TsStringLiteral(message)));

    }

    private Optional<TsObjectLiteral> createObjectLiteralFromParameters(String message, List<DecoratorParameter> parameters) {
        List<TsPropertyDefinition> propertyDefinitions = parameters.stream()
                                                                   .filter(parameter -> message.contains("{" + parameter.getParameterName() + "}"))
                                                                   .map(parameter -> new TsPropertyDefinition(parameter.getParameterName(),
                                                                                                              objectToTSLiteralConverter.convert(parameter.getParameterValue())))
                                                                   .collect(Collectors.toList());
        return !propertyDefinitions.isEmpty()
            ? Optional.of(new TsObjectLiteral(propertyDefinitions))
            : Optional.empty();

    }

    private Object invoke(Annotation annotation, Method method, Object... args) {
        try {
            return method.invoke(annotation, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CanNotResolveMethod(annotation, method);
        }
    }

}


