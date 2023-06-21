package com.infobip.typescript.showcase.hierarchy.message;

enum Channel {
    SMS(InboundSmsMessage.class, OutboundSmsMessage.class);

    private final Class<? extends InboundMessage> inboundMessageType;
    private final Class<? extends OutboundMessage> outboundMessageType;

    Channel(Class<? extends InboundMessage> inboundMessageType,
            Class<? extends OutboundMessage> outboundMessageType) {
        this.inboundMessageType = inboundMessageType;
        this.outboundMessageType = outboundMessageType;
    }

    public Class<? extends InboundMessage> getInboundMessageType() {
        return inboundMessageType;
    }

    public Class<? extends OutboundMessage> getOutboundMessageType() {
        return outboundMessageType;
    }
}
