package com.infobip.typescript;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface CustomTSDecorator {

    String typeScriptDecorator() default "";

    Class<? extends DecoratorParameterListExtractor> decoratorParameterListExtractor();

    Class<? extends Annotation> type();
}
