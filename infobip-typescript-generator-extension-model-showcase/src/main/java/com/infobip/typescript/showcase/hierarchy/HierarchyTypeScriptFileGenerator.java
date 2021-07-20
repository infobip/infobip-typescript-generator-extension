package com.infobip.typescript.showcase.hierarchy;

import com.infobip.typescript.CustomValidationSettings;
import com.infobip.typescript.TypeScriptFileGenerator;
import com.infobip.typescript.showcase.hierarchy.exception.UncheckedURISyntaxException;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Settings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

public class HierarchyTypeScriptFileGenerator extends TypeScriptFileGenerator {

    public HierarchyTypeScriptFileGenerator(Path basePath) {
        super(basePath);
    }

    @Override
    public Input getInput() {
        Input.Parameters parameters = new Input.Parameters();
        parameters.classNamePatterns = Collections.singletonList(
                "com.infobip.typescript.showcase.custom.validation.**");
        return Input.from(parameters);
    }

    @Override
    public Settings customizeSettings(Settings settings) {
        settings.setExcludeFilter(Collections.emptyList(),
                                  Arrays.asList("com.infobip.jackson.**",
                                                "**ParameterListExtractorImpl",
                                                "**Validation",
                                                "**Validator",
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
        try {
            String rootPackage = "com.infobip.typescript";
            Path validatorsPath = Paths.get(this.getClass().getClassLoader().getResource("validators").toURI());
            List<Path> customValidatorsPaths = Collections.singletonList(validatorsPath);
            return new CustomValidationSettings(rootPackage, customValidatorsPaths);
        } catch (URISyntaxException e) {
            throw new UncheckedURISyntaxException(e);
        }
    }
}
