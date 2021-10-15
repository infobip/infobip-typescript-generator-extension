package com.infobip.typescript.showcase.custom.complex.validation;

import com.infobip.typescript.CustomTypeScriptDecorator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@CustomTypeScriptDecorator(
    typeScriptDecorator = "ComplexValidator",
    decoratorParameterListExtractor = DecoratorParameterListExtractorImpl.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ComplexCustomValidator.class)
public @interface ComplexCustomValidation {

    String message() default "must be valid element";

    int length();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
