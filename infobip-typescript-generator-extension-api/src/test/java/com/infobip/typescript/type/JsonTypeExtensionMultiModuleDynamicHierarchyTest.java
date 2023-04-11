package com.infobip.typescript.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.infobip.jackson.SimpleJsonHierarchy;
import com.infobip.jackson.TypeProvider;
import com.infobip.typescript.TestBase;
import com.infobip.typescript.TypeScriptFileGenerator;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.ModuleDependency;
import cz.habarta.typescript.generator.Settings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class JsonTypeExtensionMultiModuleDynamicHierarchyTest extends TestBase {

    JsonTypeExtensionMultiModuleDynamicHierarchyTest() {
        super(new JsonTypeExtension(Stream::empty),
              Collections.emptyList(),
              createSettings());
    }

    private static Settings createSettings() {
        var basePath = Path.of(JsonTypeExtensionMultiModuleDynamicHierarchyTest.class.getProtectionDomain()
                                                                                     .getCodeSource()
                                                                                     .getLocation()
                                                                                     .getPath());
        var infoJsonFile = basePath.getParent().getParent().resolve("target").resolve("tmp").resolve("A.json").toFile();
        new CustomTypeScriptFileGenerator(basePath).generateInfoJson(infoJsonFile);

        var module = ModuleDependency.module(
            "a",
            "A",
            infoJsonFile,
            null,
            null);
        var settings = new Settings();
        settings.moduleDependencies = List.of(module);
        return settings;
    }

    @Test
    void shouldAddReadonlyTypeFieldForDynamicHierarchy() {

        // when
        String actual = whenGenerate(Input.from(FirstLeaf.class,
                                                SecondLeaf.class));

        // then
        then(actual).isEqualTo(
            """
                                
                import * as A from "a";

                export class FirstLeaf implements A.HierarchyRoot {
                    readonly type: A.HierarchyType = A.HierarchyType.FIRST_LEAF;
                }

                export class SecondLeaf implements A.HierarchyRoot {
                    readonly type: A.HierarchyType = A.HierarchyType.SECOND_LEAF;
                }
                """);
    }

    @Getter
    @AllArgsConstructor
    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        FIRST_LEAF(FirstLeaf.class),
        SECOND_LEAF(SecondLeaf.class);

        private final Class<? extends HierarchyRoot> type;
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

    static class CustomTypeScriptFileGenerator extends TypeScriptFileGenerator {

        protected CustomTypeScriptFileGenerator(Path basePath) {
            super(basePath);
        }

        @Override
        protected Input getInput() {
            return Input.from(HierarchyRoot.class);
        }

        @Override
        protected Path outputFilePath(Path basePath) {
            return null;
        }

        @Override
        protected Settings customizeSettings(Settings settings) {
            settings.setExcludeFilter(List.of(), List.of("com.infobip.jackson**"));
            return settings;
        }

    }

}
