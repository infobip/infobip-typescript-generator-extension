package com.infobip.typescript.showcase.hierarchy;

import com.infobip.typescript.CustomValidationSettings;
import com.infobip.typescript.TypeScriptFileGenerator;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Settings;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HierarchyTypeScriptFileGenerator extends TypeScriptFileGenerator {

    public HierarchyTypeScriptFileGenerator(Path basePath) {
        super(basePath);
    }

    @Override
    public Input getInput() {
        Input.Parameters parameters = new Input.Parameters();
        parameters.classNamePatterns = Collections.singletonList(
                "com.infobip.typescript.showcase.hierarchy.message.**");
        return Input.from(parameters);
    }

    @Override
    public Settings customizeSettings(Settings settings) {
        settings.setExcludeFilter(Collections.emptyList(),
                                  Arrays.asList("com.infobip.jackson.**",
                                                "**Resolver",
                                                "**Visitor",
                                                "**JsonDeserializer"));
        return settings;
    }

    @Override
    public Path outputFilePath(Path basePath) {
        Path lib = basePath.getParent().getParent().resolve("dist");

        try {
            Files.createDirectories(lib);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return lib.resolve("Hierarchy.ts");
    }

    @Override
    protected CustomValidationSettings getCustomValidationSettings() {
        List<String> customValidationNamePatterns = Collections.singletonList(
                "com.infobip.typescript.showcase.custom.validation.**");
        List<String> customValidationPackages = Collections.singletonList(
                "com.infobip.typescript.showcase.custom.validation");
        return new CustomValidationSettings(customValidationNamePatterns, customValidationPackages);
    }
}
