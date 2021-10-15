package com.infobip.typescript.validation.converter;

import com.infobip.typescript.validation.exception.UnsupportedTypeException;
import cz.habarta.typescript.generator.emitter.*;

public class ObjectToTSLiteralConverter {

    public TsExpression convert(Object object) {
        if (object instanceof Number) {
           return toTsExpression((Number) object);
        } else if (object instanceof String) {
           return toTsExpression((String) object);
        } else if (object instanceof Boolean) {
           return toTsExpression((Boolean) object);
        }

        throw new UnsupportedTypeException(object.getClass());
    }

    private TsExpression toTsExpression(Number number) {
        return new TsNumberLiteral(number);
    }

    private TsExpression toTsExpression(String string) {
        return new TsStringLiteral(string);
    }

    private TsExpression toTsExpression(Boolean bool) {
        return new TsBooleanLiteral(bool);
    }

}
