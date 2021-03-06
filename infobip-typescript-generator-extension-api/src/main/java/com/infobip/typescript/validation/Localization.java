package com.infobip.typescript.validation;

public class Localization {
    public static final String LOCALIZATION_CLASS_NAME = "Localization";
    public static final String LOCALIZATION_METHOD = "localize";
    public static final String LOCALIZATION_FILE_NAME = LOCALIZATION_CLASS_NAME + ".ts";
    public static final String LOCALIZATION_SOURCE_CODE =
            "export function localize(message, object: object = {}) { return format(message, object)};\n" +
                    "\n" +
                    "function format(text: string, arg: object = {}) {\n" +
                    "    return Object\n" +
                    "        .keys(arg)\n" +
                    "        .reduce((result: string, current: string) => {\n" +
                    "            return result.replace('{' + current + '}', (arg as any)[current]);\n" +
                    "        }, text);\n" +
                    "}\n";
}
