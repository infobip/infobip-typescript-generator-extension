package com.infobip.typescript.validation.exception;

import java.lang.annotation.Annotation;

public class TSValidatorDoesNotExist extends RuntimeException {

    public TSValidatorDoesNotExist(Annotation annotation) {
        super(String.format("For given annotation: %s, TypeScript decorator does not exists",
                            annotation.getClass().getSimpleName()));
    }
}
