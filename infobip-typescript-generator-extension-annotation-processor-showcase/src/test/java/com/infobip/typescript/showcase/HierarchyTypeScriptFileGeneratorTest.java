package com.infobip.typescript.showcase;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.BDDAssertions.then;

class HierarchyTypeScriptFileGeneratorTest {

    @Test
    void shouldGenerateTypeScript() throws IOException {
        // when
        String actual = Files.lines(Paths.get(".", "dist", "Hierarchy.ts"))
                             .collect(Collectors.joining(System.lineSeparator()));

        // then
        then(actual).isEqualTo("/* tslint:disable */\n" +
                               "/* eslint-disable */\n" +
                               "import 'reflect-metadata';\n" +
                               "import { Type } from 'class-transformer';\n" +
                               "import { IsDefined, IsNotEmpty } from 'class-validator';\n" +
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
                               "export interface Message {\n" +
                               "    channel: Channel;\n" +
                               "    direction: Direction;\n" +
                               "}\n" +
                               "\n" +
                               "export interface OutboundMessage extends Message {\n" +
                               "}\n" +
                               "\n" +
                               "export interface CommonContent extends SimpleJsonHierarchy<CommonContentType>, Content<CommonContentType> {\n" +
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
                               "}\n" +
                               "\n" +
                               "export interface SimpleJsonHierarchy<E> {\n" +
                               "    type: E;\n" +
                               "}\n" +
                               "\n" +
                               "export class InboundSmsMessage implements InboundMessage {\n" +
                               "    readonly channel: Channel = Channel.SMS;\n" +
                               "    direction: Direction;\n" +
                               "    @Type(() => Object, {\n" +
                               "        discriminator: {\n" +
                               "            property: 'type', subTypes: [\n" +
                               "                { value: TextContent, name: CommonContentType.TEXT }\n" +
                               "            ]\n" +
                               "        }\n" +
                               "    })\n" +
                               "    content: CommonContent;\n" +
                               "}\n" +
                               "\n" +
                               "export class OutboundSmsMessage implements OutboundMessage {\n" +
                               "    readonly channel: Channel = Channel.SMS;\n" +
                               "    direction: Direction;\n" +
                               "    @Type(() => Object, {\n" +
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
