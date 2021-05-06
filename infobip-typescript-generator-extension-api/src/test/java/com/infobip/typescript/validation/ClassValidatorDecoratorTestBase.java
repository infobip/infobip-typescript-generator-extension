package com.infobip.typescript.validation;

import com.infobip.typescript.TestBase;

import java.util.Arrays;

abstract class ClassValidatorDecoratorTestBase extends TestBase {

    ClassValidatorDecoratorTestBase() {
        super(new ClassValidatorDecoratorExtension(),
              Arrays.asList("import { CommonValidationMessages } from 'infobip-typescript-generator-common'",
                            "import { ValidateNested, IsDefined, IsNotEmpty, MaxLength, MinLength, Max, Min, ArrayMaxSize, ArrayMinSize } from 'class-validator'"));
    }
}
