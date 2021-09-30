package com.infobip.typescript;

import com.infobip.typescript.custom.validation.AnnotationExtractor;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecoratorsExtractor;
import com.infobip.typescript.transformer.ClassTransformerDecoratorExtension;
import com.infobip.typescript.type.JsonTypeExtension;
import com.infobip.typescript.validation.ClassValidatorDecoratorExtension;
import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.emitter.EmitterExtension;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.infobip.typescript.validation.CommonValidationMessages.COMMON_VALIDATION_MESSAGES_CLASS_NAME;
import static com.infobip.typescript.validation.CommonValidationMessages.COMMON_VALIDATION_MESSAGES_FILE_NAME;
import static com.infobip.typescript.validation.CommonValidationMessages.COMMON_VALIDATION_MESSAGES_SOURCE_CODE;
import static com.infobip.typescript.validation.Localization.LOCALIZATION_CLASS_NAME;
import static com.infobip.typescript.validation.Localization.LOCALIZATION_FILE_NAME;
import static com.infobip.typescript.validation.Localization.LOCALIZATION_SOURCE_CODE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public abstract class TypeScriptFileGenerator {

    private final Path basePath;
    private final Optional<AnnotationExtractor> annotationExtractor;
    private final List<TSCustomDecorator> tsCustomDecorators;

    protected TypeScriptFileGenerator(Path basePath) {
        this.basePath = basePath;
        this.annotationExtractor = getCustomValidationSettings().map(settings -> new AnnotationExtractor(settings.getRootPackage()));
        this.tsCustomDecorators = new TSCustomDecoratorsExtractor(getDecoratorBasePath().map(path -> Collections.singletonList(path))
                                                                                        .orElse(Collections.emptyList())).extract();
    }

    public void generate() {
        List<EmitterExtension> extensions = createExtensions();
        OrderedTypescriptGenerator generator = createGenerator(extensions);
        String code = generateTypeScript(generator, extensions);
        Path filePath = createFilePath();

        try {
            Files.createDirectories(filePath.getParent());
            writeFiles(code, filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void writeFiles(String code, Path filePath) throws
        IOException {

        writeGeneratedTypeScriptFile(code, filePath);
        writeCommonValidationMessagesTypeScriptFile(code, filePath);
        writeLocalization(code, filePath);
        writeCustomValidators(filePath.getParent());
    }

    protected void writeGeneratedTypeScriptFile(String code, Path filePath) {
        write(filePath, code);
    }

    protected void writeCommonValidationMessagesTypeScriptFile(String code, Path filePath) {
        if (code.contains(COMMON_VALIDATION_MESSAGES_CLASS_NAME)) {
            write(filePath.getParent().resolve(COMMON_VALIDATION_MESSAGES_FILE_NAME),
                  COMMON_VALIDATION_MESSAGES_SOURCE_CODE);
        }
    }

    protected void writeLocalization(String code, Path filePath) {
        if (code.contains(LOCALIZATION_CLASS_NAME)) {
            write(filePath.getParent().resolve(LOCALIZATION_FILE_NAME), LOCALIZATION_SOURCE_CODE);
        }
    }

    protected void writeCustomValidators(Path basePath) {
        tsCustomDecorators.forEach(decorator -> {
            Path destination = basePath.resolve(decorator.getDestinationPath());
            copy(decorator.getSourcePath(), destination);
        });
    }

    protected void copy(Path source, Path destination) {
        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void write(Path path, String code) {
        try {
            Files.write(path, code.trim().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String generateTypeScript(OrderedTypescriptGenerator generator,
                                        List<EmitterExtension> extensions) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        String generatedCode = generator.generateTypeScript(getInput());
        return addMissingImports(generatedCode, extensions);
    }

    protected String addMissingImports(String generatedCode,
                                       List<EmitterExtension> extensions) {

        String imports = extensions.stream()
                                   .filter(extension -> extension instanceof TypeScriptImportResolver)
                                   .map(extension -> (TypeScriptImportResolver) extension)
                                   .flatMap(resolver -> resolver.resolve(generatedCode).stream())
                                   .collect(Collectors.joining(System.lineSeparator()));

        if (!imports.isEmpty()) {
            List<String> codeLines = new ArrayList<>(Arrays.asList(generatedCode.split(System.lineSeparator())));
            codeLines.add(2, imports);
            return codeLines.stream().collect(Collectors.joining(System.lineSeparator()));
        }

        return generatedCode;
    }

    protected List<EmitterExtension> createExtensions() {
        return Stream.of(new JsonTypeExtension(),
                         new ClassTransformerDecoratorExtension(),
                         new ClassValidatorDecoratorExtension("validations", tsCustomDecorators,
                                                              annotationExtractor.map(AnnotationExtractor::extract).orElse(Collections.emptyList())))
                     .collect(Collectors.toList());
    }

    protected OrderedTypescriptGenerator createGenerator(List<EmitterExtension> extensions) {
        Settings settings = new Settings();
        settings.outputKind = TypeScriptOutputKind.module;
        settings.jsonLibrary = JsonLibrary.jackson2;
        settings.mapEnum = EnumMapping.asEnum;
        settings.nonConstEnums = true;
        settings.mapClasses = ClassMapping.asClasses;
        settings.extensions = extensions;
        settings.outputFileType = TypeScriptFileType.implementationFile;
        settings.noFileComment = true;
        settings.setStringQuotes(StringQuotes.singleQuotes);
        Settings customizedSettings = customizeSettings(settings);
        TypeScriptGenerator generator = new TypeScriptGenerator(customizedSettings);
        return new OrderedTypescriptGenerator(generator);
    }

    protected Settings customizeSettings(Settings settings) {
        return settings;
    }

    protected Path createFilePath() {
        return outputFilePath(basePath);
    }

    protected Path getBasePath() { return basePath; }

    protected Optional<Path> getDecoratorBasePath() { return Optional.empty(); }

    protected abstract Input getInput();

    protected abstract Path outputFilePath(Path basePath);

    protected Optional<CustomValidationSettings> getCustomValidationSettings() { return Optional.empty(); };

}
