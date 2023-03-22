package com.infobip.typescript.validation;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Optional;

import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

class SizeToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

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
                    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
                    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
                    @IsOptional()
                    bar: string;
                    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
                    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
                    @IsOptional()
                    optionalBar?: string;
                    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
                    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
                    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
                    notEmptyBar: string;
                    @ArrayMaxSize(4, { message: CommonValidationMessages.ArrayMaxSize(4) })
                    @ArrayMinSize(3, { message: CommonValidationMessages.ArrayMinSize(3) })
                    objects: any[];
                    @ArrayMaxSize(4, { message: CommonValidationMessages.ArrayMaxSize(4) })
                    @ArrayMinSize(3, { message: CommonValidationMessages.ArrayMinSize(3) })
                    optionalObjects?: any[];
                }
                """);
    }

    record Foo(
        @Size(min = 1, max = 2) String bar,
        Optional<@Size(min = 1, max = 2) String> optionalBar,
        @NotEmpty @Size(min = 1, max = 2) String notEmptyBar,
        @Size(min = 3, max = 4) List<Object> objects,
        Optional<@Size(min = 3, max = 4) List<Object>> optionalObjects
    ) {

    }
}
