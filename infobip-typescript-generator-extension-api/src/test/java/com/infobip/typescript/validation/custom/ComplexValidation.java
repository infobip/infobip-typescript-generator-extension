package com.infobip.typescript.validation.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.infobip.typescript.CustomTypeScriptDecorator;
import com.infobip.typescript.validation.custom.validators.ComplexTestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

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
