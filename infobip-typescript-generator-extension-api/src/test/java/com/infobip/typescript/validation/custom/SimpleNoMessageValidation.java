package com.infobip.typescript.validation.custom;

import com.infobip.typescript.CustomTSDecorator;
import com.infobip.typescript.validation.custom.validators.SimpleNoMessageTestValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@CustomTSDecorator(typeScriptDecorator = "SimpleValidation", type = SimpleNoMessageValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SimpleNoMessageTestValidator.class)
public @interface SimpleNoMessageValidation {

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}