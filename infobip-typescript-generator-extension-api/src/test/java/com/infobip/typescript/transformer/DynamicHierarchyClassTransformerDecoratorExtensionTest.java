package com.infobip.typescript.transformer;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.Value;
import org.junit.jupiter.api.Test;

class DynamicHierarchyClassTransformerDecoratorExtensionTest extends TestBase {

    DynamicHierarchyClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(HierarchyRoot.class,
                                                                                                        List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                                            "LEAF",
                                                                                                            HierarchyLeaf.class))))),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(HierarchyRoot.class,
                                                HierarchyLeaf.class,
                                                HierarchyContainer.class));

        // then
        then(fixNewlines(actual)).isEqualTo(
            "import { Type } from 'class-transformer';\n" +
            "\n" +
            "export interface HierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class HierarchyLeaf implements HierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class HierarchyContainer {\n" +
            "    @Type(() => Object, {\n" +
            "        discriminator: {\n" +
            "            property: \"type\", subTypes: [\n" +
            "                { value: HierarchyLeaf, name: \"LEAF\" }\n" +
            "            ]\n" +
            "        }\n" +
            "    })\n" +
            "    root: HierarchyRoot;\n" +
            "}");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    interface HierarchyRoot {

    }

    @Value
    static class HierarchyLeaf implements HierarchyRoot {

    }

    @Value
    static class HierarchyContainer {

        private final HierarchyRoot root;

    }

}
