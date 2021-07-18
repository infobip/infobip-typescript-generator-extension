package com.infobip.typescript.showcase.custom.validation;

import com.infobip.validation.SimpleConstraintValidator;

import java.util.Optional;

public class SimpleCustomValidator implements SimpleConstraintValidator<SimpleCustomValidation, String> {

    private String field;
    private Integer number;

    @Override
    public void initialize(SimpleCustomValidation constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.number = constraintAnnotation.number();
    }

    @Override
    public boolean isValid(String value) {
        return Optional.ofNullable(value)
                       .map(val -> val.isEmpty())
                       .orElse(false);
    }
}
