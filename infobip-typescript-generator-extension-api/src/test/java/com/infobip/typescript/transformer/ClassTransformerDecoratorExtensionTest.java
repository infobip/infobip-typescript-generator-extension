package com.infobip.typescript.transformer;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;

import com.infobip.jackson.PresentPropertyJsonHierarchy;
import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.Test;

class ClassTransformerDecoratorExtensionTest extends TestBase {

    ClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateNonHierarchiesWithType() {
        String actual = whenGenerate(Input.from(Root.class, Leaf.class, Unsupported.class));

        then(fixNewlines(actual)).isEqualTo(
            """
                import { Type } from 'class-transformer';

                export enum Enumeration {
                    VALUE = "VALUE",
                }

                export class Leaf {
                    value: number;
                }

                export interface Unsupported {
                }

                export class Root {
                    @Type(() => Leaf)
                    leaf: Leaf;
                    leafOfBuiltInType: string;
                    @Type(() => Leaf)
                    leafArray: Leaf[];
                    enumeration: Enumeration;
                    unsupported: Unsupported;
                    @Type(() => Leaf)
                    listOfLeaves: Leaf[];
                }""");
    }

    @Test
    void shouldNotDecorateNonHierarchiesWithAbstractType() {
        String actual = whenGenerate(Input.from(RootWithAbstractLeaf.class, AbstractLeaf.class, Unsupported.class));

        then(fixNewlines(actual)).isEqualTo(
            """
                import { Type } from 'class-transformer';
                
                export class RootWithAbstractLeaf {
                    leaf: AbstractLeaf;
                }
                
                export interface AbstractLeaf {
                }
                
                export interface Unsupported {
                }""");
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

    @Data
    static class Leaf {

        int value;
    }

    @Data
    static class RootWithAbstractLeaf {

        AbstractLeaf leaf;
    }

    interface AbstractLeaf {
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
    enum UnsupportedType implements TypeProvider<Unsupported> {
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
            """
                import { Type } from 'class-transformer';

                export enum FirstHierarchyType {
                    LEAF = "LEAF",
                }

                export enum SecondHierarchyType {
                    LEAF = "LEAF",
                }

                export enum NestedHierarchyType {
                    LEAF = "LEAF",
                }

                export interface FirstHierarchyRoot {
                    type: FirstHierarchyType;
                }

                export interface SecondHierarchyRoot {
                    type: SecondHierarchyType;
                }

                export interface NestedHierarchyRoot {
                    type: NestedHierarchyType;
                }

                export class NestedHierarchyLeaf implements NestedHierarchyRoot {
                    type: NestedHierarchyType;
                }

                export class FirstHierarchyLeaf implements FirstHierarchyRoot {
                    type: FirstHierarchyType;
                    @Type(() => Object, {
                        discriminator: {
                            property: "type", subTypes: [
                                { value: NestedHierarchyLeaf, name: NestedHierarchyType.LEAF }
                            ]
                        }
                    })
                    nested: NestedHierarchyRoot;
                }

                export class SecondHierarchyLeaf implements SecondHierarchyRoot {
                    type: SecondHierarchyType;
                    @Type(() => Object, {
                        discriminator: {
                            property: "type", subTypes: [
                                { value: NestedHierarchyLeaf, name: NestedHierarchyType.LEAF }
                            ]
                        }
                    })
                    nestedArray: NestedHierarchyRoot[];
                    @Type(() => Object, {
                        discriminator: {
                            property: "type", subTypes: [
                                { value: NestedHierarchyLeaf, name: NestedHierarchyType.LEAF }
                            ]
                        }
                    })
                    nestedList: NestedHierarchyRoot[];
                }""");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    @Getter
    @AllArgsConstructor
    enum FirstHierarchyType implements TypeProvider<FirstHierarchyRoot> {
        LEAF(FirstHierarchyLeaf.class);

        private final Class<? extends FirstHierarchyRoot> type;
    }

    @Getter
    @AllArgsConstructor
    enum SecondHierarchyType implements TypeProvider<SecondHierarchyRoot> {
        LEAF(SecondHierarchyLeaf.class);

        private final Class<? extends SecondHierarchyRoot> type;
    }

    @Getter
    @AllArgsConstructor
    enum NestedHierarchyType implements TypeProvider<NestedHierarchyRoot> {
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

        NestedHierarchyRoot[] nestedArray;
        List<NestedHierarchyRoot> nestedList;

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
