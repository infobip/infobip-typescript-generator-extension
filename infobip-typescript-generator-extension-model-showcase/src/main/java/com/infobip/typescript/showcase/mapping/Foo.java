package com.infobip.typescript.showcase.mapping;

import java.time.*;

public record Foo(

        Instant instant,

        LocalDateTime localDateTime,

        ZonedDateTime zonedDateTime,

        Duration duration

) {


}
