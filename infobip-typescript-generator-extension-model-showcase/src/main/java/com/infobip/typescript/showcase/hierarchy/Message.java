package com.infobip.typescript.showcase.hierarchy;

import com.infobip.jackson.JsonTypeResolveWith;

@JsonTypeResolveWith(MessageJsonTypeResolver.class)
interface Message {

    Direction getDirection();

    Channel getChannel();
}
