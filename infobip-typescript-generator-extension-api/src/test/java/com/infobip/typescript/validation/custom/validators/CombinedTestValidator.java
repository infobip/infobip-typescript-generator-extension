package com.infobip.typescript.validation.custom.validators;

import com.infobip.typescript.validation.custom.CombinedValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CombinedTestValidator implements ConstraintValidator<CombinedValidation, String> {

    @Override
    public void initialize(CombinedValidation constraintAnnotation) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }
}
