package com.infobip.typescript.transformer;

import com.infobip.jackson.*;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

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

    record Root(

            Leaf leaf,
            String leafOfBuiltInType,
            Leaf[] leafArray,
            Enumeration enumeration,
            Unsupported unsupported,
            List<Leaf> listOfLeaves

    ) {

    }

    record Leaf(int value) {

    }

    record RootWithAbstractLeaf(AbstractLeaf leaf) {

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

    enum UnsupportedType implements TypeProvider<Unsupported> {
        VALUE(UnsupportedImpl.class);

        private final Class<? extends Unsupported> type;

        UnsupportedType(Class<? extends Unsupported> type) {
            this.type = type;
        }

        @Override
        public Class<? extends Unsupported> getType() {
            return type;
        }
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

    enum FirstHierarchyType implements TypeProvider<FirstHierarchyRoot> {
        LEAF(FirstHierarchyLeaf.class);

        private final Class<? extends FirstHierarchyRoot> type;

        FirstHierarchyType(Class<? extends FirstHierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends FirstHierarchyRoot> getType() {
            return type;
        }
    }

    enum SecondHierarchyType implements TypeProvider<SecondHierarchyRoot> {
        LEAF(SecondHierarchyLeaf.class);

        private final Class<? extends SecondHierarchyRoot> type;

        SecondHierarchyType(Class<? extends SecondHierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends SecondHierarchyRoot> getType() {
            return type;
        }
    }

    enum NestedHierarchyType implements TypeProvider<NestedHierarchyRoot> {
        LEAF(NestedHierarchyLeaf.class);

        private final Class<? extends NestedHierarchyRoot> type;

        NestedHierarchyType(Class<? extends NestedHierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends NestedHierarchyRoot> getType() {
            return type;
        }
    }

    interface FirstHierarchyRoot extends SimpleJsonHierarchy<FirstHierarchyType> {

    }

    interface SecondHierarchyRoot extends SimpleJsonHierarchy<SecondHierarchyType> {

    }

    interface NestedHierarchyRoot extends SimpleJsonHierarchy<NestedHierarchyType> {

    }

    record FirstHierarchyLeaf(NestedHierarchyRoot nested) implements FirstHierarchyRoot {

        @Override
        public FirstHierarchyType getType() {
            return FirstHierarchyType.LEAF;
        }
    }

    record SecondHierarchyLeaf(

            NestedHierarchyRoot[] nestedArray,
            List<NestedHierarchyRoot> nestedList

    ) implements SecondHierarchyRoot {

            @Override
            public SecondHierarchyType getType() {
                return SecondHierarchyType.LEAF;
            }
        }

    record NestedHierarchyLeaf() implements NestedHierarchyRoot {

        @Override
        public NestedHierarchyType getType() {
            return NestedHierarchyType.LEAF;
        }
    }
}
