package com.infobip.typescript.showcase.custom.complex.validation;

import com.infobip.typescript.CustomTSDecorator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@CustomTSDecorator(
        typeScriptDecorator = "ComplexValidator",
        decoratorParameterListExtractor = DecoratorParameterListExtractorImpl.class,
        type = ComplexCustomValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ComplexCustomValidator.class)
public @interface ComplexCustomValidation {

    String message() default "must be valid element";

    int length();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
