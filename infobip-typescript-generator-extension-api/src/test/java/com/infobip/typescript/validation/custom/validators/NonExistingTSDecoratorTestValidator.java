package com.infobip.typescript.validation.custom.validators;

import com.infobip.typescript.validation.custom.NonExistingTSDecorator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NonExistingTSDecoratorTestValidator implements ConstraintValidator<NonExistingTSDecorator, String> {

    @Override
    public void initialize(NonExistingTSDecorator constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }
}
