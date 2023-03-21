package com.infobip.typescript.transformer;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.Test;

class DynamicHierarchyWithNestedStaticHierarchyInOptionalFieldClassTransformerDecoratorExtensionTest extends TestBase {

    DynamicHierarchyWithNestedStaticHierarchyInOptionalFieldClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(DynamicHierarchyRoot.class,
                                                                                                        List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                                            "LEAF",
                                                                                                            DynamicHierarchyLeaf.class))))),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(StaticHierarchyRoot.class,
                                                StaticHierarchyLeaf.class,
                                                DynamicHierarchyRoot.class,
                                                DynamicHierarchyLeaf.class));

        // then
        then(fixNewlines(actual)).isEqualTo(
            "import { Type } from 'class-transformer';\n" +
            "\n" +
            "export enum StaticHierarchyType {\n" +
            "    LEAF = \"LEAF\",\n" +
            "}\n" +
            "\n" +
            "export interface StaticHierarchyRoot {\n" +
            "    type: StaticHierarchyType;\n" +
            "}\n" +
            "\n" +
            "export class StaticHierarchyLeaf implements StaticHierarchyRoot {\n" +
            "    type: StaticHierarchyType;\n" +
            "}\n" +
            "\n" +
            "export interface DynamicHierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class DynamicHierarchyLeaf implements DynamicHierarchyRoot {\n" +
            "    @Type(() => Object, {\n" +
            "        discriminator: {\n" +
            "            property: \"type\", subTypes: [\n" +
            "                { value: StaticHierarchyLeaf, name: StaticHierarchyType.LEAF }\n" +
            "            ]\n" +
            "        }\n" +
            "    })\n" +
            "    root?: StaticHierarchyRoot;\n" +
            "}");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    interface DynamicHierarchyRoot {

    }

    @Value
    static class DynamicHierarchyLeaf implements DynamicHierarchyRoot {
        Optional<StaticHierarchyRoot> root;

    }

    interface StaticHierarchyRoot extends SimpleJsonHierarchy<StaticHierarchyType> {

    }

    static class StaticHierarchyLeaf implements StaticHierarchyRoot {

        @Override
        public StaticHierarchyType getType() {
            return StaticHierarchyType.LEAF;
        }

    }

    @Getter
    @AllArgsConstructor
    enum StaticHierarchyType implements TypeProvider<StaticHierarchyRoot> {
        LEAF(StaticHierarchyLeaf.class);

        private final Class<? extends StaticHierarchyRoot> type;
    }

}
