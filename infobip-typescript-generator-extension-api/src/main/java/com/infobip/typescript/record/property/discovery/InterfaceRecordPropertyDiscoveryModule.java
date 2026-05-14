package com.infobip.typescript.record.property.discovery;

import tools.jackson.databind.module.SimpleModule;

public class InterfaceRecordPropertyDiscoveryModule extends SimpleModule {

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new InterfaceRecordPropertiesAnnotationIntrospector());
    }
}
