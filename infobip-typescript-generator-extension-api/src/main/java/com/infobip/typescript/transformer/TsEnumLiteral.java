package com.infobip.typescript.transformer;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.compiler.Symbol;
import cz.habarta.typescript.generator.emitter.TsExpression;

public class TsEnumLiteral extends TsExpression {

    private final String literal;

    public <E extends Enum<E>> TsEnumLiteral(Symbol symbol, String enumValue) {
        this.literal = symbol.getFullName() + "." + enumValue;
    }

    @Override
    public String format(Settings settings) {
        return literal;
    }
}
