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
        then(actual).isEqualTo("/* tslint:disable */\n" +
                                   "/* eslint-disable */\n" +
                                   "import { IsDefined, IsNotEmpty, MinLength, ValidateNested, MaxLength } from 'class-validator';\n" +
                                   "import { CommonValidationMessages } from './CommonValidationMessages';\n" +
                                   "\n" +
                                   "export class Foo {\n" +
                                   "    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })\n" +
                                   "    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })\n" +
                                   "    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })\n" +
                                   "    @IsDefined({ message: CommonValidationMessages.IsDefined })\n" +
                                   "    @ValidateNested()\n" +
                                   "    bar: string;\n" +
                                   "}");
    }

}
