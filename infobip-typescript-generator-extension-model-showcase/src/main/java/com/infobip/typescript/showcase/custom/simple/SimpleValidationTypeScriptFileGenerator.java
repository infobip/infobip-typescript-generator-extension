package com.infobip.typescript.showcase.custom.simple;

import com.infobip.typescript.CustomValidationSettings;
import com.infobip.typescript.TypeScriptFileGenerator;
import com.infobip.typescript.showcase.custom.simple.validation.Foo;
import cz.habarta.typescript.generator.Input;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SimpleValidationTypeScriptFileGenerator extends TypeScriptFileGenerator {

    public SimpleValidationTypeScriptFileGenerator(Path basePath) {
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
            return lib.resolve("SimpleValidation.ts");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected Optional<CustomValidationSettings> getCustomValidationSettings() {
        String rootPackage = "com.infobip.typescript";
        return Optional.of(new CustomValidationSettings(rootPackage));

    }

    @Override
    protected Optional<Path> getDecoratorBasePath() {
        return Optional.of(getBasePath().getParent().getParent().resolve("src/main/typescript/decorators"));
    }
}
