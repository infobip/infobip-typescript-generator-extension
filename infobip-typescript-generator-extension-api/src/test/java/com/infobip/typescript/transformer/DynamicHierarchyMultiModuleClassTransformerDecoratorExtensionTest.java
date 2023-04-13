package com.infobip.typescript.transformer;

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

class DynamicHierarchyMultiModuleClassTransformerDecoratorExtensionTest extends TestBase {

    DynamicHierarchyMultiModuleClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(() -> Stream.of(new DynamicHierarchyDeserializer<>(HierarchyRoot.class,
                                                                                                        List.of(new JsonValueToJavaTypeJacksonMapping<>(
                                                                                                            HierarchyType.LEAF,
                                                                                                            HierarchyLeaf.class))))),
              Collections.singletonList("import { Type } from 'class-transformer'"),
              createSettings());
    }

    private static Settings createSettings() {
        var basePath = Path.of(DynamicHierarchyMultiModuleClassTransformerDecoratorExtensionTest.class.getProtectionDomain()
                                                                                     .getCodeSource()
                                                                                     .getLocation()
                                                                                     .getPath());
        var infoJsonFile = basePath.getParent().getParent().resolve("target").resolve("tmp").resolve("DynamicHierarchy.json").toFile();
        new DynamicHierarchyMultiModuleClassTransformerDecoratorExtensionTest.CustomTypeScriptFileGenerator(basePath).generateInfoJson(infoJsonFile);

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
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(HierarchyContainer.class));

        // then
        then(fixNewlines(actual)).isEqualTo(
            """
                import * as DynamicHierarchy from "a";
                
                import { Type } from 'class-transformer';
                                
                export class HierarchyContainer {
                    @Type(() => Object, {
                        discriminator: {
                            property: "type", subTypes: [
                                { value: DynamicHierarchy.HierarchyLeaf, name: "LEAF" }
                            ]
                        }
                    })
                    root: DynamicHierarchy.HierarchyRoot;
                }""");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    interface HierarchyRoot {

    }

    @Value
    static class HierarchyLeaf implements HierarchyRoot {


    }

    @Value
    static class HierarchyContainer {

        private final HierarchyRoot root;

    }

    @Getter
    @AllArgsConstructor
    enum HierarchyType implements TypeProvider<HierarchyRoot> {
        LEAF(HierarchyLeaf.class);

        private final Class<? extends HierarchyRoot> type;
    }

    static class CustomTypeScriptFileGenerator extends TypeScriptFileGenerator {

        protected CustomTypeScriptFileGenerator(Path basePath) {
            super(basePath);
        }

        @Override
        protected Input getInput() {
            return Input.from(HierarchyRoot.class,
                              HierarchyLeaf.class);
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
