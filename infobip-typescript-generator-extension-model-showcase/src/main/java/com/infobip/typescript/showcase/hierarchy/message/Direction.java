package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.jackson.TypeProvider;

enum Direction implements TypeProvider<Message> {
    INBOUND(InboundMessage.class),
    OUTBOUND(OutboundMessage.class);

    private final Class<? extends Message> type;

    Direction(Class<? extends Message> type) {
        this.type = type;
    }

    @Override
    public Class<? extends Message> getType() {
        return type;
    }
}
