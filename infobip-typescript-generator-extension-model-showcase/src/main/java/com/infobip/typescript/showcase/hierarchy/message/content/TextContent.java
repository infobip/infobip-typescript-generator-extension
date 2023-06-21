package com.infobip.typescript.showcase.hierarchy.message.content;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record TextContent(

        @NotNull
        @NotEmpty
        String text

) implements CommonContent {

    @Override
    public CommonContentType getType() {
        return CommonContentType.TEXT;
    }
}
