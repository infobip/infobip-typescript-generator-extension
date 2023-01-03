package com.infobip.typescript.validation;

import static org.assertj.core.api.BDDAssertions.then;

import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.junit.jupiter.api.Test;

class NotNullToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

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
                    @IsDefined({ message: CommonValidationMessages.IsDefined })
                    bar: string;
                }
                """);
    }

    @Value
    static class Foo {

        @NotNull
        String bar;
    }
}
