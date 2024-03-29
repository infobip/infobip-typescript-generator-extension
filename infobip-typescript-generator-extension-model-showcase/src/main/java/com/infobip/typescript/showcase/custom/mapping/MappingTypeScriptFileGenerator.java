package com.infobip.typescript.showcase.custom.mapping;

import com.infobip.typescript.CustomValidationSettings;
import com.infobip.typescript.TypeScriptFileGenerator;
import com.infobip.typescript.showcase.mapping.Foo;
import cz.habarta.typescript.generator.Input;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class MappingTypeScriptFileGenerator extends TypeScriptFileGenerator {

    public MappingTypeScriptFileGenerator(Path basePath) {
        super(basePath);
    }

    @Override
    public Input getInput() {
        return Input.from(Foo.class);
    }

    @Override
    public Path outputFilePath(Path basePath) {
        try {
            Path lib = basePath.getParent().getParent().resolve("dist");
            Files.createDirectories(lib);
            return lib.resolve("Mapping.ts");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected Optional<CustomValidationSettings> getCustomValidationAnnotationSettings() {
        String rootPackage = "com.infobip.typescript";
        return Optional.of(new CustomValidationSettings(rootPackage));

    }

    @Override
    protected Optional<Path> getCustomDecoratorBasePath() {
        return Optional.of(getBasePath().getParent().getParent().resolve("src/main/typescript/decorators"));
    }
}
