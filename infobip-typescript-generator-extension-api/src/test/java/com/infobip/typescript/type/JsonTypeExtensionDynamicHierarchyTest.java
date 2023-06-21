package com.infobip.typescript.type;

import com.infobip.jackson.TypeProvider;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class JsonTypeExtensionDynamicHierarchyTest extends TestBase {

    JsonTypeExtensionDynamicHierarchyTest() {
        super(new JsonTypeExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(DynamicHierarchyRoot.class,
                                                                                       List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                               "LEAF",
                                                                                               DynamicLeaf.class))),
                                                    new DynamicHierarchyDeserializer<>(
                                                            DynamicHierarchyRootWithEnum.class,
                                                            List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                    DynamicHierarchyRootWithEnumType.LEAF,
                                                                    DynamicLeafWithEnum.class))))),
              Collections.emptyList());
    }

    @Test
    void shouldAddReadonlyTypeFieldForDynamicHierarchy() {

        // when
        String actual = whenGenerate(Input.from(DynamicHierarchyRoot.class,
                                                DynamicLeaf.class));

        // then
        then(actual).isEqualTo(
                """

                        export interface DynamicHierarchyRoot {
                        }

                        export class DynamicLeaf implements DynamicHierarchyRoot {
                            value: string;
                            readonly type: string = "LEAF";
                        }
                        """);
    }

    @Test
    void shouldAddReadonlyTypeFieldForDynamicHierarchyWithEnum() {

        // when
        String actual = whenGenerate(Input.from(DynamicHierarchyRootWithEnum.class,
                                                DynamicLeafWithEnum.class));

        // then
        then(actual).isEqualTo(
                """
                                        
                        export enum DynamicHierarchyRootWithEnumType {
                            LEAF = "LEAF",
                        }
                                        
                        export interface DynamicHierarchyRootWithEnum {
                            type: DynamicHierarchyRootWithEnumType;
                        }

                        export class DynamicLeafWithEnum implements DynamicHierarchyRootWithEnum {
                            readonly type: DynamicHierarchyRootWithEnumType = DynamicHierarchyRootWithEnumType.LEAF;
                            value: string;
                        }
                        """);
    }

    interface DynamicHierarchyRoot {

    }

    record DynamicLeaf(String value) implements DynamicHierarchyRoot {

        public String getType() {
            return "LEAF";
        }
    }

    interface DynamicHierarchyRootWithEnum {

        DynamicHierarchyRootWithEnumType getType();
    }

    record DynamicLeafWithEnum(String value) implements DynamicHierarchyRootWithEnum {

        public DynamicHierarchyRootWithEnumType getType() {
            return DynamicHierarchyRootWithEnumType.LEAF;
        }
    }

    enum DynamicHierarchyRootWithEnumType implements TypeProvider<DynamicHierarchyRootWithEnum> {
        LEAF(DynamicLeafWithEnum.class);

        private final Class<? extends DynamicHierarchyRootWithEnum> type;

        DynamicHierarchyRootWithEnumType(Class<? extends DynamicHierarchyRootWithEnum> type) {
            this.type = type;
        }

        @Override
        public Class<? extends DynamicHierarchyRootWithEnum> getType() {
            return type;
        }
    }
}
