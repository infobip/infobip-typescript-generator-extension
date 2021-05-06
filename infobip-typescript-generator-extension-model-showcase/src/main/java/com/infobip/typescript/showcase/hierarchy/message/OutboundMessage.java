package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.jackson.JsonTypeResolveWith;

@JsonTypeResolveWith(OutboundMessageJsonTypeResolver.class)
interface OutboundMessage extends Message {

    @Override
    default Direction getDirection() {
        return Direction.OUTBOUND;
    }
}
