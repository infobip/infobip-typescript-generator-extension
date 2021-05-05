package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SizeToTsDecoratorConverter extends BeanValidationToTsDecoratorConverter<Size> {

    public SizeToTsDecoratorConverter(ValidationMessageReferenceResolver validationMessageReferenceResolver) {
        super(validationMessageReferenceResolver);
    }

    @Override
    public Stream<TsDecorator> convert(Field field, Size annotation) {
        String min = String.valueOf(annotation.min());
        if (field.getType().equals(String.class)) {
            return Stream.concat(max("MaxLength", annotation, annotation.max()),
                                 getMin(field, annotation, min));
        }

        if (Collection.class.isAssignableFrom(field.getType())) {
            return Stream.concat(max("ArrayMaxSize", annotation, annotation.max()),
                                 Stream.of(new TsDecorator(new TsIdentifierReference("@ArrayMinSize"),
                                                           Stream.of(new TsIdentifierReference(
                                                                             min),
                                                                     internationalization("ArrayMinSize",
                                                                                          annotation,
                                                                                          () -> min))
                                                                 .collect(Collectors.toList()))));
        }

        return Stream.empty();
    }

    @Override
    public String extractMessage(Size annotation) {
        return annotation.message();
    }

    private Stream<TsDecorator> max(String identifier, Size annotation, int max) {
        if (max == Integer.MAX_VALUE) {
            return Stream.empty();
        }

        String value = String.valueOf(max);
        return Stream.of(new TsDecorator(new TsIdentifierReference(identifier),
                                         Stream.of(new TsIdentifierReference(value),
                                                   internationalization(identifier,
                                                                        annotation,
                                                                        () -> value)).collect(Collectors.toList())));
    }

    private Stream<TsDecorator> getMin(Field field, Size annotation, String min) {

        if (Objects.nonNull(field.getAnnotation(NotBlank.class))) {
            return Stream.empty();
        }

        return Stream.of(new TsDecorator(new TsIdentifierReference("@MinLength"),
                                         Stream.of(new TsIdentifierReference(min),
                                                   internationalization("MinLength",
                                                                        annotation,
                                                                        () -> min)).collect(Collectors.toList())));
    }

    private TsObjectLiteral internationalization(String identifier, Size annotation, Supplier<String> valueSupplier) {
        String validationsReference = validationMessageReferenceResolver.getMessageReference(annotation::message,
                                                                                             identifier);
        return new TsObjectLiteral(
                new TsPropertyDefinition("message",
                                         new TsCallExpression(new TsIdentifierReference(validationsReference),
                                                              new TsIdentifierReference(valueSupplier.get()))));
    }
}
