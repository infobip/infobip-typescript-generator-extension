package com.infobip.typescript.validation.custom.validators;

import com.infobip.typescript.validation.custom.SimpleValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SimpleTestValidator implements ConstraintValidator<SimpleValidation, String> {

    @Override
    public void initialize(SimpleValidation constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }
}
