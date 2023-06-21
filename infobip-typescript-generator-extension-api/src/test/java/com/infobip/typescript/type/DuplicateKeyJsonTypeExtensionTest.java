package com.infobip.typescript.type;

import com.infobip.jackson.*;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;

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
            """

                export enum HierarchyType {
                    FIRST_LEAF = "FIRST_LEAF",
                    FIRST = "FIRST",
                }

                export interface HierarchyRoot {
                    type: HierarchyType;
                }

                export class FirstLeaf implements HierarchyRoot {
                    readonly type: HierarchyType = HierarchyType.FIRST_LEAF;
                }
                """);
    }

    @Test
    void shouldAllowPresentPropertyHierarchies() {

        // when
        String actual = whenGenerate(Input.from(PresentPropertyHierarchyRoot.class,
                                             One.class,
                                             Two.class));

        // then
        then(actual).isEqualTo(
            """

                export interface PresentPropertyHierarchyRoot {
                }

                export class One implements PresentPropertyHierarchyRoot {
                }

                export class Two implements PresentPropertyHierarchyRoot {
                }
                """);
}


    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        FIRST_LEAF(FirstLeaf.class),
        FIRST(FirstLeaf.class);

        private final Class<? extends HierarchyRoot> type;

        HierarchyType(Class<? extends HierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends HierarchyRoot> getType() {
            return type;
        }
    }

    interface HierarchyRoot extends SimpleJsonHierarchy<HierarchyType> {

    }

    static class FirstLeaf implements HierarchyRoot {

        public HierarchyType getType() {
            return HierarchyType.FIRST_LEAF;
        }
    }

    enum PresentPropertyHierarchyType implements TypeProvider {
        ONE(One.class),
        TWO(Two.class);

        private final Class<? extends PresentPropertyHierarchyRoot> type;

        PresentPropertyHierarchyType(Class<? extends PresentPropertyHierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends PresentPropertyHierarchyRoot> getType() {
            return type;
        }
    }

    interface PresentPropertyHierarchyRoot extends PresentPropertyJsonHierarchy<PresentPropertyHierarchyType> {

    }

    static class One implements PresentPropertyHierarchyRoot {

    }

    static class Two implements PresentPropertyHierarchyRoot {

    }
}
