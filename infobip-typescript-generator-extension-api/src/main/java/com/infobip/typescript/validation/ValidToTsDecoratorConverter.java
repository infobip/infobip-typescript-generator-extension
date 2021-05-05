package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.TsDecorator;
import cz.habarta.typescript.generator.emitter.TsIdentifierReference;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.stream.Stream;

class ValidToTsDecoratorConverter extends BeanValidationToTsDecoratorConverter<Valid> {

    public ValidToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        super(validationMessageReferenceResolver);
    }

    @Override
    public Stream<TsDecorator> convert(Field field, Valid annotation) {
        return Stream.of(new TsDecorator(new TsIdentifierReference("@ValidateNested"), Collections.emptyList()));
    }

    @Override
    public String extractMessage(Valid annotation) {
        throw new IllegalStateException();
    }
}
