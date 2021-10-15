package com.infobip.typescript.validation.custom;

import java.util.Arrays;
import java.util.List;

import com.infobip.typescript.DecoratorParameter;
import com.infobip.typescript.DecoratorParameterListExtractor;

public class DecoratorParameterExtractorImpl implements DecoratorParameterListExtractor<ComplexValidation> {

    @Override
    public List<DecoratorParameter> extract(ComplexValidation annotation) {
        return Arrays.asList(new DecoratorParameter("length", annotation.length()));
    }
}
