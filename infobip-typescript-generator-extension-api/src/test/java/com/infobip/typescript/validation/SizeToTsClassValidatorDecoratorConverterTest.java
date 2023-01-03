package com.infobip.typescript.validation;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.junit.jupiter.api.Test;

class SizeToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

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
                "    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })\n" +
                "    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })\n" +
                "    @IsOptional()\n" +
                "    bar: string;\n" +
                "    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })\n" +
                "    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })\n" +
                "    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })\n" +
                "    notEmptyBar: string;\n" +
                "    @ArrayMaxSize(4, { message: CommonValidationMessages.ArrayMaxSize(4) })\n" +
                "    @ArrayMinSize(3, { message: CommonValidationMessages.ArrayMinSize(3) })\n" +
                "    objects: any[];\n" +
                "}\n");
    }

    @Value
    static class Foo {

        @Size(min = 1, max = 2)
        String bar;

        @NotEmpty
        @Size(min = 1, max = 2)
        String notEmptyBar;

        @Size(min = 3, max = 4)
        List<Object> objects;
    }
}
