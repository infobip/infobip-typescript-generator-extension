package com.infobip.typescript.custom.validation;

import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomValidationData {

    private final Map<Class<? extends Annotation>, CustomValidationAnnotation> customValidationAnnotations;
    private final List<TSCustomDecorator> tsCustomDecorators;
    private final List<String> tsDecoratorsNames;

    public CustomValidationData(Map<Class<? extends Annotation>, CustomValidationAnnotation> customValidationAnnotations,
                                List<TSCustomDecorator> tsCustomDecorators) {
        this.customValidationAnnotations = customValidationAnnotations;
        this.tsCustomDecorators = tsCustomDecorators;
        this.tsDecoratorsNames = tsCustomDecorators.stream()
                                                   .map(TSCustomDecorator::getName)
                                                   .collect(Collectors.toList());
    }

    public Map<Class<? extends Annotation>, CustomValidationAnnotation> getCustomValidationAnnotations() {
        return customValidationAnnotations;
    }

    public List<TSCustomDecorator> getTsCustomDecorators() {
        return tsCustomDecorators;
    }

    public List<String> getTsDecoratorsNames() {
        return tsDecoratorsNames;
    }
}
