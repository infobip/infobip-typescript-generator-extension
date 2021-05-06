package com.infobip.typescript;

import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.TypeScriptGenerator;

public class OrderedTypescriptGenerator {
    private final TypeScriptGenerator generator;

    public OrderedTypescriptGenerator(TypeScriptGenerator generator) {
        this.generator = generator;
    }

    public String generateTypeScript(Input input) {
        String generated = generator.generateTypeScript(input);
        int firstExportIndex = generated.indexOf("export") - 1;
        int firstEnumIndex = generated.indexOf("export enum") - 1;

        if(firstEnumIndex < 0) {
            return generated;
        }

        String imports = generated.substring(0, firstExportIndex);
        String nonEnums = generated.substring(firstExportIndex, firstEnumIndex);
        String enums = generated.substring(firstEnumIndex);

        return (imports + enums + nonEnums);
    }
}
