package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.TsDecorator;
import cz.habarta.typescript.generator.emitter.TsIdentifierReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SimpleTsDecoratorConverter<A extends Annotation> extends BeanValidationToTsDecoratorConverter<A> {

    private final String identifier;
    private final Function<A, String> messageExtractor;

    public SimpleTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver,
                                      String identifier, Function<A, String> messageExtractor) {
        super(validationMessageReferenceResolver);
        this.identifier = identifier;
        this.messageExtractor = messageExtractor;
    }

    @Override
    public Stream<TsDecorator> convert(Field field, A annotation) {
        return Stream.of(new TsDecorator(new TsIdentifierReference("@" + this.identifier),
                                         Stream.of(internationalization(() -> messageExtractor.apply(annotation),
                                                                      identifier)).collect(Collectors.toList())));
    }

    @Override
    public String extractMessage(A annotation) {
        return messageExtractor.apply(annotation);
    }
}
