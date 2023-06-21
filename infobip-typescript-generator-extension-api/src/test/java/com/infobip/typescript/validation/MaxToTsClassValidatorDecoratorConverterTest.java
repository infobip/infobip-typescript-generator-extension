package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.Max;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class MaxToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo("""

                                       import { CommonValidationMessages } from 'infobip-typescript-generator-common';
                                       import { ValidateNested, IsOptional, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator';

                                       export class Foo {
                                           @Max(1, { message: CommonValidationMessages.Max(1) })
                                           bar: any;
                                       }
                                       """);
    }

    record Foo(@Max(value = 1) Object bar) {

    }
}
