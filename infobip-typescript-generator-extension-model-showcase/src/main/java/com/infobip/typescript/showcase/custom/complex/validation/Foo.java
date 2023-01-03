package com.infobip.typescript.showcase.custom.complex.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class Foo {

    @Size(min = 1, max = 2)
    @ComplexCustomValidation(length = 100)
    @NotEmpty
    @NotNull
    @Valid
    private final String bar;

}
