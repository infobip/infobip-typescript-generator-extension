package com.infobip.typescript;

import com.infobip.typescript.custom.validation.AnnotationExtractor;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecoratorsExtractor;
import com.infobip.typescript.transformer.ClassTransformerDecoratorExtension;
import com.infobip.typescript.type.JsonTypeExtension;
import com.infobip.typescript.validation.ClassValidatorDecoratorExtension;
import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.emitter.EmitterExtension;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.parser.Model;
import cz.habarta.typescript.generator.util.Utils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.infobip.typescript.validation.CommonValidationMessages.*;
import static com.infobip.typescript.validation.Localization.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public abstract class TypeScriptFileGenerator {

    private final Path basePath;
    private final List<TSCustomDecorator> tsCustomDecorators;

    protected TypeScriptFileGenerator(Path basePath) {
        this.basePath = basePath;
        this.tsCustomDecorators = new TSCustomDecoratorsExtractor(getCustomDecoratorBasePath().map(Collections::singletonList)
                                                                                              .orElse(Collections.emptyList())).extract();
    }

    public void generate() {
        var objectMapper = Utils.getObjectMapper();
        objectMapper.findAndRegisterModules();
        List<EmitterExtension> extensions = createExtensions();
        Settings settings = createSettings(extensions);
        OrderedTypescriptGenerator generator = createGenerator(settings);
        String code = generateTypeScript(generator, extensions, settings);
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
            Files.writeString(path, code.trim());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String generateTypeScript(OrderedTypescriptGenerator generator,
                                        List<EmitterExtension> extensions,
                                        Settings settings) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        String generatedCode = generator.generateTypeScript(getInput());
        return addMissingImports(generatedCode, extensions, settings);
    }

    protected String addMissingImports(String generatedCode,
                                       List<EmitterExtension> extensions,
                                       Settings settings) {

        String imports = extensions.stream()
                                   .filter(extension -> extension instanceof TypeScriptImportResolver)
                                   .map(extension -> (TypeScriptImportResolver) extension)
                                   .flatMap(resolver -> resolver.resolve(generatedCode).stream())
                                   .collect(Collectors.joining(settings.newline));

        if (!imports.isEmpty()) {
            List<String> codeLines = new ArrayList<>(Arrays.asList(generatedCode.split(settings.newline)));
            codeLines.add(2, imports);
            return codeLines.stream().collect(Collectors.joining(settings.newline));
        }

        return generatedCode;
    }

    protected List<EmitterExtension> createExtensions() {
        return Stream.of(new JsonTypeExtension(),
                         new ClassTransformerDecoratorExtension(),
                         new ClassValidatorDecoratorExtension("validations", tsCustomDecorators, getCustomAnnotations()))
                     .collect(Collectors.toList());
    }

    protected Settings createSettings(List<EmitterExtension> extensions) {
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
        settings.customTypeMappings.put(Instant.class.getName(), "string");
        settings.customTypeMappings.put(LocalDateTime.class.getName(), "string");
        settings.customTypeMappings.put(ZonedDateTime.class.getName(), "string");
        settings.customTypeMappings.put(Duration.class.getName(), "string");
        return customizeSettings(settings);
    }

    protected OrderedTypescriptGenerator createGenerator(Settings settings) {
        TypeScriptGenerator generator = new TypeScriptGenerator(settings);
        return new OrderedTypescriptGenerator(generator);
    }

    protected Settings customizeSettings(Settings settings) {
        return settings;
    }

    protected Path createFilePath() {
        return outputFilePath(basePath);
    }

    protected Path getBasePath() {
        return basePath;
    }

    protected Optional<Path> getCustomDecoratorBasePath() {
        return Optional.empty();
    }

    protected abstract Input getInput();

    protected abstract Path outputFilePath(Path basePath);

    protected Optional<CustomValidationSettings> getCustomValidationAnnotationSettings() {
        return Optional.empty();
    }

    public void generateInfoJson(File file) {
        List<EmitterExtension> extensions = createExtensions();
        Settings settings = createSettings(extensions);
        OrderedTypescriptGenerator orderedGenerator = createGenerator(settings);
        final Model model = orderedGenerator.generator.getModelParser().parseModel(getInput().getSourceTypes());
        final TsModel tsModel = orderedGenerator.generator.getModelCompiler().javaToTypeScript(model);
        final Output out = Output.to(file);
        orderedGenerator.generator.getInfoJsonEmitter().emit(tsModel, out.getWriter(), out.getName(), out.shouldCloseWriter());
    }

    private List<Class<? extends Annotation>> getCustomAnnotations() {
        return getCustomValidationAnnotationSettings().map(this::getCustomAnnotations).orElse(Collections.emptyList());
    }

    private List<Class<? extends Annotation>> getCustomAnnotations(CustomValidationSettings customValidationSettings) {
        return new AnnotationExtractor(customValidationSettings.getRootPackage()).extract();
    }

}
