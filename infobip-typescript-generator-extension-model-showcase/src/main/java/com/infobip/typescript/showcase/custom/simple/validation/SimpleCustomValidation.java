package com.infobip.typescript.showcase.custom.simple.validation;

import com.infobip.typescript.CustomTSDecorator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@CustomTSDecorator(type = SimpleCustomValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SimpleCustomValidator.class)
public @interface SimpleCustomValidation {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
