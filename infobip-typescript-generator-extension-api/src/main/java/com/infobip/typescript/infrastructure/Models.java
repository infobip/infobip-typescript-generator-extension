package com.infobip.typescript.infrastructure;

import java.util.List;

import cz.habarta.typescript.generator.parser.BeanModel;
import cz.habarta.typescript.generator.parser.Model;

public class Models {

    public static Model withBeans(Model model, List<BeanModel> beans) {
        return new Model(beans, model.getEnums(), model.getRestApplications());
    }
}
