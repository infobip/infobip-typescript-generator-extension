package com.infobip.typescript.validation.custom;

import com.infobip.typescript.CustomTypeScriptDecorator;
import com.infobip.typescript.validation.custom.validators.ComplexTestValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@CustomTypeScriptDecorator(decoratorParameterListExtractor = DecoratorParameterExtractorImpl.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ComplexTestValidator.class)
public @interface ComplexValidation {

    String message() default "must be valid element";

    int length();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
