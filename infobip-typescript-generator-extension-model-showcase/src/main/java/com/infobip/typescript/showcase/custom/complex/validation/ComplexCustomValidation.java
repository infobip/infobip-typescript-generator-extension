package com.infobip.typescript.showcase.custom.complex.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.infobip.typescript.CustomTypeScriptDecorator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@CustomTypeScriptDecorator(
    typeScriptDecorator = "ComplexValidator",
    decoratorParameterListExtractor = DecoratorParameterListExtractorImpl.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ComplexCustomValidator.class)
public @interface ComplexCustomValidation {

    String message() default "must be valid element";

    int length();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
