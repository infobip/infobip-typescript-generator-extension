package com.infobip.typescript.validation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import com.infobip.typescript.TestBase;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;

abstract class ClassValidatorDecoratorTestBase extends TestBase {

    ClassValidatorDecoratorTestBase() {
        super(new ClassValidatorDecoratorExtension(),
              Arrays.asList("import { CommonValidationMessages } from 'infobip-typescript-generator-common'",
                            "import { ValidateNested, IsOptional, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator'"));
    }

    ClassValidatorDecoratorTestBase(List<TSCustomDecorator> tsCustomDecorators,
                                    List<Class<? extends Annotation>> customAnnotations,
                                    List<String> imports) {
        super(new ClassValidatorDecoratorExtension(null, tsCustomDecorators, customAnnotations), imports);
    }
}
