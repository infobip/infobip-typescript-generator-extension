package com.infobip.typescript.type;

import com.infobip.jackson.*;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;

class JsonTypeExtensionTest extends TestBase {

    JsonTypeExtensionTest() {
        super(new JsonTypeExtension(), Collections.emptyList());
    }

    @Test
    void shouldAddReadonlyTypeField() {

        // when
        String actual = whenGenerate(Input.from(HierarchyRoot.class,
                                                FirstLeaf.class,
                                                SecondLeaf.class));

        // then
        then(actual).isEqualTo(
                "\n" +
                "export enum HierarchyType {\n" +
                "    FIRST_LEAF = \"FIRST_LEAF\",\n" +
                "    SECOND_LEAF = \"SECOND_LEAF\",\n" +
                "}\n" +
                "\n" +
                "export interface HierarchyRoot {\n" +
                "    type: HierarchyType;\n" +
                "}\n" +
                "\n" +
                "export class FirstLeaf implements HierarchyRoot {\n" +
                "    readonly type: HierarchyType = HierarchyType.FIRST_LEAF;\n" +
                "}\n" +
                "\n" +
                "export class SecondLeaf implements HierarchyRoot {\n" +
                "    readonly type: HierarchyType = HierarchyType.SECOND_LEAF;\n" +
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
        FIRST_LEAF(FirstLeaf.class),
        SECOND_LEAF(SecondLeaf.class);

        private final Class<? extends HierarchyRoot> type;
    }

    interface HierarchyRoot extends SimpleJsonHierarchy<HierarchyType> {

    }

    static class FirstLeaf implements HierarchyRoot {

        public HierarchyType getType() {
            return HierarchyType.FIRST_LEAF;
        }
    }

    static class SecondLeaf implements HierarchyRoot {

        public HierarchyType getType() {
            return HierarchyType.SECOND_LEAF;
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
