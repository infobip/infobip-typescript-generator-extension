package com.infobip.typescript.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.infobip.jackson.TypeProvider;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import com.infobip.typescript.TypeScriptFileGenerator;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.ModuleDependency;
import cz.habarta.typescript.generator.Settings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.Test;

class JsonTypeExtensionMultiModuleDynamicHierarchyTest extends TestBase {

    JsonTypeExtensionMultiModuleDynamicHierarchyTest() {
        super(new JsonTypeExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(DynamicHierarchyRootWithEnum.class,
                                                                                       List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                           DynamicHierarchyRootWithEnumType.LEAF,
                                                                                           DynamicLeafWithEnum.class))))),
              Collections.emptyList(),
              createSettings());
    }

    private static Settings createSettings() {
        var basePath = Path.of(JsonTypeExtensionMultiModuleDynamicHierarchyTest.class.getProtectionDomain()
                                                                                     .getCodeSource()
                                                                                     .getLocation()
                                                                                     .getPath());
        var infoJsonFile = basePath.getParent().getParent().resolve("target").resolve("tmp").resolve("DynamicHierarchy.json").toFile();
        new CustomTypeScriptFileGenerator(basePath).generateInfoJson(infoJsonFile);

        var module = ModuleDependency.module(
            "a",
            "DynamicHierarchy",
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
        String actual = whenGenerate(Input.from(DynamicLeafWithEnum.class));

        // then
        then(actual).isEqualTo(
            "\n" +
            "import * as DynamicHierarchy from \"a\";\n" +
            "\n" +
            "export class DynamicLeafWithEnum implements DynamicHierarchy.DynamicHierarchyRootWithEnum {\n" +
            "    value: string;\n" +
            "    readonly type: DynamicHierarchy.DynamicHierarchyRootWithEnumType = DynamicHierarchy.DynamicHierarchyRootWithEnumType.LEAF;\n" +
            "}\n");
    }

    interface DynamicHierarchyRootWithEnum {

        DynamicHierarchyRootWithEnumType getType();

    }

    @Value
    static class DynamicLeafWithEnum implements DynamicHierarchyRootWithEnum {

        private final String value;

        public DynamicHierarchyRootWithEnumType getType() {
            return DynamicHierarchyRootWithEnumType.LEAF;
        }

    }

    @Getter
    @AllArgsConstructor
    enum DynamicHierarchyRootWithEnumType implements TypeProvider<DynamicHierarchyRootWithEnum> {
        LEAF(DynamicLeafWithEnum.class);

        private final Class<? extends DynamicHierarchyRootWithEnum> type;
    }

    static class CustomTypeScriptFileGenerator extends TypeScriptFileGenerator {

        protected CustomTypeScriptFileGenerator(Path basePath) {
            super(basePath);
        }

        @Override
        protected Input getInput() {
            return Input.from(DynamicHierarchyRootWithEnum.class);
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
