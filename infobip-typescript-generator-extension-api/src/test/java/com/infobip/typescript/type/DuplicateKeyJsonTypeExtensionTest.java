package com.infobip.typescript.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;

import com.infobip.jackson.PresentPropertyJsonHierarchy;
import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class DuplicateKeyJsonTypeExtensionTest extends TestBase {

    DuplicateKeyJsonTypeExtensionTest() {
        super(new JsonTypeExtension(), Collections.emptyList());
    }

    @Test
    void shouldAddReadonlyTypeField() {

        // when
        String actual = whenGenerate(Input.from(HierarchyRoot.class,
                                                FirstLeaf.class));

        // then
        then(actual).isEqualTo(
            "\n" +
            "export enum HierarchyType {\n" +
            "    FIRST = \"FIRST\",\n" +
            "    FIRST_LEAF = \"FIRST_LEAF\",\n" +
            "}\n" +
            "\n" +
            "export interface HierarchyRoot {\n" +
            "    type: HierarchyType;\n" +
            "}\n" +
            "\n" +
            "export class FirstLeaf implements HierarchyRoot {\n" +
            "    readonly type: HierarchyType = HierarchyType.FIRST_LEAF;\n" +
            "}\n");
    }

    @Test
    void shouldAllowPresentPropertyHierarchies() {

        // when
        String actual = whenGenerate(Input.from(PresentPropertyHierarchyRoot.class,
                                             One.class,
                                             Two.class));

        // then
        then(actual).isEqualTo(
            "\n" +
            "export interface PresentPropertyHierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class One implements PresentPropertyHierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class Two implements PresentPropertyHierarchyRoot {\n" +
            "}\n");
}


    @Getter
    @AllArgsConstructor
    enum HierarchyType implements TypeProvider {
        FIRST(FirstLeaf.class),
        FIRST_LEAF(FirstLeaf.class);

        private final Class<? extends HierarchyRoot> type;
    }

    interface HierarchyRoot extends SimpleJsonHierarchy<HierarchyType> {

    }

    static class FirstLeaf implements HierarchyRoot {

        public HierarchyType getType() {
            return HierarchyType.FIRST_LEAF;
        }
    }

    @Getter
    @AllArgsConstructor
    enum PresentPropertyHierarchyType implements TypeProvider {
        ONE(One.class),
        TWO(Two.class);

        private final Class<? extends PresentPropertyHierarchyRoot> type;
    }

    interface PresentPropertyHierarchyRoot extends PresentPropertyJsonHierarchy<PresentPropertyHierarchyType> {

    }

    static class One implements PresentPropertyHierarchyRoot {

    }

    static class Two implements PresentPropertyHierarchyRoot {

    }
}
