package com.infobip.typescript.showcase.custom.validation;

import com.infobip.typescript.DecoratorParameterListExtractor;

import java.util.List;

public class DecoratorParameterListExtractorImpl implements DecoratorParameterListExtractor<SimpleCustomValidation> {

    @Override
    public List<Object> extract(SimpleCustomValidation annotation) {
        return null;
    }
}
