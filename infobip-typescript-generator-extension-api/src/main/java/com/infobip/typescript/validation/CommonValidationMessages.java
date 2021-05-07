package com.infobip.typescript.validation;

public class CommonValidationMessages {

    public static final String COMMON_VALIDATION_MESSAGES_CLASS_NAME = "CommonValidationMessages";
    public static final String COMMON_VALIDATION_MESSAGES_FILE_NAME = COMMON_VALIDATION_MESSAGES_CLASS_NAME + ".ts";
    public static final String COMMON_VALIDATION_MESSAGES_SOURCE_CODE =
            "const localize = (message, object: object = {}) => format(message, object);\n" +
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
            "};\n" +
            "\n" +
            "function format(string: string, arg: object = {}) {\n" +
            "    return Object\n" +
            "        .keys(arg)\n" +
            "        .reduce((first: string, second: string) => {\n" +
            "            return first.replace('{' + second + '}', (arg as any)[second]);\n" +
            "        }, string);\n" +
            "}\n";
}
