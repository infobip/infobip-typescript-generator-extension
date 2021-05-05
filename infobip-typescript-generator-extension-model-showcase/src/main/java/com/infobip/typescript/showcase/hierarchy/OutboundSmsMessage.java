package com.infobip.typescript.showcase.hierarchy;

import com.infobip.typescript.showcase.hierarchy.content.CommonContent;
import lombok.Value;

import javax.validation.constraints.Size;

@Value
class OutboundSmsMessage implements OutboundMessage {

    private final CommonContent content;

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
}
