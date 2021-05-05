package com.infobip.typescript;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@Documented
public @interface GenerateTypescript {

    Class<? extends TypeScriptFileGenerator> generator();
}
