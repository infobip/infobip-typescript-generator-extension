package com.infobip.typescript;

import java.lang.annotation.Annotation;
import java.util.List;

public interface DecoratorParameterListExtractor<T extends Annotation> {

    List<DecoratorParameter> extract(T annotation);
}
