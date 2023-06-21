package com.infobip.typescript.transformer;

import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class HierarchyInsideOptionalTransformerDecoratorExtensionTest extends TestBase {

    HierarchyInsideOptionalTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(Stream::empty),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(HierarchyWrapper.class,
                                                HierarchyRoot.class,
                                                HierarchyLeaf.class));

        // then
        then(fixNewlines(actual)).isEqualTo("""
                                                import { Type } from 'class-transformer';
                                                                         
                                                export enum HierarchyType {
                                                    LEAF = "LEAF",
                                                }
                                                                         
                                                export interface HierarchyRoot {
                                                    type: HierarchyType;
                                                }
                                                                         
                                                export class HierarchyLeaf implements HierarchyRoot {
                                                    type: HierarchyType;
                                                }
                                                                         
                                                export class HierarchyWrapper {
                                                    @Type(() => Object, {
                                                        discriminator: {
                                                            property: "type", subTypes: [
                                                                { value: HierarchyLeaf, name: HierarchyType.LEAF }
                                                            ]
                                                        }
                                                    })
                                                    root?: HierarchyRoot;
                                                }""");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    record HierarchyWrapper(Optional<HierarchyRoot> root) {

    }

    interface HierarchyRoot extends SimpleJsonHierarchy<HierarchyType> {

    }

    record HierarchyLeaf() implements HierarchyRoot {

        @Override
        public HierarchyType getType() {
            return HierarchyType.LEAF;
        }

    }

    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        LEAF(HierarchyLeaf.class);

        private final Class<? extends HierarchyRoot> type;

        HierarchyType(Class<? extends HierarchyRoot> type) {
            this.type = type;
        }

        @Override
        public Class<? extends HierarchyRoot> getType() {
            return type;
        }
    }

}
