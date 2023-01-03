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
        then(actual).startsWith("""
                                    /* tslint:disable */
                                    /* eslint-disable */
                                    import 'reflect-metadata';
                                    import { Type } from 'class-transformer';
                                    import { IsDefined, IsNotEmpty } from 'class-validator';
                                    import { CommonValidationMessages } from './CommonValidationMessages';

                                    export enum Channel {
                                        SMS = 'SMS',
                                    }

                                    export enum Direction {
                                        INBOUND = 'INBOUND',
                                        OUTBOUND = 'OUTBOUND',
                                    }

                                    export enum CommonContentType {
                                        TEXT = 'TEXT',
                                    }

                                    export interface InboundMessage extends Message {
                                    }

                                    export interface Message {""")
                    .contains("""
                                  }

                                  export interface OutboundMessage extends Message {
                                  }

                                  export interface CommonContent extends Content<CommonContentType> {
                                      type: CommonContentType;
                                  }

                                  export interface Content<T> {
                                      type: T;
                                  }

                                  export interface ContentType {
                                  }

                                  export class TextContent implements CommonContent {
                                      readonly type: CommonContentType = CommonContentType.TEXT;
                                      @IsDefined({ message: CommonValidationMessages.IsDefined })
                                      @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
                                      text: string;
                                  }""")
                    .contains("""
                                  @Type(() => Object, {
                                          discriminator: {
                                              property: 'type', subTypes: [
                                                  { value: TextContent, name: CommonContentType.TEXT }
                                              ]
                                          }
                                      })
                                      content: CommonContent;""")
                    .endsWith("""
                                  @Type(() => Object, {
                                          discriminator: {
                                              property: 'type', subTypes: [
                                                  { value: TextContent, name: CommonContentType.TEXT }
                                              ]
                                          }
                                      })
                                      content: CommonContent;
                                  }""");
    }

}
