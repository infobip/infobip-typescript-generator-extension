package com.infobip.typescript.transformer;

import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;

class ClassTransformerDecoratorExtensionTest extends TestBase {

    ClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorate() {

        // when
        String actual = whenGenerate(Input.from(FirstHierarchyRoot.class,
                                                FirstHierarchyLeaf.class,
                                                SecondHierarchyRoot.class,
                                                SecondHierarchyLeaf.class,
                                                NestedHierarchyRoot.class,
                                                NestedHierarchyLeaf.class));

        // then
        then(actual).isEqualTo(
                "\n" +
                "import { Type } from 'class-transformer';\n" +
                "\n" +
                "export enum FirstHierarchyType {\n" +
                "    LEAF = \"LEAF\",\n" +
                "}\n" +
                "\n" +
                "export enum SecondHierarchyType {\n" +
                "    LEAF = \"LEAF\",\n" +
                "}\n" +
                "\n" +
                "export enum NestedHierarchyType {\n" +
                "    LEAF = \"LEAF\",\n" +
                "}\n" +
                "\n" +
                "export interface FirstHierarchyRoot {\n" +
                "    type: FirstHierarchyType;\n" +
                "}\n" +
                "\n" +
                "export interface SecondHierarchyRoot {\n" +
                "    type: SecondHierarchyType;\n" +
                "}\n" +
                "\n" +
                "export interface NestedHierarchyRoot {\n" +
                "    type: NestedHierarchyType;\n" +
                "}\n" +
                "\n" +
                "export class NestedHierarchyLeaf implements NestedHierarchyRoot {\n" +
                "    type: NestedHierarchyType;\n" +
                "}\n" +
                "\n" +
                "export class FirstHierarchyLeaf implements FirstHierarchyRoot {\n" +
                "    type: FirstHierarchyType;\n" +
                "    @Type(() => Object, {\n" +
                "        discriminator: {\n" +
                "            property: \"type\", subTypes: [\n" +
                "                { value: NestedHierarchyLeaf, name: NestedHierarchyType.LEAF }\n" +
                "            ]\n" +
                "        }\n" +
                "    })\n" +
                "    nested: NestedHierarchyRoot;\n" +
                "}\n" +
                "\n" +
                "export class SecondHierarchyLeaf implements SecondHierarchyRoot {\n" +
                "    type: SecondHierarchyType;\n" +
                "    @Type(() => Object, {\n" +
                "        discriminator: {\n" +
                "            property: \"type\", subTypes: [\n" +
                "                { value: NestedHierarchyLeaf, name: NestedHierarchyType.LEAF }\n" +
                "            ]\n" +
                "        }\n" +
                "    })\n" +
                "    nested: NestedHierarchyRoot;\n" +
                "}\n");
    }

    @Getter
    @AllArgsConstructor
    enum FirstHierarchyType implements TypeProvider {
        LEAF(FirstHierarchyLeaf.class);

        private final Class<? extends FirstHierarchyRoot> type;
    }

    @Getter
    @AllArgsConstructor
    enum SecondHierarchyType implements TypeProvider {
        LEAF(SecondHierarchyLeaf.class);

        private final Class<? extends SecondHierarchyRoot> type;
    }

    @Getter
    @AllArgsConstructor
    enum NestedHierarchyType implements TypeProvider {
        LEAF(NestedHierarchyLeaf.class);

        private final Class<? extends NestedHierarchyRoot> type;
    }

    interface FirstHierarchyRoot extends SimpleJsonHierarchy<FirstHierarchyType> {

    }

    interface SecondHierarchyRoot extends SimpleJsonHierarchy<SecondHierarchyType> {

    }

    interface NestedHierarchyRoot extends SimpleJsonHierarchy<NestedHierarchyType> {

    }

    @Value
    static class FirstHierarchyLeaf implements FirstHierarchyRoot {

        private final NestedHierarchyRoot nested;

        @Override
        public FirstHierarchyType getType() {
            return FirstHierarchyType.LEAF;
        }
    }

    @Value
    static class SecondHierarchyLeaf implements SecondHierarchyRoot {

        private final NestedHierarchyRoot nested;

        @Override
        public SecondHierarchyType getType() {
            return SecondHierarchyType.LEAF;
        }
    }

    @Value
    static class NestedHierarchyLeaf implements NestedHierarchyRoot {

        @Override
        public NestedHierarchyType getType() {
            return NestedHierarchyType.LEAF;
        }
    }
}
