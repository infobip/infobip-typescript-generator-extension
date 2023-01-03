package com.infobip.typescript.validation;

import static org.assertj.core.api.BDDAssertions.then;

import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.Min;
import lombok.Value;
import org.junit.jupiter.api.Test;

class MinToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

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
                    @Min(1, { message: CommonValidationMessages.Min(1) })
                    bar: any;
                }
                """);
    }

    @Value
    static class Foo {

        @Min(1)
        Object bar;
    }
}
