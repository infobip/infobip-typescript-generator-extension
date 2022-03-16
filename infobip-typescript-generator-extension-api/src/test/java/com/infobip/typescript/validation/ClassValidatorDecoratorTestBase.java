package com.infobip.typescript.validation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import com.infobip.typescript.TestBase;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;

abstract class ClassValidatorDecoratorTestBase extends TestBase {

    private static final String IMPORT_COMMON_VALIDATION_MESSAGES = "import { CommonValidationMessages } from 'infobip-typescript-generator-common'";
    private static final String IMPORT_CLASS_VALIDATOR = "import { ValidateNested, IsDefined, IsOptional, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator'";
    protected static final String IMPORTS = IMPORT_COMMON_VALIDATION_MESSAGES + ";\n" + IMPORT_CLASS_VALIDATOR + ";\n";

    ClassValidatorDecoratorTestBase() {
        super(new ClassValidatorDecoratorExtension(),
              Arrays.asList(IMPORT_COMMON_VALIDATION_MESSAGES, IMPORT_CLASS_VALIDATOR));
    }

    ClassValidatorDecoratorTestBase(List<TSCustomDecorator> tsCustomDecorators,
                                    List<Class<? extends Annotation>> customAnnotations,
                                    List<String> imports) {
        super(new ClassValidatorDecoratorExtension(null, tsCustomDecorators, customAnnotations), imports);
    }
}
