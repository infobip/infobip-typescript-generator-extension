package com.infobip.typescript;

import java.util.List;

public class CustomValidationSettings {

    private final List<String> customValidationNamePatterns;
    private final List<String> customValidationNamePackages;

    public CustomValidationSettings(List<String> customValidationNamePatterns, List<String> customValidationNamePackages) {
        this.customValidationNamePatterns = customValidationNamePatterns;
        this.customValidationNamePackages = customValidationNamePackages;
    }

    public List<String> getCustomValidationNamePatterns() {
        return customValidationNamePatterns;
    }

    public List<String> getCustomValidationNamePackages() {
        return customValidationNamePackages;
    }
}
