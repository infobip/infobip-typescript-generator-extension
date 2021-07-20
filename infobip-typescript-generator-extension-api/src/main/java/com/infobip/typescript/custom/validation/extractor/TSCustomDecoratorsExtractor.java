package com.infobip.typescript.custom.validation.extractor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TSCustomDecoratorsExtractor {

    private static final String DESTINATION_BASE_PATH = "validators";
    private static final String SUPPORTED_EXTENSIONS_REGEX = ".ts$|.tsx$";
    private static final Pattern SUPPORTED_EXTENSIONS_PATTERN = Pattern.compile(SUPPORTED_EXTENSIONS_REGEX);

    private final List<Path> validatorPaths;

    public TSCustomDecoratorsExtractor(List<Path> validatorPaths) {
        this.validatorPaths = validatorPaths;
    }

    public List<TSCustomDecorator> extract() {
        return validatorPaths.stream()
                             .flatMap(this::walk)
                             .filter(decorator -> SUPPORTED_EXTENSIONS_PATTERN.matcher(
                                     decorator.getSource().getFileName().toString()).find())
                             .map(this::convert)
                             .collect(Collectors.toList());
    }

    private Stream<TSDecoratorPath> walk(Path path) {
        //TODO refactor this
        try {
            if (Files.isRegularFile(path)) {
                Path destination = getDestinationPath(path.getFileName());
                return Stream.of(new TSDecoratorPath(path, destination));
            }

            return Files.walk(path)
                        .filter(Files::isRegularFile)
                        .map(filePath -> {
                            Path subPath = filePath.subpath(path.getNameCount(), filePath.getNameCount());
                            Path destination = getDestinationPath(subPath);

                            return new TSDecoratorPath(filePath, destination);
                        });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getDestinationPath(Path path) {
        return Paths.get(".", DESTINATION_BASE_PATH).resolve(path);
    }

    private TSCustomDecorator convert(TSDecoratorPath decoratorPath) {
        String name = decoratorPath.getSource().toFile().getName().replaceAll(SUPPORTED_EXTENSIONS_REGEX, "");
        Path tsPath = Paths.get(decoratorPath.getDestination().toString().replaceAll(SUPPORTED_EXTENSIONS_REGEX, ""));
        return new TSCustomDecorator(name, decoratorPath.getSource(), decoratorPath.getDestination(), tsPath);
    }

    public static class TSDecoratorPath {
        private final Path source;
        private final Path destination;

        public TSDecoratorPath(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
        }

        public Path getSource() {
            return source;
        }

        public Path getDestination() {
            return destination;
        }
    }
}
