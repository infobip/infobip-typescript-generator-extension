package com.infobip.typescript;

import com.infobip.typescript.record.property.discovery.InterfaceRecordPropertyDiscoveryModule;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeScriptGenerator;

public class OrderedTypescriptGenerator {
    final TypeScriptGenerator generator;

    public OrderedTypescriptGenerator(Settings settings) {
        settings.jackson3Modules.add(InterfaceRecordPropertyDiscoveryModule.class);
        this.generator = new TypeScriptGenerator(settings);
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
