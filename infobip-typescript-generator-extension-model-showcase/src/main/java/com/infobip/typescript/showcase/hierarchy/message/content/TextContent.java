package com.infobip.typescript.showcase.hierarchy.message.content;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

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
