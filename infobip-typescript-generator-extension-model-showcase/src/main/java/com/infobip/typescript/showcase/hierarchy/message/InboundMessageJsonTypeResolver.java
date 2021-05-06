package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.jackson.CompositeJsonTypeResolver;

class InboundMessageJsonTypeResolver extends CompositeJsonTypeResolver<Channel> {

    public InboundMessageJsonTypeResolver() {
        super(Channel.class, "channel", Channel::getInboundMessageType);
    }
}
