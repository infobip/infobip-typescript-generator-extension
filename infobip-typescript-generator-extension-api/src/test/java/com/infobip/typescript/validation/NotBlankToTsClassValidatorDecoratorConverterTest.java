package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class NotBlankToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo(
                """

                        import { CommonValidationMessages } from 'infobip-typescript-generator-common';
                        import { ValidateNested, IsOptional, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator';

                        export class Foo {
                            @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
                            bar: string;
                        }
                        """);
    }

    record Foo(@NotBlank String bar) {

    }
}
