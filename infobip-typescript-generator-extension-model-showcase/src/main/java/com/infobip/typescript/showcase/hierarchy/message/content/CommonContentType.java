package com.infobip.typescript.showcase.hierarchy.message.content;

import com.infobip.jackson.TypeProvider;

public enum CommonContentType implements TypeProvider<CommonContent>, ContentType {
    TEXT(TextContent.class);

    private final Class<? extends CommonContent> type;

    CommonContentType(Class<? extends CommonContent> type) {
        this.type = type;
    }

    @Override
    public Class<? extends CommonContent> getType() {
        return type;
    }
}
