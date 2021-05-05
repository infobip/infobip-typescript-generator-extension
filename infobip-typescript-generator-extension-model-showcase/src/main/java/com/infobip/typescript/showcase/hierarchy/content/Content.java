package com.infobip.typescript.showcase.hierarchy.content;

public interface Content<T extends ContentType> {

    T getType();
}
