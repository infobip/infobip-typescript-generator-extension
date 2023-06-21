package com.infobip.typescript.showcase.custom.simple.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record Foo(

        @SimpleCustomValidation
        @Size(min = 1, max = 2)
        @NotEmpty
        @NotNull
        @Valid
        String bar

) {

}
