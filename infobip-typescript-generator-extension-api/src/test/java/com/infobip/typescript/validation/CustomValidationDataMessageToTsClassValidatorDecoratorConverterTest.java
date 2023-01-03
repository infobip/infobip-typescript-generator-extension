package com.infobip.typescript.validation;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;

import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.junit.jupiter.api.Test;

class CustomValidationDataMessageToTsClassValidatorDecoratorConverterTest extends TestBase {

    public CustomValidationDataMessageToTsClassValidatorDecoratorConverterTest() {
        super(new ClassValidatorDecoratorExtension("validations", Collections.emptyList(), Collections.emptyList()),
              Arrays.asList("import { CommonValidationMessages } from 'infobip-typescript-generator-common'",
                            "import { validations } from './i18n'",
                            "import { ValidateNested, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator'"));
    }

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(Foo.class));

        // then
        then(actual).isEqualTo("""

                                   import { CommonValidationMessages } from 'infobip-typescript-generator-common';
                                   import { validations } from './i18n';
                                   import { ValidateNested, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator';

                                   export class Foo {
                                       @MaxLength(1000, { message: validations.textMessageMaxLength(1000) })
                                       @IsNotEmpty({ message: validations.textMessageNotEmpty })
                                       bar: string;
                                   }
                                   """);
    }

    @Value
    static class Foo {

        @Size(max = 1000, message = "{textMessageMaxLength}")
        @NotBlank(message = "{textMessageNotEmpty}")
        private final String bar;
    }
}
