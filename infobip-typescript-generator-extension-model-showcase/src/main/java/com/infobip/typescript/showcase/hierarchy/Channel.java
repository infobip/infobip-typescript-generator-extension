package com.infobip.typescript.showcase.hierarchy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum Channel {
    SMS(InboundSmsMessage.class, OutboundSmsMessage.class);

    private final Class<? extends InboundMessage> inboundMessageType;
    private final Class<? extends OutboundMessage> outboundMessageType;
}
