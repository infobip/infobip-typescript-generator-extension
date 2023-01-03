package com.infobip.typescript.validation.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.infobip.typescript.CustomTypeScriptDecorator;
import com.infobip.typescript.validation.custom.validators.CombinedTestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotEmpty;

@CustomTypeScriptDecorator(typeScriptDecorator = "SimpleValidation")
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@NotEmpty
@Constraint(validatedBy = CombinedTestValidator.class)
public @interface CombinedValidation {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
