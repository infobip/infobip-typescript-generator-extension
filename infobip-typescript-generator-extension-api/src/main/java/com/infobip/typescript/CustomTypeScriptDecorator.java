package com.infobip.typescript;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface CustomTypeScriptDecorator {

    String typeScriptDecorator() default "";

    Class<? extends DecoratorParameterListExtractor> decoratorParameterListExtractor() default NoParametersDecorator.class;

}
