package com.infobip.typescript.validation.custom;

import com.infobip.typescript.CustomTSDecorator;
import com.infobip.typescript.validation.custom.validators.CombinedTestValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotEmpty;
import java.lang.annotation.*;

@CustomTSDecorator(typeScriptDecorator = "SimpleValidation", type = CombinedValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@NotEmpty
@Constraint(validatedBy = CombinedTestValidator.class)
public @interface CombinedValidation {

    String message() default "must be valid element";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
