package com.infobip.typescript.validation.exception;

public class TSParameterExtractorInstantiationException extends RuntimeException{

    public TSParameterExtractorInstantiationException(Throwable cause) {
        super("Can not extract typescript parameters", cause);
    }
}
