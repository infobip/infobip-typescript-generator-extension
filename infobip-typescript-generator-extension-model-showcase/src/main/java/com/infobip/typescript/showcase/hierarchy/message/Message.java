package com.infobip.typescript.showcase.hierarchy.message;

import com.infobip.jackson.JsonTypeResolveWith;

@JsonTypeResolveWith(MessageJsonTypeResolver.class)
interface Message {

    Direction getDirection();

    Channel getChannel();
}
