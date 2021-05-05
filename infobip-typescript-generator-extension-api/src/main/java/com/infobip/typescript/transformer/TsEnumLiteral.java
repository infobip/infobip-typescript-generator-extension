package com.infobip.typescript.transformer;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.emitter.TsExpression;

public class TsEnumLiteral extends TsExpression {

    private final String literal;

    public <E extends Enum<E>> TsEnumLiteral(Class<E> type, String enumValue) {
        this.literal = type.getSimpleName() + "." + enumValue;
    }

    @Override
    public String format(Settings settings) {
        return literal;
    }
}
