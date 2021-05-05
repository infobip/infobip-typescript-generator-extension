package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.*;

import javax.validation.constraints.Max;
import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MaxToTsDecoratorConverter extends BeanValidationToTsDecoratorConverter<Max> {

    public MaxToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        super(validationMessageReferenceResolver);
    }

    @Override
    public Stream<TsDecorator> convert(Field field, Max annotation) {
        return Stream.of(
                new TsDecorator(new TsIdentifierReference("@Max"),
                                Stream.of(new TsIdentifierReference(String.valueOf(annotation.value())),
                                          internationalization(annotation)).collect(Collectors.toList())));
    }

    private TsObjectLiteral internationalization(Max annotation) {
        String validationsReference = validationMessageReferenceResolver.getMessageReference(annotation::message,
                                                                                             "Max");
        return new TsObjectLiteral(
                new TsPropertyDefinition("message",
                                         new TsCallExpression(new TsIdentifierReference(validationsReference),
                                                              new TsIdentifierReference(Long.toString(
                                                                      annotation.value())))));
    }

    @Override
    public String extractMessage(Max annotation) {
        return annotation.message();
    }
}
