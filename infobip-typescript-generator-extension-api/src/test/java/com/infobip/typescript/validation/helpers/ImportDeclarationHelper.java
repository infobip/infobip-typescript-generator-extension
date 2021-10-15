package com.infobip.typescript.validation.helpers;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import com.infobip.typescript.validation.custom.CombinedValidation;
import com.infobip.typescript.validation.custom.ComplexValidation;
import com.infobip.typescript.validation.custom.NonExistingTSDecorator;
import com.infobip.typescript.validation.custom.SimpleNoMessageValidation;
import com.infobip.typescript.validation.custom.SimpleValidation;

public class ImportDeclarationHelper {

    public static List<String> getImports() {
        return Arrays.asList("import { CommonValidationMessages } from 'infobip-typescript-generator-common'",
                             "import { ComplexValidation } from './validators/ComplexValidation'",
                             "import { SimpleValidation } from './validators/SimpleValidation'",
                             "import { localize } from './Localization'");
    }

    public static List<String> getImportsWithoutLocalization() {
        return Arrays.asList("import { CommonValidationMessages } from 'infobip-typescript-generator-common'",
                             "import { ComplexValidation } from './validators/ComplexValidation'",
                             "import { SimpleValidation } from './validators/SimpleValidation'");
    }

}
