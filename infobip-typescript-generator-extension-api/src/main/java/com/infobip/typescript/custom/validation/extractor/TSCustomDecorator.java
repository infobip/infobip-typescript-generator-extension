package com.infobip.typescript.custom.validation.extractor;

import java.nio.file.Path;

public class TSCustomDecorator {

    private final String name;
    private final Path sourcePath;
    private final Path destinationPath;
    private final Path tsPath;

    public TSCustomDecorator(String name,Path sourcePath, Path destinationPath, Path tsPath) {
        this.name = name;
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.tsPath = tsPath;
    }

    public String getName() {
        return name;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public Path getDestinationPath() {
        return destinationPath;
    }

    public Path getTsPath() {
        return tsPath;
    }
}
