package com.infobip.typescript.showcase.hierarchy.content;

import com.infobip.jackson.SimpleJsonTypeResolver;

public class CommonContentJsonTypeResolver extends SimpleJsonTypeResolver<CommonContentType> {

    public CommonContentJsonTypeResolver() {
        super(CommonContentType.class);
    }
}
