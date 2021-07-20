package com.infobip.typescript.custom.validation.extractor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidatorsNameExtractor {

    private static final String DESTINATION_BASE_PATH = "validators";
    private static final String SUPPORTED_EXTENSIONS_REGEX = ".ts$|.tsx$";
    private static final Pattern SUPPORTED_EXTENSIONS_PATTERN = Pattern.compile(SUPPORTED_EXTENSIONS_REGEX);

    private final List<Path> validatorPaths;

    public ValidatorsNameExtractor(List<Path> validatorPaths) {
        this.validatorPaths = validatorPaths;
    }

    public List<ValidatorName> extract() {
        return validatorPaths.stream()
                             .flatMap(this::walk)
                             .filter(path -> SUPPORTED_EXTENSIONS_PATTERN.matcher(path.getFileName().toString()).find())
                             .map(this::convert)
                             .collect(Collectors.toList());
    }

    private Stream<Path> walk(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                Stream.of(Paths.get(".", DESTINATION_BASE_PATH, path.getFileName().toString()));
            }

            return Files.walk(path)
                        .filter(Files::isRegularFile)
                        .map(filePath -> Paths.get(".", DESTINATION_BASE_PATH)
                                              .resolve(filePath.subpath(path.getNameCount(), filePath.getNameCount())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ValidatorName convert(Path path) {
        String name = path.toFile().getName().replace(SUPPORTED_EXTENSIONS_REGEX, "");
        return new ValidatorName(name, path);
    }
}
