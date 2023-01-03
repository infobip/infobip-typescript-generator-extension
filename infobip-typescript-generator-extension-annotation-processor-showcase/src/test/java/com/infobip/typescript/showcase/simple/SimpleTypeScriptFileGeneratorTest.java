package com.infobip.typescript.showcase.simple;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.IOException;
import java.nio.file.Paths;

import com.infobip.typescript.showcase.TestBase;
import org.junit.jupiter.api.Test;

class SimpleTypeScriptFileGeneratorTest extends TestBase {

    @Test
    void shouldGenerateTypeScript() throws IOException {
        // when
        String actual = whenActualFileIsGenerated(Paths.get(".", "dist", "Simple.ts"));

        // then
        then(actual).isEqualTo("""
                                   /* tslint:disable */
                                   /* eslint-disable */
                                   import { IsDefined, IsNotEmpty, MinLength, ValidateNested, MaxLength } from 'class-validator';
                                   import { CommonValidationMessages } from './CommonValidationMessages';

                                   export class Foo {
                                       @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
                                       @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
                                       @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
                                       @IsDefined({ message: CommonValidationMessages.IsDefined })
                                       @ValidateNested()
                                       bar: string;
                                   }""");
    }

}
