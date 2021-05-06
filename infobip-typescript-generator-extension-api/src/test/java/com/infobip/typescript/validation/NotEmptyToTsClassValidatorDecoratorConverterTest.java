package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.Input;
import lombok.Value;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotEmpty;

import static org.assertj.core.api.BDDAssertions.then;

class NotEmptyToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo(
                "\n" +
                "import { CommonValidationMessages } from 'infobip-typescript-generator-common';\n" +
                "import { ValidateNested, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator';\n" +
                "\n" +
                "export class Foo {\n" +
                "    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })\n" +
                "    bar: string;\n" +
                "}\n");
    }

    @Value
    static class Foo {

        @NotEmpty
        private final String bar;
    }
}
