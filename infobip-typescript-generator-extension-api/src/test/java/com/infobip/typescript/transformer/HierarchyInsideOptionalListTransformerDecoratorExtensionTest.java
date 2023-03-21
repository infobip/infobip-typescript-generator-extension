package com.infobip.typescript.transformer;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.Test;

class HierarchyInsideOptionalListTransformerDecoratorExtensionTest extends TestBase {

    HierarchyInsideOptionalListTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(Stream::empty),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(HierarchyWrapper.class,
                                                HierarchyRoot.class,
                                                HierarchyLeaf.class));

        // then
        then(fixNewlines(actual)).isEqualTo("import { Type } from 'class-transformer';\n" +
                                            "\n" +
                                            "export enum HierarchyType {\n" +
                                            "    LEAF = \"LEAF\",\n" +
                                            "}\n" +
                                            "\n" +
                                            "export interface HierarchyRoot {\n" +
                                            "    type: HierarchyType;\n" +
                                            "}\n" +
                                            "\n" +
                                            "export class HierarchyLeaf implements HierarchyRoot {\n" +
                                            "    type: HierarchyType;\n" +
                                            "}\n" +
                                            "\n" +
                                            "export class HierarchyWrapper {\n" +
                                            "    @Type(() => Object, {\n" +
                                            "        discriminator: {\n" +
                                            "            property: \"type\", subTypes: [\n" +
                                            "                { value: HierarchyLeaf, name: HierarchyType.LEAF }\n" +
                                            "            ]\n" +
                                            "        }\n" +
                                            "    })\n" +
                                            "    roots?: HierarchyRoot[];\n" +
                                            "}");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    @Value
    static class HierarchyWrapper {
        private final Optional<List<HierarchyRoot>> roots;
    }

    interface HierarchyRoot extends SimpleJsonHierarchy<HierarchyType> {

    }

    static class HierarchyLeaf implements HierarchyRoot {

        @Override
        public HierarchyType getType() {
            return HierarchyType.LEAF;
        }

    }

    @Getter
    @AllArgsConstructor
    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        LEAF(HierarchyLeaf.class);

        private final Class<? extends HierarchyRoot> type;
    }

}
