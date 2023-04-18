package com.infobip.typescript.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.stream.Stream;

import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.Test;

class JsonTypeExtensionSimpleJsonHierarchyTest extends TestBase {

    JsonTypeExtensionSimpleJsonHierarchyTest() {
        super(new JsonTypeExtension(Stream::empty),
              Collections.emptyList());
    }

    @Test
    void shouldAddReadonlyTypeField() {

        // when
        String actual = whenGenerate(Input.from(HierarchyRoot.class,
                                                FirstLeaf.class,
                                                SecondLeaf.class));

        // then
        then(actual).isEqualTo(
            """

                export enum HierarchyType {
                    FIRST_LEAF = "FIRST_LEAF",
                    SECOND_LEAF = "SECOND_LEAF",
                }

                export interface HierarchyRoot {
                    type: HierarchyType;
                }

                export class FirstLeaf implements HierarchyRoot {
                    readonly type: HierarchyType = HierarchyType.FIRST_LEAF;
                    value: string;
                }

                export class SecondLeaf implements HierarchyRoot {
                    readonly type: HierarchyType = HierarchyType.SECOND_LEAF;
                }
                """);
    }

    @Getter
    @AllArgsConstructor
    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        FIRST_LEAF(FirstLeaf.class),
        SECOND_LEAF(SecondLeaf.class);

        private final Class<? extends HierarchyRoot> type;
    }

    interface HierarchyRoot extends SimpleJsonHierarchy<HierarchyType> {

    }

    @Value
    static class FirstLeaf implements HierarchyRoot {

        private final String value;

        public HierarchyType getType() {
            return HierarchyType.FIRST_LEAF;
        }

    }

    static class SecondLeaf implements HierarchyRoot {

        public HierarchyType getType() {
            return HierarchyType.SECOND_LEAF;
        }

    }

}
