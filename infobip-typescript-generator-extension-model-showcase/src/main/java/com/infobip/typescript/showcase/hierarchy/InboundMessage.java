package com.infobip.typescript.showcase.hierarchy;

import com.infobip.jackson.JsonTypeResolveWith;

@JsonTypeResolveWith(InboundMessageJsonTypeResolver.class)
interface InboundMessage extends Message {

    @Override
    default Direction getDirection() {
        return Direction.INBOUND;
    }
}
