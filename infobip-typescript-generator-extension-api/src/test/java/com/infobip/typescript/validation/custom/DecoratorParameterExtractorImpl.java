package com.infobip.typescript.validation.custom;

import com.infobip.typescript.DecoratorParameterListExtractor;

import java.util.Arrays;
import java.util.List;

public class DecoratorParameterExtractorImpl implements DecoratorParameterListExtractor<ComplexValidation> {

    @Override
    public List<Object> extract(ComplexValidation annotation) {
        return Arrays.asList(annotation.length());
    }
}
