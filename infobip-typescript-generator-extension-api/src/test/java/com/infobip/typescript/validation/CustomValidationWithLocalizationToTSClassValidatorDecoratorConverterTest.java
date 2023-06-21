package com.infobip.typescript.validation;

import com.infobip.typescript.validation.custom.ComplexValidation;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import static com.infobip.typescript.validation.helpers.AnnotationHelper.getSupportedAnnotations;
import static com.infobip.typescript.validation.helpers.ImportDeclarationHelper.getImports;
import static com.infobip.typescript.validation.helpers.TsCustomDecoratorHelper.getDecorators;
import static org.assertj.core.api.BDDAssertions.then;

public class CustomValidationWithLocalizationToTSClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    public CustomValidationWithLocalizationToTSClassValidatorDecoratorConverterTest() {
        super(getDecorators(), getSupportedAnnotations(), getImports());
    }

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(CustomValidationWithLocalizationToTSClassValidatorDecoratorConverterTest.Foo1.class));

        // then
        then(actual).isEqualTo("""

                                   import { CommonValidationMessages } from 'infobip-typescript-generator-common';
                                   import { ComplexValidation } from './validators/ComplexValidation';
                                   import { SimpleValidation } from './validators/SimpleValidation';
                                   import { localize } from './Localization';

                                   export class Foo1 {
                                       @ComplexValidation(100, { message: localize("Value must be valid {length}", { length: 100 }) })
                                       bar1: string;
                                   }
                                   """);
    }

    record Foo1(

            @ComplexValidation(length = 100, message = "Value must be valid {length}")
            String bar1

    ) {

    }

}
