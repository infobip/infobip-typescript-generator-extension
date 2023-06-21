package com.infobip.typescript.validation;

import com.infobip.typescript.validation.custom.*;
import com.infobip.typescript.validation.exception.TSValidatorDoesNotExist;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import static com.infobip.typescript.validation.helpers.AnnotationHelper.getSupportedAnnotations;
import static com.infobip.typescript.validation.helpers.ImportDeclarationHelper.getImportsWithoutLocalization;
import static com.infobip.typescript.validation.helpers.TsCustomDecoratorHelper.getDecorators;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class CustomValidationToTSClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    public CustomValidationToTSClassValidatorDecoratorConverterTest() {
        super(getDecorators(), getSupportedAnnotations(), getImportsWithoutLocalization());
    }

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(CustomValidationToTSClassValidatorDecoratorConverterTest.Foo1.class));

        // then
        then(actual).isEqualTo("""

                                   import { CommonValidationMessages } from 'infobip-typescript-generator-common';
                                   import { ComplexValidation } from './validators/ComplexValidation';
                                   import { SimpleValidation } from './validators/SimpleValidation';

                                   export class Foo1 {
                                       @SimpleValidation({ message: "must be valid element" })
                                       bar1: string;
                                       @SimpleValidation()
                                       bar2: string;
                                       @ComplexValidation(100, { message: "must be valid element" })
                                       bar3: string;
                                       @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
                                       @SimpleValidation({ message: "must be valid element" })
                                       bar4: string;
                                   }
                                   """);
    }

    @Test
    void shouldThrowExceptionWhenDecoratorIsMissing() {
        // when
        Throwable actual = catchThrowable(
                () -> whenGenerate(Input.from(CustomValidationToTSClassValidatorDecoratorConverterTest.Foo2.class)));

        // then
        assertThat(actual)
                .isInstanceOf(TSValidatorDoesNotExist.class)
                .hasMessageContaining(
                        "For given annotation: NonExistingTSDecorator, TypeScript decorator does not exists");
    }

    record Foo1(

            @SimpleValidation
            String bar1,

            @SimpleNoMessageValidation
            String bar2,

            @ComplexValidation(length = 100)
            String bar3,

            @CombinedValidation
            String bar4

    ) {

    }

    record Foo2(

            @NonExistingTSDecorator
            String bar1

    ) {

    }
}
