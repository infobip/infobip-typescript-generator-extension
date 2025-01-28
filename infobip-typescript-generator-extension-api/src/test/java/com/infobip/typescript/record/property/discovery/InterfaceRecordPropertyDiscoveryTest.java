package com.infobip.typescript.record.property.discovery;

import com.infobip.typescript.TestBase;
import com.infobip.typescript.type.JsonTypeExtension;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class InterfaceRecordPropertyDiscoveryTest extends TestBase {

    InterfaceRecordPropertyDiscoveryTest() {
        super(new JsonTypeExtension(Stream::empty),
              Collections.emptyList());
    }

    @Test
    void shouldAddReadonlyTypeField() {

        // when
        String actual = whenGenerate(Input.from(HierarchyRoot.class,
                                                FirstLeaf.class));

        // then
        then(actual).isEqualTo(
                """

                        export interface HierarchyRoot {
                            value: string;
                        }

                        export class FirstLeaf implements HierarchyRoot {
                            value: string;
                        }
                        """);
    }

    sealed interface HierarchyRoot permits FirstLeaf {
//        @JsonGetter
        String value();
    }

    record FirstLeaf(String value) implements HierarchyRoot {
    }
}
