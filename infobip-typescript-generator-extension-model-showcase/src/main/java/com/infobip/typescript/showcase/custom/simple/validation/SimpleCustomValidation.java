package com.infobip.typescript.showcase.custom.simple.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.infobip.typescript.CustomTypeScriptDecorator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@CustomTypeScriptDecorator()
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SimpleCustomValidator.class)
public @interface SimpleCustomValidation {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
