package com.infobip.typescript.showcase.simple;

import com.infobip.typescript.TypeScriptFileGenerator;
import cz.habarta.typescript.generator.Input;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;

public class BasicTypeScriptFileGenerator extends TypeScriptFileGenerator {

    public BasicTypeScriptFileGenerator(Path basePath) {
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

        return lib.resolve("Simple.ts");
    }
}
