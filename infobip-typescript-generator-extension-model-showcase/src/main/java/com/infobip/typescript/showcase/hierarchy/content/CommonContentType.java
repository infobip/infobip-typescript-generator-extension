package com.infobip.typescript.showcase.hierarchy.content;

import com.infobip.jackson.TypeProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonContentType implements TypeProvider, ContentType {
    TEXT(TextContent.class);

    private final Class<? extends CommonContent> type;
}
