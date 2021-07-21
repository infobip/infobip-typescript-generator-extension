package com.infobip.typescript.validation;

public class Localization {
    public static final String LOCALIZATION_CLASS_NAME = "Localization";
    public static final String LOCALIZATION_METHOD = "localize";
    public static final String LOCALIZATION_FILE_NAME = LOCALIZATION_CLASS_NAME + ".ts";
    public static final String LOCALIZATION_SOURCE_CODE =
            "export function localize(message, object: object = {}) {format(message, object)};\n" +
                    "\n" +
                    "function format(string: string, arg: object = {}) {\n" +
                    "    return Object\n" +
                    "        .keys(arg)\n" +
                    "        .reduce((first: string, second: string) => {\n" +
                    "            return first.replace('{' + second + '}', (arg as any)[second]);\n" +
                    "        }, string);\n" +
                    "}\n";
}
