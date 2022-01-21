package com.infobip.typescript.transformer;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.emitter.TsObjectLiteral;
import cz.habarta.typescript.generator.emitter.TsPropertyDefinition;
import cz.habarta.typescript.generator.util.Utils;

import java.util.*;

class DiscriminatorValueTsObjectLiteral extends TsObjectLiteral {

    private final List<TsPropertyDefinition> propertyDefinitions;

    public DiscriminatorValueTsObjectLiteral(TsPropertyDefinition... propertyDefinitions) {
        super(Utils.removeNulls(Arrays.asList(propertyDefinitions)));
        this.propertyDefinitions = Utils.removeNulls(Arrays.asList(propertyDefinitions));
    }

    @Override
    public String format(Settings settings) {
        final List<String> props = new ArrayList<>();
        for (TsPropertyDefinition property : propertyDefinitions) {
            props.add(property.format(settings));
        }
        if (props.isEmpty()) {
            return "{}";
        } else {
            return "{" + settings.newline + "            " + String.join(", ",
                                                                               props) + settings.newline + "        }";
        }
    }
}
