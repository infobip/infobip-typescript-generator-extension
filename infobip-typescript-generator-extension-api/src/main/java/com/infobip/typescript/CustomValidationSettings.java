package com.infobip.typescript;

public class CustomValidationSettings {

    private final String rootPackage;

    public CustomValidationSettings(String rootPackage) {
        if (rootPackage == null || rootPackage.isEmpty()) {
            throw new IllegalArgumentException("rootPackage parameter cannot be null");
        }

        this.rootPackage = rootPackage;
    }

    public String getRootPackage() {
        return rootPackage;
    }
}
