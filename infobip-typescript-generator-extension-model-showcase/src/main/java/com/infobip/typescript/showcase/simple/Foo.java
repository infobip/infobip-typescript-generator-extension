package com.infobip.typescript.showcase.simple;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Value
public class Foo {

    @Size(min = 1, max = 2)
    @NotEmpty
    @NotNull
    @Valid
    private final String bar;

}
