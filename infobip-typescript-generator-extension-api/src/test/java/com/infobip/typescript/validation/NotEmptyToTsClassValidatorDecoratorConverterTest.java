package com.infobip.typescript.validation;

import static org.assertj.core.api.BDDAssertions.then;

import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;
import org.junit.jupiter.api.Test;

class NotEmptyToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo(
                "\n" +
                "import { CommonValidationMessages } from 'infobip-typescript-generator-common';\n" +
                "import { ValidateNested, IsOptional, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator';\n" +
                "\n" +
                "export class Foo {\n" +
                "    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })\n" +
                "    bar: string;\n" +
                "}\n");
    }

    @Value
    static class Foo {

        @NotEmpty
        String bar;
    }
}
