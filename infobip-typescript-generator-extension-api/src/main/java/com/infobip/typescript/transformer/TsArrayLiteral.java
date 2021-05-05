package com.infobip.typescript.transformer;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.emitter.TsExpression;

import java.util.ArrayList;
import java.util.List;

class TsArrayLiteral extends TsExpression {

    private final List<TsExpression> expressions;

    TsArrayLiteral(List<TsExpression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public String format(Settings settings) {
        final List<String> props = new ArrayList<>();
        for (TsExpression property : expressions) {
            props.add(property.format(settings));
        }
        if (props.isEmpty()) {
            return "[]";
        } else {
            return "[" + System.lineSeparator() + "                " + String.join(
                    "," + System.lineSeparator() + "                ", props) + System.lineSeparator() + "            ]";
        }
    }
}
