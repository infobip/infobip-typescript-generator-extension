package com.infobip.typescript.type;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;

class EnumInitializerType<E extends Enum<E>> extends TsType {

    private final Class<E> type;
    private final E value;

    EnumInitializerType(Class<E> type, E value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String format(Settings settings) {
        java.lang.String enumName = type.getSimpleName();
        return enumName + " = " + enumName + '.' + value ;
    }
}
