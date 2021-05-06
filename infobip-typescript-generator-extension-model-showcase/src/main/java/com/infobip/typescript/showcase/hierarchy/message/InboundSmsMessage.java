package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.typescript.showcase.hierarchy.message.content.CommonContent;
import lombok.Value;

@Value
class InboundSmsMessage implements InboundMessage {

    private final CommonContent content;

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
}
