package com.infobip.typescript.transformer;

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
import lombok.Value;
import org.junit.jupiter.api.Test;

class SimpleJsonHierarchyMultiModuleClassTransformerDecoratorExtensionTest extends TestBase {

    SimpleJsonHierarchyMultiModuleClassTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(Stream::empty),
              Collections.singletonList("import { Type } from 'class-transformer'"),
              createSettings());
    }

    private static Settings createSettings() {
        var basePath = Path.of(SimpleJsonHierarchyMultiModuleClassTransformerDecoratorExtensionTest.class.getProtectionDomain()
                                                                                                         .getCodeSource()
                                                                                                         .getLocation()
                                                                                                         .getPath());
        var infoJsonFile = basePath.getParent().getParent().resolve("target").resolve("tmp").resolve("DynamicHierarchy.json").toFile();
        new SimpleJsonHierarchyMultiModuleClassTransformerDecoratorExtensionTest.CustomTypeScriptFileGenerator(basePath).generateInfoJson(
            infoJsonFile);

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
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(HierarchyContainer.class));

        // then
        then(fixNewlines(actual)).isEqualTo(
            """
                import * as SimpleJsonHierarchy from "a";
                                
                import { Type } from 'class-transformer';
                                
                export class HierarchyContainer {
                    @Type(() => Object, {
                        discriminator: {
                            property: "type", subTypes: [
                                { value: SimpleJsonHierarchy.FirstLeaf, name: HierarchyType.FIRST_LEAF },
                                { value: SimpleJsonHierarchy.SecondLeaf, name: HierarchyType.SECOND_LEAF }
                            ]
                        }
                    })
                    root: SimpleJsonHierarchy.HierarchyRoot;
                }""");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
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

    @Value
    static class HierarchyContainer {

        private final HierarchyRoot root;

    }

    static class CustomTypeScriptFileGenerator extends TypeScriptFileGenerator {

        protected CustomTypeScriptFileGenerator(Path basePath) {
            super(basePath);
        }

        @Override
        protected Input getInput() {
            return Input.from(HierarchyRoot.class,
                              FirstLeaf.class,
                              SecondLeaf.class,
                              HierarchyType.class);
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
