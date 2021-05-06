package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import java.util.stream.Stream;

abstract class BeanValidationToTsDecoratorConverter<A extends Annotation> {

    protected final ValidationMessageReferenceResolver validationMessageReferenceResolver;

    protected BeanValidationToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        this.validationMessageReferenceResolver = validationMessageReferenceResolver;
    }

    abstract Stream<TsDecorator> convert(Field field, A annotation);

    abstract String extractMessage(A annotation);

    TsObjectLiteral internationalization(Supplier<String> messageProvider, String identifier) {
        String validationsReference = validationMessageReferenceResolver.getMessageReference(messageProvider, identifier);
        return new TsObjectLiteral(
                new TsPropertyDefinition("message",
                                         new TsIdentifierReference(validationsReference)));
    }
}
