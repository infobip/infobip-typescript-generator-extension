package com.infobip.typescript.validation.custom.validators;

import com.infobip.typescript.validation.custom.SimpleNoMessageValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SimpleNoMessageTestValidator implements ConstraintValidator<SimpleNoMessageValidation, String>{

    @Override
    public void initialize(SimpleNoMessageValidation constraintAnnotation) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }
}
