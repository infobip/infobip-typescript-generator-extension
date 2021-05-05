package com.infobip.typescript.showcase.hierarchy;

import com.infobip.typescript.TypeScriptFileGenerator;
import com.infobip.typescript.showcase.hierarchy.content.CommonContent;
import cz.habarta.typescript.generator.Input;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class HierarchyTypeScriptFileGenerator extends TypeScriptFileGenerator {

    public HierarchyTypeScriptFileGenerator(Path basePath) {
        super(basePath);
    }

    @Override
    public Input getInput() {
        Input.Parameters parameters = new Input.Parameters();
        parameters.classNamePatterns = Collections.singletonList("com.infobip.typescript.showcase.hierarchy.**");
        return Input.from(parameters);
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
}
