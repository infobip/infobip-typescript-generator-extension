package com.infobip.typescript.showcase.mapping;

import com.infobip.typescript.showcase.TestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.BDDAssertions.then;

class MappingTypeScriptFileGeneratorTest extends TestBase {

    @Test
    void shouldGenerateTypeScript() throws IOException {
        // when
        String actual = whenActualFileIsGenerated(Paths.get(".", "dist", "Mapping.ts"));

        // then
        then(actual).isEqualTo("""
                                   /* tslint:disable */
                                   /* eslint-disable */

                                   export class Foo {
                                       instant: string;
                                       localDateTime: string;
                                       zonedDateTime: string;
                                       duration: string;
                                   }""");
    }

}
