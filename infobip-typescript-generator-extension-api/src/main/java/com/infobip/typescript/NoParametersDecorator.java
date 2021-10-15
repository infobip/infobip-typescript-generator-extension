package com.infobip.typescript;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class NoParametersDecorator implements DecoratorParameterListExtractor<Annotation> {

    @Override
    public List<DecoratorParameter> extract(Annotation annotation) {
        return Collections.emptyList();
    }
}
