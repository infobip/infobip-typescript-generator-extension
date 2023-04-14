package com.infobip.typescript.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.stream.Stream;

import com.infobip.jackson.JsonTypeResolveWith;
import com.infobip.jackson.SimpleJsonTypeResolver;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

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
            "\n" +
            "export enum HierarchyType {\n" +
            "    FIRST_LEAF = \"FIRST_LEAF\",\n" +
            "    SECOND_LEAF = \"SECOND_LEAF\",\n" +
            "}\n" +
            "\n" +
            "export interface HierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class FirstLeaf implements SubsetHierarchyRoot {\n" +
            "    readonly type: HierarchyType = HierarchyType.FIRST_LEAF;\n" +
            "}\n" +
            "\n" +
            "export class SecondLeaf implements HierarchyRoot {\n" +
            "    readonly type: HierarchyType = HierarchyType.SECOND_LEAF;\n" +
            "}\n" +
            "\n" +
            "export interface SubsetHierarchyRoot extends HierarchyRoot {\n" +
            "}\n");
    }

    @Getter
    @AllArgsConstructor
    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        FIRST_LEAF(FirstLeaf.class),
        SECOND_LEAF(SecondLeaf.class);

        private final Class<? extends HierarchyRoot> type;
    }

    @Getter
    @AllArgsConstructor
    enum SubsetHierarchyType implements TypeProvider<HierarchyRoot> {
        FIRST_LEAF(FirstLeaf.class);

        private final Class<? extends HierarchyRoot> type;
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
