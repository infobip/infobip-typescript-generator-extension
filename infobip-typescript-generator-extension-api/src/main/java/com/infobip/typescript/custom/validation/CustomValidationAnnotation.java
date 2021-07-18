package com.infobip.typescript.custom.validation;

import java.lang.annotation.Annotation;
import java.util.List;

public class CustomValidationAnnotation {

    private final Class<? extends Annotation> annotation;
    private final List<String> methods;
    private final List<Annotation> additionalAnnotations;

    public CustomValidationAnnotation(Class<? extends Annotation> annotation,
                                      List<String> methods,
                                      List<Annotation> additionalAnnotations) {
        this.annotation = annotation;
        this.methods = methods;
        this.additionalAnnotations = additionalAnnotations;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    public List<Annotation> getAdditionalAnnotations() {
        return additionalAnnotations;
    }

    public List<String> getMethods() {
        return methods;
    }
}
