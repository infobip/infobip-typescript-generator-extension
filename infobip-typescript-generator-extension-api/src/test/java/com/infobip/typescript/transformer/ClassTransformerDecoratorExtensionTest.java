package com.infobip.typescript.transformer;

import com.infobip.jackson.*;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class ClassTransformerDecoratorExtensionTest extends TestBase {

    ClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateNonHierarchiesWithType() {
        String actual = whenGenerate(Input.from(Root.class, Leaf.class, Unsupported.class));

        then(fixNewlines(actual)).isEqualTo(
                "" +
                        "import { Type } from 'class-transformer';\n" +
                        "\n" +
                        "export enum Enumeration {\n" +
                        "    VALUE = \"VALUE\",\n" +
                        "}\n" +
                        "\n" +
                        "export class Leaf {\n" +
                        "}\n" +
                        "\n" +
                        "export interface Unsupported {\n" +
                        "}\n" +
                        "\n" +
                        "export class Root {\n" +
                        "    @Type(() => Leaf)\n" +
                        "    leaf: Leaf;\n" +
                        "    leafOfBuiltInType: string;\n" +
                        "    @Type(() => Leaf)\n" +
                        "    leafArray: Leaf[];\n" +
                        "    enumeration: Enumeration;\n" +
                        "    unsupported: Unsupported;\n" +
                        "    @Type(() => Leaf)\n" +
                        "    listOfLeaves: Leaf[];\n" +
                        "}");
    }

    @Value
    static class Root {

        Leaf leaf;
        String leafOfBuiltInType;
        Leaf[] leafArray;
        Enumeration enumeration;
        Unsupported unsupported;
        List<Leaf> listOfLeaves;
    }

    @Value
    static class Leaf {

    }

    enum Enumeration {
        VALUE
    }

    interface Unsupported extends PresentPropertyJsonHierarchy<UnsupportedType> {

    }

    static class UnsupportedImpl implements Unsupported {

    }

    @Getter
    @AllArgsConstructor
    enum UnsupportedType implements TypeProvider {
        VALUE(UnsupportedImpl.class);

        private final Class<? extends Unsupported> type;
    }

    @Test
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(FirstHierarchyRoot.class,
                                                FirstHierarchyLeaf.class,
                                                SecondHierarchyRoot.class,
                                                SecondHierarchyLeaf.class,
                                                NestedHierarchyRoot.class,
                                                NestedHierarchyLeaf.class));

        // then
        then(fixNewlines(actual)).isEqualTo(
                "" +
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
                        "    nested: NestedHierarchyRoot[];\n" +
                        "}");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
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

        NestedHierarchyRoot nested;

        @Override
        public FirstHierarchyType getType() {
            return FirstHierarchyType.LEAF;
        }
    }

    @Value
    static class SecondHierarchyLeaf implements SecondHierarchyRoot {

        NestedHierarchyRoot[] nested;

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
