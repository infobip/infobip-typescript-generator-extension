package com.infobip.typescript.type;

import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class JsonTypeExtensionMultiLevelDynamicHierarchyTest extends TestBase {

    JsonTypeExtensionMultiLevelDynamicHierarchyTest() {
        super(new JsonTypeExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(DynamicHierarchyRoot.class,
                                                                                       List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                           "LEAF",
                                                                                           DynamicLeaf.class))),
                                                    new DynamicHierarchyDeserializer<>(DynamicHierarchyNode.class,
                                                                                       List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                           "LEAF",
                                                                                           DynamicLeaf.class))))),
              Collections.emptyList());
    }

    @Test
    void shouldHandleMultiLevelHierarchy() {

        // when
        String actual = whenGenerate(Input.from(DynamicHierarchyRoot.class,
                                                DynamicLeaf.class));

        // then
        then(actual).isEqualTo(
            """

                export interface DynamicHierarchyRoot {
                }

                export class DynamicLeaf implements DynamicHierarchyNode {
                    value: string;
                    readonly type: string = "LEAF";
                }
                
                export interface DynamicHierarchyNode extends DynamicHierarchyRoot {
                }
                """);
    }

    interface DynamicHierarchyRoot {

    }

    interface DynamicHierarchyNode extends DynamicHierarchyRoot {

    }

    record DynamicLeaf(String value) implements DynamicHierarchyNode {

            public String getType() {
                return "LEAF";
            }
    }
}
