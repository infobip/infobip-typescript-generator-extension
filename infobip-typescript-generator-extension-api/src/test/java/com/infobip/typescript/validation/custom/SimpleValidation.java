package com.infobip.typescript.validation.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.infobip.typescript.CustomTypeScriptDecorator;
import com.infobip.typescript.validation.custom.validators.SimpleTestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@CustomTypeScriptDecorator()
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SimpleTestValidator.class)
public @interface SimpleValidation {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
