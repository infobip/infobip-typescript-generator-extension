package com.infobip.typescript.showcase.custom.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SimpleCustomValidator.class)
public @interface SimpleCustomValidation {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
