package com.infobip.typescript.validation;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.habarta.typescript.generator.emitter.*;
import jakarta.validation.constraints.Min;

class MinToTsDecoratorConverter extends BeanValidationToTsDecoratorConverter<Min> {

    public MinToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        super(validationMessageReferenceResolver);
    }

    @Override
    public Stream<TsDecorator> convert(Field field, Min annotation) {
        return Stream.of(
                new TsDecorator(new TsIdentifierReference("@Min"),
                                Stream.of(new TsIdentifierReference(String.valueOf(annotation.value())),
                                                          internationalization(annotation)).collect(Collectors.toList())));
    }

    private TsObjectLiteral internationalization(Min annotation) {
        String validationsReference = validationMessageReferenceResolver.getMessageReference(annotation::message, "Min");
        return new TsObjectLiteral(
                new TsPropertyDefinition("message",
                                         new TsCallExpression(new TsIdentifierReference(validationsReference),
                                                              new TsIdentifierReference(Long.toString(
                                                                      annotation.value())))));
    }

    @Override
    public String extractMessage(Min annotation) {
        return annotation.message();
    }
}
