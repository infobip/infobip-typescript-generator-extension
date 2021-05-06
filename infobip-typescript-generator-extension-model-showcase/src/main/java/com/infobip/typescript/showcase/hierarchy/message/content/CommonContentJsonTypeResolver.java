package com.infobip.typescript.showcase.hierarchy.message.content;

import com.infobip.jackson.SimpleJsonTypeResolver;

public class CommonContentJsonTypeResolver extends SimpleJsonTypeResolver<CommonContentType> {

    public CommonContentJsonTypeResolver() {
        super(CommonContentType.class);
    }
}
