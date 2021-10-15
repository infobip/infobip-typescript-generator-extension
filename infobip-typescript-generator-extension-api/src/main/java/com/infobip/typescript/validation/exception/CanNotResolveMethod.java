package com.infobip.typescript.validation.exception;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class CanNotResolveMethod extends RuntimeException {

    public CanNotResolveMethod(Annotation annotation, Method method) {
        super(String.format("For given annotation: {}, Method {} method can not be resolved",
                            annotation.getClass().getName(), method.getName()));
    }
}
