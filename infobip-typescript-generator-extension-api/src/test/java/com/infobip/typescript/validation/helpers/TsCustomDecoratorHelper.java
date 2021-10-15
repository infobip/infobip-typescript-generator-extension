package com.infobip.typescript.validation.helpers;

import com.infobip.typescript.custom.validation.extractor.TSCustomDecorator;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class TsCustomDecoratorHelper {

    public static final List<TSCustomDecorator> getDecorators() {
        TSCustomDecorator simpleDecorator = new TSCustomDecorator("SimpleValidation", null, null,
                                                                  Paths.get(".", "validators", "SimpleValidation"));
        TSCustomDecorator complexDecorator = new TSCustomDecorator("ComplexValidation", null, null,
                                                                   Paths.get(".", "validators", "ComplexValidation"));

        return Arrays.asList(simpleDecorator, complexDecorator);
    }
}
