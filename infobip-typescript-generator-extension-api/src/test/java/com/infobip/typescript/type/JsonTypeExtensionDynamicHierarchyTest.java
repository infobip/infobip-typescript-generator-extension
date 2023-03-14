package com.infobip.typescript.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.infobip.jackson.TypeProvider;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.Test;

class JsonTypeExtensionDynamicHierarchyTest extends TestBase {

    JsonTypeExtensionDynamicHierarchyTest() {
        super(new JsonTypeExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(DynamicHierarchyRoot.class,
                                                                                       List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                           "LEAF",
                                                                                           DynamicLeaf.class))),
                                                    new DynamicHierarchyDeserializer<>(DynamicHierarchyRootWithEnum.class,
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
            "\n" +
            "export interface DynamicHierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class DynamicLeaf implements DynamicHierarchyRoot {\n" +
            "    value: string;\n" +
            "    readonly type: string = \"LEAF\";\n" +
            "}\n");
    }

    @Test
    void shouldAddReadonlyTypeFieldForDynamicHierarchyWithEnum() {

        // when
        String actual = whenGenerate(Input.from(DynamicHierarchyRootWithEnum.class,
                                                DynamicLeafWithEnum.class));

        // then
        then(actual).isEqualTo(
            "\n" +
            "export enum DynamicHierarchyRootWithEnumType {\n" +
            "    LEAF = \"LEAF\",\n" +
            "}\n" +
            "\n" +
            "export interface DynamicHierarchyRootWithEnum {\n" +
            "    type: DynamicHierarchyRootWithEnumType;\n" +
            "}\n" +
            "\n" +
            "export class DynamicLeafWithEnum implements DynamicHierarchyRootWithEnum {\n" +
            "    readonly type: DynamicHierarchyRootWithEnumType = DynamicHierarchyRootWithEnumType.LEAF;\n" +
            "    value: string;\n" +
            "}\n");
    }

    interface DynamicHierarchyRoot {

    }

    @Value
    static class DynamicLeaf implements DynamicHierarchyRoot {

        private final String value;

        public String getType() {
            return "LEAF";
        }

    }

    interface DynamicHierarchyRootWithEnum {

        DynamicHierarchyRootWithEnumType getType();

    }

    @Value
    static class DynamicLeafWithEnum implements DynamicHierarchyRootWithEnum {

        private final String value;

        public DynamicHierarchyRootWithEnumType getType() {
            return DynamicHierarchyRootWithEnumType.LEAF;
        }

    }

    @Getter
    @AllArgsConstructor
    enum DynamicHierarchyRootWithEnumType implements TypeProvider<DynamicHierarchyRootWithEnum> {
        LEAF(DynamicLeafWithEnum.class);

        private final Class<? extends DynamicHierarchyRootWithEnum> type;
    }

}
