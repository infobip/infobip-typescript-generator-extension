package com.infobip.typescript.showcase.simple;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record Foo(

        @Size(min = 1, max = 2)
        @NotEmpty
        @NotNull
        @Valid
        String bar

) {

}
