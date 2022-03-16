package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.Input;
import lombok.Value;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotBlank;

import static org.assertj.core.api.BDDAssertions.then;

class NotBlankToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo(
                "\n" + IMPORTS + "\n" +
                "export class Foo {\n" +
                "    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })\n" +
                "    bar: string;\n" +
                "}\n");
    }

    @Value
    static class Foo {

        @NotBlank
        String bar;
    }
}
