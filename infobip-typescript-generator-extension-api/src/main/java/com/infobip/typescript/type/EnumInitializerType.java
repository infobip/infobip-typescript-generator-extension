package com.infobip.typescript.type;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;

class EnumInitializerType<E extends Enum<E>> extends TsType {

    private final String type;
    private final String value;

    EnumInitializerType(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String format(Settings settings) {
        java.lang.String enumName = type;
        return enumName + " = " + enumName + '.' + value ;
    }
}
