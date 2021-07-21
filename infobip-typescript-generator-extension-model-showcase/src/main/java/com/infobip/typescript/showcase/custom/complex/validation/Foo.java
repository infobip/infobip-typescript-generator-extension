package com.infobip.typescript.showcase.custom.complex.validation;

import com.infobip.typescript.showcase.custom.simple.validation.SimpleCustomValidation;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Value
public class Foo {

    @Size(min = 1, max = 2)
    @ComplexCustomValidation(length = 100)
    @NotEmpty
    @NotNull
    @Valid
    private final String bar;
}
