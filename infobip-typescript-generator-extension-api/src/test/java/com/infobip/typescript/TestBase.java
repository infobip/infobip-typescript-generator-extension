package com.infobip.typescript;

import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.emitter.EmitterExtension;

import java.util.Collections;
import java.util.List;

public abstract class TestBase {

    private final OrderedTypescriptGenerator generator;

    public TestBase(EmitterExtension extension, List<String> importDeclarations) {
        Settings givenSettings = new Settings();
        givenSettings.outputKind = TypeScriptOutputKind.global;
        givenSettings.jsonLibrary = JsonLibrary.jackson2;
        givenSettings.mapEnum = EnumMapping.asEnum;
        givenSettings.nonConstEnums = true;
        givenSettings.mapClasses = ClassMapping.asClasses;
        givenSettings.noFileComment = true;
        givenSettings.noTslintDisable = true;
        givenSettings.noEslintDisable = true;
        givenSettings.outputFileType = TypeScriptFileType.implementationFile;
        givenSettings.outputKind = TypeScriptOutputKind.module;
        givenSettings.importDeclarations = importDeclarations;
        givenSettings.extensions.add(extension);
        givenSettings.setExcludeFilter(Collections.emptyList(), Collections.singletonList("**SimpleJsonHierarchy"));
        this.generator = new OrderedTypescriptGenerator(new TypeScriptGenerator(givenSettings));
    }

    protected String whenGenerate(Input input) {
        return generator.generateTypeScript(input);
    }
}
