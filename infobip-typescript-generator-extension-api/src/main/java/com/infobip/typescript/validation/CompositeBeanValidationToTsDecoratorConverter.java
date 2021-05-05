package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.emitter.TsDecorator;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CompositeBeanValidationToTsDecoratorConverter extends BeanValidationToTsDecoratorConverter<Annotation> {

    private final Map<Class<? extends Annotation>, BeanValidationToTsDecoratorConverter<?>> annotationToHandler;

    CompositeBeanValidationToTsDecoratorConverter(ValidationMessageReferenceResolver resolver) {
        super(resolver);
        this.annotationToHandler =
                Stream.of(entry(Valid.class, new ValidToTsDecoratorConverter(resolver)),
                          entry(NotNull.class, new SimpleTsDecoratorConverter<>(resolver, "IsDefined", NotNull::message)),
                          entry(NotBlank.class, new SimpleTsDecoratorConverter<>(resolver, "IsNotEmpty", NotBlank::message)),
                          entry(NotEmpty.class, new SimpleTsDecoratorConverter<>(resolver, "IsNotEmpty", NotEmpty::message)),
                          entry(Size.class, new SizeToTsDecoratorConverter(resolver)),
                          entry(Min.class, new MinToTsDecoratorConverter(resolver)),
                          entry(Max.class, new MaxToTsDecoratorConverter(resolver)))
                      .collect(Collectors.toMap(MapEntry::getKey, MapEntry::getValue));
    }

    @Override
    public Stream<TsDecorator> convert(Field field, Annotation annotation) {

        BeanValidationToTsDecoratorConverter<Annotation> converter = getConverter(annotation);

        if (Objects.isNull(converter)) {
            return Stream.empty();
        }

        return converter.convert(field, annotation);
    }

    @Override
    public String extractMessage(Annotation annotation) {
        BeanValidationToTsDecoratorConverter<Annotation> converter = getConverter(annotation);

        if (Objects.isNull(converter)) {
            return null;
        }

        return converter.extractMessage(annotation);
    }

    private <T extends Annotation> MapEntry<Class<T>, BeanValidationToTsDecoratorConverter<T>> entry(Class<T> type,
                                                                                                     BeanValidationToTsDecoratorConverter<T> handler) {
        return new MapEntry<>(type, handler);
    }

    @SuppressWarnings("unchecked")
    private BeanValidationToTsDecoratorConverter<Annotation> getConverter(Annotation annotation) {
        return (BeanValidationToTsDecoratorConverter<Annotation>) annotationToHandler.get(
                annotation.annotationType());
    }
}
