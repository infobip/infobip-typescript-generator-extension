package com.infobip.typescript.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.stream.Stream;

import com.infobip.jackson.PresentPropertyJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class JsonTypeExtensionPresentPropertyHierarchyTest extends TestBase {

    JsonTypeExtensionPresentPropertyHierarchyTest() {
        super(new JsonTypeExtension(Stream::empty),
              Collections.emptyList());
    }

    @Test
    void shouldAllowPresentPropertyHierarchies() {

        // when
        String actual = whenGenerate(Input.from(PresentPropertyHierarchyRoot.class,
                                                One.class,
                                                Two.class));

        // then
        then(actual).isEqualTo(
            "\n" +
            "export interface PresentPropertyHierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class One implements PresentPropertyHierarchyRoot {\n" +
            "}\n" +
            "\n" +
            "export class Two implements PresentPropertyHierarchyRoot {\n" +
            "}\n");
    }

    @Getter
    @AllArgsConstructor
    enum PresentPropertyHierarchyType implements TypeProvider<PresentPropertyHierarchyRoot> {
        ONE(One.class),
        TWO(Two.class);

        private final Class<? extends PresentPropertyHierarchyRoot> type;
    }

    interface PresentPropertyHierarchyRoot extends PresentPropertyJsonHierarchy<PresentPropertyHierarchyType> {

    }

    static class One implements PresentPropertyHierarchyRoot {

    }

    static class Two implements PresentPropertyHierarchyRoot {

    }

}
