package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.Input;
import lombok.Value;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotNull;

import static org.assertj.core.api.BDDAssertions.then;

class NotNullToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo(
                "\n" + IMPORTS + "\n" +
                "export class Foo {\n" +
                "    @IsDefined({ message: CommonValidationMessages.IsDefined })\n" +
                "    bar: string;\n" +
                "}\n");
    }

    @Value
    static class Foo {

        @NotNull
        String bar;
    }
}
