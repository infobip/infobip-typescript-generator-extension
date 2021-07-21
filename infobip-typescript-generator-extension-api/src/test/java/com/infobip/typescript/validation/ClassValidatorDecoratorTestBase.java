package com.infobip.typescript.validation;

import com.infobip.typescript.TestBase;
import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

abstract class ClassValidatorDecoratorTestBase extends TestBase {

    ClassValidatorDecoratorTestBase() {
        super(new ClassValidatorDecoratorExtension(),
              Arrays.asList("import { CommonValidationMessages } from 'infobip-typescript-generator-common'",
                            "import { ValidateNested, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator'"));
    }

    ClassValidatorDecoratorTestBase(List<TSCustomDecorator> tsCustomDecorators,
                                    List<Class<? extends Annotation>> customAnnotations) {
        super(new ClassValidatorDecoratorExtension(null, tsCustomDecorators, customAnnotations),
              Arrays.asList("import { CommonValidationMessages } from 'infobip-typescript-generator-common'",
                            "import { ComplexValidation } from './validators/ComplexValidation'",
                            "import { SimpleValidation } from './validators/SimpleValidation'",
                            "import { localize } from './Localization'"));
    }
}
