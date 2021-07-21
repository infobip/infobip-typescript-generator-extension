package com.infobip.typescript.showcase.custom.simple.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SimpleCustomValidator implements ConstraintValidator<SimpleCustomValidation, String> {

    @Override
    public void initialize(SimpleCustomValidation constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value.length() > 100;
    }
}
