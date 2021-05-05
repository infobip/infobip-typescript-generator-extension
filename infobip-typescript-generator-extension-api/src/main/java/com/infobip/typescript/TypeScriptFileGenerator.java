package com.infobip.typescript;

import com.infobip.typescript.transformer.ClassTransformerDecoratorExtension;
import com.infobip.typescript.type.JsonTypeExtension;
import com.infobip.typescript.validation.ClassValidatorDecoratorExtension;
import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.emitter.EmitterExtension;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TypeScriptFileGenerator {

    private final Path basePath;

    protected TypeScriptFileGenerator(Path basePath) {
        this.basePath = basePath;
    }

    public void generate() {
        List<EmitterExtension> extensions = createExtensions();
        OrderedTypescriptGenerator generator = createGenerator(extensions);
        String code = generateTypeScript(generator, extensions);
        Path path = createFilePath();

        try {
            Files.write(path, code.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String generateTypeScript(OrderedTypescriptGenerator generator,
                                      List<EmitterExtension> extensions) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        String generatedCode = generator.generateTypeScript(getInput())
                                        .replaceAll("\\r", "")
                                        .replaceAll("\\n$", "");

        return addMissingImports(generatedCode, extensions);
    }

    private String addMissingImports(String generatedCode,
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

    public List<EmitterExtension> createExtensions() {
        return Stream.of(new JsonTypeExtension(),
                         new ClassTransformerDecoratorExtension(),
                         new ClassValidatorDecoratorExtension("validations"))
                     .collect(Collectors.toList());
    }

    public OrderedTypescriptGenerator createGenerator(List<EmitterExtension> extensions) {
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

    public Settings customizeSettings(Settings settings) {
        return settings;
    }

    public Path createFilePath() {
        return outputFilePath(basePath);
    }

    public abstract Input getInput();

    public abstract Path outputFilePath(Path basePath);
}
