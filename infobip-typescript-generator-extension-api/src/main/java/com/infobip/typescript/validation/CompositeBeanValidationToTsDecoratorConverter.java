package com.infobip.typescript.validation;

import com.infobip.validation.SimpleConstraintValidator;
import cz.habarta.typescript.generator.emitter.TsDecorator;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CompositeBeanValidationToTsDecoratorConverter extends BeanValidationToTsDecoratorConverter<Annotation> {

    private final Map<Class<? extends Annotation>, BeanValidationToTsDecoratorConverter<?>> annotationToHandler;
    private final BeanValidationToTsDecoratorConverter<Annotation> customAnotationHandler;

    CompositeBeanValidationToTsDecoratorConverter(ValidationMessageReferenceResolver resolver) {
        super(resolver);
        this.annotationToHandler =
                Stream.of(entry(Valid.class, new ValidToTsDecoratorConverter(resolver)),
                          entry(NotNull.class,
                                new SimpleTsDecoratorConverter<>(resolver, "IsDefined", NotNull::message)),
                          entry(NotBlank.class,
                                new SimpleTsDecoratorConverter<>(resolver, "IsNotEmpty", NotBlank::message)),
                          entry(NotEmpty.class,
                                new SimpleTsDecoratorConverter<>(resolver, "IsNotEmpty", NotEmpty::message)),
                          entry(Size.class, new SizeToTsDecoratorConverter(resolver)),
                          entry(Min.class, new MinToTsDecoratorConverter(resolver)),
                          entry(Max.class, new MaxToTsDecoratorConverter(resolver)))
                      .collect(Collectors.toMap(MapEntry::getKey, MapEntry::getValue));
        this.customAnotationHandler = new CustomValidationToTsDecoratorConverter(resolver);
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
        BeanValidationToTsDecoratorConverter<Annotation> converter = (BeanValidationToTsDecoratorConverter<Annotation>) annotationToHandler
                .get(
                        annotation.annotationType());

        if (Objects.isNull(converter) && isCustom(annotation)) {
            return this.customAnotationHandler;
        }

        return converter;
    }

    private boolean isCustom(Annotation annotation) {
        boolean isSimpleConstraintValidator = Arrays.stream(
                annotation.annotationType().getAnnotation(Constraint.class).validatedBy())
                                                    .filter(type -> SimpleConstraintValidator.class.equals(type))
                                                    .findFirst().isPresent();
        boolean isField = Arrays.stream(annotation.annotationType().getAnnotation(Target.class).value())
                                .filter(elementType -> ElementType.FIELD.equals(elementType))
                                .findFirst()
                                .isPresent();

        return isSimpleConstraintValidator && isField;
    }
}
