package com.infobip.typescript.showcase.custom.validation;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Value
public class Foo {

    @Size(min = 1, max = 2)
    @SimpleCustomValidation(field = "bla", number = 3)
    @NotEmpty
    @NotNull
    @Valid
    private final String bar;
}