package com.infobip.typescript.type;

import com.infobip.jackson.*;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class JsonTypeExtensionOverridenSimpleJsonResolverHierarchyTest extends TestBase {

    JsonTypeExtensionOverridenSimpleJsonResolverHierarchyTest() {
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
                }

                export class FirstLeaf implements SubsetHierarchyRoot {
                    readonly type: HierarchyType = HierarchyType.FIRST_LEAF;
                }

                export class SecondLeaf implements HierarchyRoot {
                    readonly type: HierarchyType = HierarchyType.SECOND_LEAF;
                }
                
                export interface SubsetHierarchyRoot extends HierarchyRoot {
                }
                """);
    }

    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        FIRST_LEAF(FirstLeaf.class),
        SECOND_LEAF(SecondLeaf.class);

        private final Class<? extends HierarchyRoot> type;

        HierarchyType(Class<? extends HierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends HierarchyRoot> getType() {
            return type;
        }
    }

    enum SubsetHierarchyType implements TypeProvider<HierarchyRoot> {
        FIRST_LEAF(FirstLeaf.class);

        private final Class<? extends HierarchyRoot> type;

        SubsetHierarchyType(Class<? extends HierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends HierarchyRoot> getType() {
            return type;
        }
    }

    @JsonTypeResolveWith(HierarchyRootTypeResolver.class)
    interface HierarchyRoot {

    }

    @JsonTypeResolveWith(SubsetHierarchyRootTypeResolver.class)
    interface SubsetHierarchyRoot extends HierarchyRoot {

    }

    static class FirstLeaf implements SubsetHierarchyRoot {

        public HierarchyType getType() {
            return HierarchyType.FIRST_LEAF;
        }

    }

    static class SecondLeaf implements HierarchyRoot {

        public HierarchyType getType() {
            return HierarchyType.SECOND_LEAF;
        }

    }

    static class HierarchyRootTypeResolver extends SimpleJsonTypeResolver<HierarchyType> {

        public HierarchyRootTypeResolver() {
            super(HierarchyType.class, "type");
        }
    }

    static class SubsetHierarchyRootTypeResolver extends SimpleJsonTypeResolver<SubsetHierarchyType> {

        public SubsetHierarchyRootTypeResolver() {
            super(SubsetHierarchyType.class, "type");
        }
    }

}
