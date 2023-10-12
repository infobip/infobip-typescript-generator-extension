package com.infobip.typescript;

import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.emitter.EmitterExtension;

import java.util.*;

public abstract class TestBase {

    protected final OrderedTypescriptGenerator generator;

    public TestBase(EmitterExtension extension, List<String> importDeclarations) {
        this(extension, importDeclarations, new Settings());
    }

    public TestBase(EmitterExtension extension, List<String> importDeclarations, Settings settings) {
        settings.jsonLibrary = JsonLibrary.jackson2;
        settings.mapEnum = EnumMapping.asEnum;
        settings.nonConstEnums = true;
        settings.mapClasses = ClassMapping.asClasses;
        settings.noFileComment = true;
        settings.noTslintDisable = true;
        settings.noEslintDisable = true;
        settings.outputFileType = TypeScriptFileType.implementationFile;
        settings.outputKind = TypeScriptOutputKind.module;
        settings.importDeclarations = importDeclarations;
        settings.extensions.add(extension);
        settings.setExcludeFilter(Collections.emptyList(),
                                  Arrays.asList("**SealedSimpleJsonHierarchies", "**SimpleJsonHierarchy", "**PresentPropertyJsonHierarchy"));
        settings.newline = "\n";
        this.generator = new OrderedTypescriptGenerator(new TypeScriptGenerator(settings));
    }

    protected String whenGenerate(Input input) {
        return generator.generateTypeScript(input);
    }

}
