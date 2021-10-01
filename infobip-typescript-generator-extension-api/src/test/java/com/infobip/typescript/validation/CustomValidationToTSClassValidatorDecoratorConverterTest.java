package com.infobip.typescript.validation;

import static com.infobip.typescript.validation.helpers.AnnotationHelper.getSupportedAnnotations;
import static com.infobip.typescript.validation.helpers.ImportDeclarationHelper.getImportsWithoutLocalization;
import static com.infobip.typescript.validation.helpers.TsCustomDecoratorHelper.getDecorators;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import com.infobip.typescript.validation.custom.CombinedValidation;
import com.infobip.typescript.validation.custom.ComplexValidation;
import com.infobip.typescript.validation.custom.NonExistingTSDecorator;
import com.infobip.typescript.validation.custom.SimpleNoMessageValidation;
import com.infobip.typescript.validation.custom.SimpleValidation;
import com.infobip.typescript.validation.exception.TSValidatorDoesNotExist;
import cz.habarta.typescript.generator.Input;
import lombok.Value;
import org.junit.jupiter.api.Test;

public class CustomValidationToTSClassValidatorDecoratorConverterTest extends ClassValidatorDecoratorTestBase {

    public CustomValidationToTSClassValidatorDecoratorConverterTest() {
        super(getDecorators(), getSupportedAnnotations(), getImportsWithoutLocalization());
    }

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(CustomValidationToTSClassValidatorDecoratorConverterTest.Foo1.class));

        // then
        then(actual).isEqualTo("\n" +
                                       "import { CommonValidationMessages } from 'infobip-typescript-generator-common';\n" +
                                       "import { ComplexValidation } from './validators/ComplexValidation';\n" +
                                       "import { SimpleValidation } from './validators/SimpleValidation';\n" +
                                       "\n" +
                                       "export class Foo1 {\n" +
                                       "    @SimpleValidation({ message: \"must be valid element\" })\n" +
                                       "    bar1: string;\n" +
                                       "    @SimpleValidation()\n" +
                                       "    bar2: string;\n" +
                                       "    @ComplexValidation(100, { message: \"must be valid element\" })\n" +
                                       "    bar3: string;\n" +
                                       "    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })\n" +
                                       "    @SimpleValidation({ message: \"must be valid element\" })\n" +
                                       "    bar4: string;\n" +
                                       "}\n");
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

    @Value
    static class Foo1 {

        @SimpleValidation
        private final String bar1;

        @SimpleNoMessageValidation
        private final String bar2;

        @ComplexValidation(length = 100)
        private final String bar3;

        @CombinedValidation
        private final String bar4;
    }

    @Value
    static class Foo2 {

        @NonExistingTSDecorator
        private final String bar1;

    }
}
