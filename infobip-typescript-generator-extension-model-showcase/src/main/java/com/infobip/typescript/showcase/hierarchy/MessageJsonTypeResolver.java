package com.infobip.typescript.showcase.hierarchy;

import com.infobip.jackson.SimpleJsonTypeResolver;

class MessageJsonTypeResolver extends SimpleJsonTypeResolver<Direction> {

    public MessageJsonTypeResolver() {
        super(Direction.class, "direction");
    }
}
