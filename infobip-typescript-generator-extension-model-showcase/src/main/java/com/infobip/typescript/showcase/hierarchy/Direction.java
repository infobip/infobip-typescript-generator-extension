package com.infobip.typescript.showcase.hierarchy;

import com.infobip.jackson.TypeProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum Direction implements TypeProvider {
    INBOUND(InboundMessage.class),
    OUTBOUND(OutboundMessage.class);

    private final Class<? extends Message> type;
}
