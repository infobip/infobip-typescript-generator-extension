package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.Input;
import lombok.Value;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.Max;

import static org.assertj.core.api.BDDAssertions.then;

class MaxToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo("\n" + IMPORTS + "\n" +
                               "export class Foo {\n" +
                               "    @Max(1, { message: CommonValidationMessages.Max(1) })\n" +
                               "    bar: any;\n" +
                               "}\n");
    }

    @Value
    static class Foo {

        @Max(value = 1)
        Object bar;
    }
}
