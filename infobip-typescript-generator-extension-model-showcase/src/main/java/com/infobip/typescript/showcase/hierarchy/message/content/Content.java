package com.infobip.typescript.showcase.hierarchy.message.content;

public interface Content<T extends ContentType> {

    T getType();
}
