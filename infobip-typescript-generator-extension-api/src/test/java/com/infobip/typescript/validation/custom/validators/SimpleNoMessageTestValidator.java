package com.infobip.typescript.validation.custom.validators;

import com.infobip.typescript.validation.custom.SimpleNoMessageValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SimpleNoMessageTestValidator implements ConstraintValidator<SimpleNoMessageValidation, String>{

    @Override
    public void initialize(SimpleNoMessageValidation constraintAnnotation) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }
}
