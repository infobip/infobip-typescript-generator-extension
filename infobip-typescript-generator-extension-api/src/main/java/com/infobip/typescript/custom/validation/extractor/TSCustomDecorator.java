package com.infobip.typescript.custom.validation.extractor;

import java.nio.file.Path;

public class TSCustomDecorator {

    private final String name;
    private final Path path;
    private final Path tsPath;

    public TSCustomDecorator(String name, Path path, Path tsPath) {
        this.name = name;
        this.path = path;
        this.tsPath = tsPath;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public Path getTsPath() {
        return tsPath;
    }
}
