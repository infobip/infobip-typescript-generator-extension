package com.infobip.typescript.validation.custom;

import com.infobip.typescript.CustomTSDecorator;
import com.infobip.typescript.validation.custom.validators.NonExistingTSDecoratorTestValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@CustomTSDecorator( type = NonExistingTSDecorator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NonExistingTSDecoratorTestValidator.class)
public @interface NonExistingTSDecorator {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
