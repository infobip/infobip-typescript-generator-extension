package com.infobip.typescript.showcase.custom.complex;

import com.infobip.typescript.CustomValidationSettings;
import com.infobip.typescript.TypeScriptFileGenerator;
import com.infobip.typescript.showcase.custom.complex.validation.Foo;
import com.infobip.typescript.showcase.hierarchy.exception.UncheckedURISyntaxException;
import cz.habarta.typescript.generator.Input;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

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
        Path lib = basePath.getParent().getParent().resolve("dist");

        try {
            Files.createDirectories(lib);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return lib.resolve("ComplexValidation.ts");
    }

    @Override
    protected CustomValidationSettings getCustomValidationSettings() {
        String rootPackage = "com.infobip.typescript";
        List<Path> customValidatorsPaths = Collections.singletonList(validatorsPath());
        return new CustomValidationSettings(rootPackage, customValidatorsPaths);

    }

    private Path validatorsPath() {
        try {
            Path sourcePath = Paths.get(ComplexValidationTypeScriptFileGenerator.class.getProtectionDomain()
                                                                                      .getCodeSource()
                                                                                      .getLocation()
                                                                                      .toURI());
            return Files.isRegularFile(sourcePath)
                    ? sourcePath.getParent().resolve("classes/validators")
                    : sourcePath.resolve("validators");
        } catch (URISyntaxException e) {
            throw new UncheckedURISyntaxException(e);
        }
    }
}
