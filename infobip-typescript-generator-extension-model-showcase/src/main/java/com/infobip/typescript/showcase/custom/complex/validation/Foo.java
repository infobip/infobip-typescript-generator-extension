package com.infobip.typescript.showcase.custom.complex.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record Foo(

        @ComplexCustomValidation(length = 100)
        @Size(min = 1, max = 2)
        @NotEmpty
        @NotNull
        @Valid
        String bar

) {

}
