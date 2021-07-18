package com.infobip.typescript.custom.validation;

import java.lang.annotation.Annotation;
import java.util.List;

public class CustomValidation {

    private final Class<? extends Annotation> annotation;
    private final List<Annotation> additionalAnnotations;

    public CustomValidation(Class<? extends Annotation> annotation,
                            List<Annotation> additionalAnnotations) {
        this.annotation = annotation;
        this.additionalAnnotations = additionalAnnotations;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    public List<Annotation> getAdditionalAnnotations() {
        return additionalAnnotations;
    }
}
