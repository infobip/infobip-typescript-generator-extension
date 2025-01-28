package com.infobip.typescript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobip.typescript.record.property.discovery.InterfaceRecordPropertyDiscoveryModule;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.TypeScriptGenerator;
import cz.habarta.typescript.generator.parser.Jackson2Parser;

public class OrderedTypescriptGenerator {
    final TypeScriptGenerator generator;

    public OrderedTypescriptGenerator(TypeScriptGenerator generator) {
        this.generator = generator;
        var typeScriptGeneratorObjectMapper = getObjectMapper(generator);
        typeScriptGeneratorObjectMapper.registerModule(new InterfaceRecordPropertyDiscoveryModule());
    }

    private ObjectMapper getObjectMapper(TypeScriptGenerator generator) {
        var modelParser = (Jackson2Parser) generator.getModelParser();
        try {
            var field = Jackson2Parser.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            return (ObjectMapper) field.get(modelParser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generateTypeScript(Input input) {
        String generated = generator.generateTypeScript(input);
        int indexOfExport = generated.indexOf("export");

        if (indexOfExport < 1) {
            return generated;
        }

        int firstExportIndex = indexOfExport - 1;

        int indexOfExportEnum = generated.indexOf("export enum");

        if (indexOfExportEnum < 1) {
            return generated;
        }

        int firstEnumIndex = indexOfExportEnum - 1;
        String imports = generated.substring(0, firstExportIndex);
        String nonEnums = generated.substring(firstExportIndex, firstEnumIndex);
        String enums = generated.substring(firstEnumIndex);

        return (imports + enums + nonEnums);
    }
}
