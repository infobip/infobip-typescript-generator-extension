package com.infobip.typescript.showcase.custom.complex.validation;

import java.util.Collections;
import java.util.List;

import com.infobip.typescript.DecoratorParameter;
import com.infobip.typescript.DecoratorParameterListExtractor;

public class DecoratorParameterListExtractorImpl implements DecoratorParameterListExtractor<ComplexCustomValidation> {

    @Override
    public List<DecoratorParameter> extract(ComplexCustomValidation annotation) {
        return Collections.singletonList(new DecoratorParameter("length", annotation.length()));
    }

}
