package com.infobip.typescript.validation.exception;

public class UnsupportedTypeException extends RuntimeException{

    public UnsupportedTypeException(Class type) {
        super(String.format("Type %s is not supported", type.getSimpleName()));
    }
}
