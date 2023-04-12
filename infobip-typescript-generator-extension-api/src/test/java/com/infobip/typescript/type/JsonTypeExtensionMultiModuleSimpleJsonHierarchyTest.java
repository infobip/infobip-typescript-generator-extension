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

class JsonTypeExtensionMultiModuleSimpleJsonHierarchyTest extends TestBase {

    JsonTypeExtensionMultiModuleSimpleJsonHierarchyTest() {
        super(new JsonTypeExtension(Stream::empty),
              Collections.emptyList(),
              createSettings());
    }

    private static Settings createSettings() {
        var basePath = Path.of(JsonTypeExtensionMultiModuleSimpleJsonHierarchyTest.class.getProtectionDomain()
                                                                                        .getCodeSource()
                                                                                        .getLocation()
                                                                                        .getPath());
        var infoJsonFile = basePath.getParent().getParent().resolve("target").resolve("tmp").resolve("SimpleJsonHierarchy.json").toFile();
        new CustomTypeScriptFileGenerator(basePath).generateInfoJson(infoJsonFile);

        var module = ModuleDependency.module(
            "a",
            "SimpleJsonHierarchy",
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
            "\n" +
            "import * as SimpleJsonHierarchy from \"a\";\n" +
            "\n" +
            "export class FirstLeaf implements SimpleJsonHierarchy.HierarchyRoot {\n" +
            "    readonly type: SimpleJsonHierarchy.HierarchyType = SimpleJsonHierarchy.HierarchyType.FIRST_LEAF;\n" +
            "}\n" +
            "\n" +
            "export class SecondLeaf implements SimpleJsonHierarchy.HierarchyRoot {\n" +
            "    readonly type: SimpleJsonHierarchy.HierarchyType = SimpleJsonHierarchy.HierarchyType.SECOND_LEAF;\n" +
            "}\n");
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
