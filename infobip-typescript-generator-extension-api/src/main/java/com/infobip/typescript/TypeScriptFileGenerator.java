package com.infobip.typescript;

import com.infobip.typescript.transformer.ClassTransformerDecoratorExtension;
import com.infobip.typescript.type.JsonTypeExtension;
import com.infobip.typescript.validation.ClassValidatorDecoratorExtension;
import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.emitter.EmitterExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.infobip.typescript.validation.ClassValidatorDecoratorExtension.COMMON_VALIDATION_MESSAGES;
import static com.infobip.typescript.validation.ClassValidatorDecoratorExtension.COMMON_VALIDATION_MESSAGES_FILE_NAME;

public abstract class TypeScriptFileGenerator {

    private final Path basePath;

    protected TypeScriptFileGenerator(Path basePath) {
        this.basePath = basePath;
    }

    public void generate() {
        List<EmitterExtension> extensions = createExtensions();
        OrderedTypescriptGenerator generator = createGenerator(extensions);
        String code = generateTypeScript(generator, extensions);
        Path filePath = createFilePath();

        try {
            writeFiles(code, filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void writeFiles(String code, Path filePath) throws IOException {

        Files.write(filePath, code.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
        writeCommonValidationMessagesFile(code, filePath);
    }

    protected void writeCommonValidationMessagesFile(String code, Path filePath) throws IOException {
        System.out.println(1);
        String commonValidationMessagesContent = getCommonValidationMessagesCode()
                .orElseThrow(
                        () -> new IllegalStateException("Resource not found: " + COMMON_VALIDATION_MESSAGES_FILE_NAME));

        System.out.println(2);
        if (code.contains(COMMON_VALIDATION_MESSAGES)) {
            Files.copy(new ByteArrayInputStream(commonValidationMessagesContent.getBytes(StandardCharsets.UTF_8)),
                       filePath.getParent().resolve(COMMON_VALIDATION_MESSAGES_FILE_NAME),
                       StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println(3);
    }

    protected String generateTypeScript(OrderedTypescriptGenerator generator,
                                        List<EmitterExtension> extensions) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        String generatedCode = generator.generateTypeScript(getInput())
                                        .replaceAll("\\r", "")
                                        .replaceAll("\\n$", "");

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
                         new ClassValidatorDecoratorExtension("validations"))
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

    protected abstract Input getInput();

    protected abstract Path outputFilePath(Path basePath);

    protected Optional<String> getCommonValidationMessagesCode() throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(COMMON_VALIDATION_MESSAGES_FILE_NAME)) {
            if (inputStream == null) {
                return Optional.empty();
            }
            try (InputStreamReader isr = new InputStreamReader(inputStream);
                 BufferedReader reader = new BufferedReader(isr)) {
                return Optional.of(reader.lines().collect(Collectors.joining(System.lineSeparator())));
            }
        }
    }
}
