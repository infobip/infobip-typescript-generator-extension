package com.infobip.typescript.type;

import com.infobip.jackson.*;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class JsonTypeExtensionTest extends TestBase {

    JsonTypeExtensionTest() {
        super(new JsonTypeExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(DynamicHierarchyRoot.class,
                                                                                       List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                               "LEAF",
                                                                                               DynamicLeaf.class))),
                                                    new DynamicHierarchyDeserializer<>(
                                                            DynamicHierarchyRootWithEnum.class,
                                                            List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                    DynamicHierarchyRootWithEnumType.LEAF,
                                                                    DynamicLeafWithEnum.class))))),
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
                        }

                        export class SecondLeaf implements HierarchyRoot {
                            readonly type: HierarchyType = HierarchyType.SECOND_LEAF;
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

    @Test
    void shouldAddReadonlyTypeFieldForDynamicHierarchy() {

        // when
        String actual = whenGenerate(Input.from(DynamicHierarchyRoot.class,
                                                DynamicLeaf.class));

        // then
        then(actual).isEqualTo(
                """

                        export interface DynamicHierarchyRoot {
                        }

                        export class DynamicLeaf implements DynamicHierarchyRoot {
                            value: string;
                            readonly type: string = "LEAF";
                        }
                        """);
    }

    @Test
    void shouldAddReadonlyTypeFieldForDynamicHierarchyWithEnum() {

        // when
        String actual = whenGenerate(Input.from(DynamicHierarchyRootWithEnum.class,
                                                DynamicLeafWithEnum.class));

        // then
        then(actual).isEqualTo(
                """
                                        
                        export enum DynamicHierarchyRootWithEnumType {
                            LEAF = "LEAF",
                        }
                                        
                        export interface DynamicHierarchyRootWithEnum {
                            type: DynamicHierarchyRootWithEnumType;
                        }

                        export class DynamicLeafWithEnum implements DynamicHierarchyRootWithEnum {
                            readonly type: DynamicHierarchyRootWithEnumType = DynamicHierarchyRootWithEnumType.LEAF;
                            value: string;
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

    enum PresentPropertyHierarchyType implements TypeProvider<PresentPropertyHierarchyRoot> {
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

    interface DynamicHierarchyRoot {

    }

    record DynamicLeaf(String value) implements DynamicHierarchyRoot {

        public String getType() {
            return "LEAF";
        }
    }

    interface DynamicHierarchyRootWithEnum {

        DynamicHierarchyRootWithEnumType getType();
    }

    record DynamicLeafWithEnum(String value) implements DynamicHierarchyRootWithEnum {

        public DynamicHierarchyRootWithEnumType getType() {
            return DynamicHierarchyRootWithEnumType.LEAF;
        }
    }

    enum DynamicHierarchyRootWithEnumType implements TypeProvider<DynamicHierarchyRootWithEnum> {
        LEAF(DynamicLeafWithEnum.class);

        private final Class<? extends DynamicHierarchyRootWithEnum> type;

        DynamicHierarchyRootWithEnumType(Class<? extends DynamicHierarchyRootWithEnum> type) {
            this.type = type;
        }

        @Override
        public Class<? extends DynamicHierarchyRootWithEnum> getType() {
            return type;
        }
    }
}
