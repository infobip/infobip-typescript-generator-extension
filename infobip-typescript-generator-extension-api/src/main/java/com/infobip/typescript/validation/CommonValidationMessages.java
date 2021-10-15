package com.infobip.typescript.validation;

public class CommonValidationMessages {

    public static final String COMMON_VALIDATION_MESSAGES_CLASS_NAME = "CommonValidationMessages";
    public static final String COMMON_VALIDATION_MESSAGES_FILE_NAME = COMMON_VALIDATION_MESSAGES_CLASS_NAME + ".ts";
    public static final String COMMON_VALIDATION_MESSAGES_SOURCE_CODE =
            "import { localize } from './Localization';\n" +
            "\n"+
            "const getMaxLengthMessage = (value: number) => localize('must be less than or equal to {value}', { value });\n" +
            "const getMinLengthMessage = (value: number) => localize('must be greater than or equal to {value}', { value });\n" +
            "\n" +
            "export const CommonValidationMessages = {\n" +
            "\n" +
            "    IsDefined: localize('must not be null'),\n" +
            "    IsNotEmpty: localize('must not be blank'),\n" +
            "    Max: getMaxLengthMessage,\n" +
            "    MaxLength: getMaxLengthMessage,\n" +
            "    ArrayMaxSize: getMaxLengthMessage,\n" +
            "    Min: getMinLengthMessage,\n" +
            "    MinLength: getMinLengthMessage,\n" +
            "    ArrayMinSize: getMinLengthMessage,\n" +
            "};\n";
}
