package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.typescript.showcase.hierarchy.message.content.CommonContent;

record OutboundSmsMessage(

        CommonContent content

) implements OutboundMessage {

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
}
