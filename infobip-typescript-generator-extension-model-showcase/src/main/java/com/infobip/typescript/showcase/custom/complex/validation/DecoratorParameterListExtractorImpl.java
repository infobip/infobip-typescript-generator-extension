package com.infobip.typescript.showcase.custom.complex.validation;

import com.infobip.typescript.DecoratorParameterListExtractor;

import java.util.Collections;
import java.util.List;

public class DecoratorParameterListExtractorImpl implements DecoratorParameterListExtractor<ComplexCustomValidation> {

    @Override
    public List<Object> extract(ComplexCustomValidation annotation) {
        return Collections.singletonList(annotation.length());
    }
}
