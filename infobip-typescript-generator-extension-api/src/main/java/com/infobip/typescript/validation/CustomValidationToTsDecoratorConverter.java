package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.TsDecorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.stream.Stream;

public class CustomValidationToTsDecoratorConverter extends BeanValidationToTsDecoratorConverter<Annotation> {

    CustomValidationToTsDecoratorConverter(ValidationMessageReferenceResolver resolver) {
        super(resolver);
    }

    @Override
    Stream<TsDecorator> convert(Field field, Annotation annotation) {
        return null;
    }

    @Override
    String extractMessage(Annotation annotation) {
        return null;
    }
}
