package com.infobip.typescript.validation;

import cz.habarta.typescript.generator.Input;
import lombok.Value;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class SizeToTsClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo(
                "\n" + IMPORTS + "\n" +
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
