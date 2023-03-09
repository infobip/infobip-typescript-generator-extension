package com.infobip.typescript.type;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;

class StringInitializerType extends TsType {

    private final String value;

    StringInitializerType(String value) {
        this.value = value;
    }

    @Override
    public String format(Settings settings) {
        return "string = \"" + this.value + "\"";
    }
}
