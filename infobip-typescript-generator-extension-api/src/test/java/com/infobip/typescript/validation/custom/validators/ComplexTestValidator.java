package com.infobip.typescript.validation.custom.validators;

import com.infobip.typescript.validation.custom.ComplexValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ComplexTestValidator implements ConstraintValidator<ComplexValidation, String> {

    private int length;

    @Override
    public void initialize(ComplexValidation constraintAnnotation) {
        this.length = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value.length() > length;
    }
}
