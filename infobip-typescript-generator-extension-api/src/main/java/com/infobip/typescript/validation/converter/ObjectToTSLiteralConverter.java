package com.infobip.typescript.validation.converter;

import com.infobip.typescript.validation.exception.UnsupportedTypeException;
import cz.habarta.typescript.generator.emitter.*;

import java.util.Arrays;
import java.util.List;

public class ObjectToTSLiteralConverter {

    private static final List<Class> SUPPORTED_TYPES = Arrays.asList(Number.class, String.class, Boolean.class);

    public TsExpression convert(Object object) {
        if (object instanceof Number) {
            toTsExpression((Number) object);
        } else if (object instanceof String) {
            toTsExpression((String) object);
        } else if (object instanceof Boolean) {
            toTsExpression((Boolean) object);
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
