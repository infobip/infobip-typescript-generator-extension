package com.infobip.typescript.showcase.custom.complex.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComplexCustomValidator implements ConstraintValidator<ComplexCustomValidation, String> {

    private int length;

    @Override
    public void initialize(ComplexCustomValidation constraintAnnotation) {
        this.length = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value.length() > length;
    }
}
