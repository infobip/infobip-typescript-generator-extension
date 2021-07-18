package com.infobip.typescript.custom.validation.extractor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidatorsNameExtractor {

    private final List<Path> validatorPaths;

    public ValidatorsNameExtractor(List<Path> validatorPaths) {
        this.validatorPaths = validatorPaths;
    }

    public List<String> extract() {
        return validatorPaths.stream()
                             .flatMap(this::walk)
                             .map(path -> path.toFile().getName())
                             .collect(Collectors.toList());
    }

    private Stream<Path> walk(Path path) {
        try {
            return Files.walk(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
