package com.infobip.typescript.validation.helpers;

import com.infobip.typescript.validation.custom.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class AnnotationHelper {

    public static List<Class<? extends Annotation>> getSupportedAnnotations() {
        return Arrays.asList(SimpleValidation.class, ComplexValidation.class, CombinedValidation.class,
                             SimpleNoMessageValidation.class, NonExistingTSDecorator.class);
    }
}
