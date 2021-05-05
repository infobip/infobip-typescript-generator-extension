package com.infobip.typescript.showcase.hierarchy.message.content;

import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
class TextContent implements CommonContent {

    @NotNull
    @NotEmpty
    private final String text;

    @Override
    public CommonContentType getType() {
        return CommonContentType.TEXT;
    }
}
