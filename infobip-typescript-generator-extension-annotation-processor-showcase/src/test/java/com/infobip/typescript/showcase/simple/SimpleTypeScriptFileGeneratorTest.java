package com.infobip.typescript.showcase.simple;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.BDDAssertions.then;

class SimpleTypeScriptFileGeneratorTest {

    @Test
    void shouldGenerateTypeScript() throws IOException {
        // when
        String actual = Files.lines(Paths.get(".", "dist", "Simple.ts"))
                             .collect(Collectors.joining(System.lineSeparator()));

        // then
        then(actual.replace("\r\n","\n")).isEqualTo("/* tslint:disable */\n" +
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
