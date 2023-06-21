package com.infobip.typescript.type;

import com.infobip.jackson.TypeProvider;
import com.infobip.jackson.dynamic.DynamicHierarchyDeserializer;
import com.infobip.jackson.dynamic.JsonValueToJavaTypeJacksonMapping;
import com.infobip.typescript.TestBase;
import com.infobip.typescript.TypeScriptFileGenerator;
import cz.habarta.typescript.generator.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class JsonTypeExtensionMultiModuleDynamicHierarchyTest extends TestBase {

    JsonTypeExtensionMultiModuleDynamicHierarchyTest() {
        super(new JsonTypeExtension(
                      () -> Stream.of(new DynamicHierarchyDeserializer<>(DynamicHierarchyRootWithEnum.class,
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
        var infoJsonFile = basePath.getParent()
                                   .getParent()
                                   .resolve("target")
                                   .resolve("tmp")
                                   .resolve("DynamicHierarchy.json")
                                   .toFile();
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
                """
                                        
                        import * as DynamicHierarchy from "a";

                        export class DynamicLeafWithEnum implements DynamicHierarchy.DynamicHierarchyRootWithEnum {
                            value: string;
                            readonly type: DynamicHierarchy.DynamicHierarchyRootWithEnumType = DynamicHierarchy.DynamicHierarchyRootWithEnumType.LEAF;
                        }
                        """);
    }

    interface DynamicHierarchyRootWithEnum {

        DynamicHierarchyRootWithEnumType getType();
    }

    record DynamicLeafWithEnum(String value) implements DynamicHierarchyRootWithEnum {

        public DynamicHierarchyRootWithEnumType getType() {
            return DynamicHierarchyRootWithEnumType.LEAF;
        }
    }

    enum DynamicHierarchyRootWithEnumType implements TypeProvider<DynamicHierarchyRootWithEnum> {
        LEAF(DynamicLeafWithEnum.class);

        private final Class<? extends DynamicHierarchyRootWithEnum> type;

        DynamicHierarchyRootWithEnumType(Class<? extends DynamicHierarchyRootWithEnum> type) {
            this.type = type;
        }

        @Override
        public Class<? extends DynamicHierarchyRootWithEnum> getType() {
            return type;
        }
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
