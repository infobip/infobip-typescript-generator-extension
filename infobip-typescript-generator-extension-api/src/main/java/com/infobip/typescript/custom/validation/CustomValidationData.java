package com.infobip.typescript.custom.validation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class CustomValidationData {

    private final Map<Class<? extends Annotation>, CustomValidationAnnotation> customValidationAnnotations;
    private final List<String> validatorsNames;

    public CustomValidationData(Map<Class<? extends Annotation>, CustomValidationAnnotation> customValidationAnnotations,
                                List<String> validatorsNames) {
        this.customValidationAnnotations = customValidationAnnotations;
        this.validatorsNames = validatorsNames;
    }

    public Map<Class<? extends Annotation>, CustomValidationAnnotation> getCustomValidationAnnotations() {
        return customValidationAnnotations;
    }

    public List<String> getValidatorsNames() {
        return validatorsNames;
    }
}
