package com.infobip.typescript.showcase.custom.complex;

import com.infobip.typescript.CustomValidationSettings;
import com.infobip.typescript.TypeScriptFileGenerator;
import com.infobip.typescript.showcase.custom.complex.validation.Foo;
import cz.habarta.typescript.generator.Input;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ComplexValidationTypeScriptFileGenerator extends TypeScriptFileGenerator {

    public ComplexValidationTypeScriptFileGenerator(Path basePath) {
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
            return lib.resolve("ComplexValidation.ts");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected CustomValidationSettings getCustomValidationSettings() {
        String rootPackage = "com.infobip.typescript";
        return new CustomValidationSettings(rootPackage);

    }

    @Override
    protected Path getDecoratorBasePath() {
        return getBasePath().getParent().getParent().resolve("src/main/typescript/decorators");
    }
}
