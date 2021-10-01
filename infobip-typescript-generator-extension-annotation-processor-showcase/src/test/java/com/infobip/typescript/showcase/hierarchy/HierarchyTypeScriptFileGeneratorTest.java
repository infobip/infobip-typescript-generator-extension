package com.infobip.typescript.showcase.hierarchy;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.IOException;
import java.nio.file.Paths;

import com.infobip.typescript.showcase.TestBase;
import org.junit.jupiter.api.Test;

class HierarchyTypeScriptFileGeneratorTest extends TestBase {

    @Test
    void shouldGenerateTypeScript() throws IOException {
        // when
        String actual = whenActualFileIsGenerated(Paths.get(".", "dist", "Hierarchy.ts"));

        // then
        then(actual).startsWith("/* tslint:disable */\n" +
                                    "/* eslint-disable */\n" +
                                    "import 'reflect-metadata';\n" +
                                    "import { Type } from 'class-transformer';\n" +
                                    "import { IsDefined, IsNotEmpty } from 'class-validator';\n" +
                                    "import { CommonValidationMessages } from './CommonValidationMessages';\n" +
                                    "\n" +
                                    "export enum Channel {\n" +
                                    "    SMS = 'SMS',\n" +
                                    "}\n" +
                                    "\n" +
                                    "export enum Direction {\n" +
                                    "    INBOUND = 'INBOUND',\n" +
                                    "    OUTBOUND = 'OUTBOUND',\n" +
                                    "}\n" +
                                    "\n" +
                                    "export enum CommonContentType {\n" +
                                    "    TEXT = 'TEXT',\n" +
                                    "}\n" +
                                    "\n" +
                                    "export interface InboundMessage extends Message {\n" +
                                    "}\n" +
                                    "\n" +
                                    "export interface Message {")
                    .contains("}\n" +
                                  "\n" +
                                  "export interface OutboundMessage extends Message {\n" +
                                  "}\n" +
                                  "\n" +
                                  "export interface CommonContent extends Content<CommonContentType> {\n" +
                                  "    type: CommonContentType;\n" +
                                  "}\n" +
                                  "\n" +
                                  "export interface Content<T> {\n" +
                                  "    type: T;\n" +
                                  "}\n" +
                                  "\n" +
                                  "export interface ContentType {\n" +
                                  "}\n" +
                                  "\n" +
                                  "export class TextContent implements CommonContent {\n" +
                                  "    readonly type: CommonContentType = CommonContentType.TEXT;\n" +
                                  "    @IsDefined({ message: CommonValidationMessages.IsDefined })\n" +
                                  "    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })\n" +
                                  "    text: string;\n" +
                                  "}")
                    .contains("@Type(() => Object, {\n" +
                                  "        discriminator: {\n" +
                                  "            property: 'type', subTypes: [\n" +
                                  "                { value: TextContent, name: CommonContentType.TEXT }\n" +
                                  "            ]\n" +
                                  "        }\n" +
                                  "    })\n" +
                                  "    content: CommonContent;")
                    .endsWith("@Type(() => Object, {\n" +
                                  "        discriminator: {\n" +
                                  "            property: 'type', subTypes: [\n" +
                                  "                { value: TextContent, name: CommonContentType.TEXT }\n" +
                                  "            ]\n" +
                                  "        }\n" +
                                  "    })\n" +
                                  "    content: CommonContent;\n" +
                                  "}");
    }

}
