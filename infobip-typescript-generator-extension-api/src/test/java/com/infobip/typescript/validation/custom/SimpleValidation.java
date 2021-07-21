package com.infobip.typescript.validation.custom;

import com.infobip.typescript.CustomTSDecorator;
import com.infobip.typescript.validation.custom.validators.SimpleTestValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@CustomTSDecorator(type = SimpleValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SimpleTestValidator.class)
public @interface SimpleValidation {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
