package com.infobip.typescript.showcase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public abstract class TestBase {

    protected String whenActualFileIsGenerated(Path path) throws IOException {
        return Files.lines(path)
                    .collect(Collectors.joining(System.lineSeparator()))
                    .replace("\r\n", "\n");
    }

}
