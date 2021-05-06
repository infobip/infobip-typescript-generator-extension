package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.jackson.CompositeJsonTypeResolver;

class OutboundMessageJsonTypeResolver extends CompositeJsonTypeResolver<Channel> {

    public OutboundMessageJsonTypeResolver() {
        super(Channel.class, "channel", Channel::getOutboundMessageType);
    }
}
