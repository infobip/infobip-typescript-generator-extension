package com.infobip.typescript.transformer;

import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class DynamicHierarchyClassTransformerDecoratorExtensionTest extends TestBase {

    DynamicHierarchyClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(HierarchyRoot.class,
                                                                                                        List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                                            "LEAF",
                                                                                                            HierarchyLeaf.class))))),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(HierarchyRoot.class,
                                                HierarchyLeaf.class,
                                                HierarchyContainer.class));

        // then
        then(fixNewlines(actual)).isEqualTo(
            """
                import { Type } from 'class-transformer';
                                
                export interface HierarchyRoot {
                }
                                
                export class HierarchyLeaf implements HierarchyRoot {
                }
                                
                export class HierarchyContainer {
                    @Type(() => Object, {
                        discriminator: {
                            property: "type", subTypes: [
                                { value: HierarchyLeaf, name: "LEAF" }
                            ]
                        }
                    })
                    root: HierarchyRoot;
                }""");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    interface HierarchyRoot {

    }

    record HierarchyLeaf() implements HierarchyRoot {

    }

    record HierarchyContainer(HierarchyRoot root) {

    }

}
