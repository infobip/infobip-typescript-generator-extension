package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.typescript.showcase.hierarchy.message.content.CommonContent;

record InboundSmsMessage(

        CommonContent content

) implements InboundMessage {

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
}
