package com.infobip.typescript.showcase.validation;

import com.infobip.typescript.showcase.TestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.BDDAssertions.then;

public class SimpleCustomValidationTypeScriptFileGeneratorTest extends TestBase {

    @Test
    void shouldGenerateTypeScript() throws IOException {
        // when
        String actual = whenActualFileIsGenerated(Paths.get(".", "dist", "SimpleValidation.ts"));

        // then
        then(actual).isEqualTo("""
                                   /* tslint:disable */
                                   /* eslint-disable */
                                   import { IsDefined, IsNotEmpty, MinLength, ValidateNested, MaxLength } from 'class-validator';
                                   import { CommonValidationMessages } from './CommonValidationMessages';
                                   import { SimpleCustomValidation } from './validators/SimpleCustomValidation';

                                   export class Foo {
                                       @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
                                       @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
                                       @SimpleCustomValidation({ message: 'must be valid element' })
                                       @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
                                       @IsDefined({ message: CommonValidationMessages.IsDefined })
                                       @ValidateNested()
                                       bar: string;
                                   }""");
    }

}
