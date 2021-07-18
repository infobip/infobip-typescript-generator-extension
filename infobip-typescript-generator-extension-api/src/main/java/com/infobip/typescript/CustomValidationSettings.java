package com.infobip.typescript;

import java.nio.file.Path;
import java.util.List;

public class CustomValidationSettings {

    private final List<String> customValidationNamePatterns;
    private final List<String> customValidationNamePackages;
    private final List<Path> customValidatorsPaths;

    public CustomValidationSettings(List<String> customValidationNamePatterns,
                                    List<String> customValidationNamePackages,
                                    List<Path> customValidatorsPaths) {
        this.customValidationNamePatterns = customValidationNamePatterns;
        this.customValidationNamePackages = customValidationNamePackages;
        this.customValidatorsPaths = customValidatorsPaths;
    }

    public List<String> getCustomValidationNamePatterns() {
        return customValidationNamePatterns;
    }

    public List<String> getCustomValidationNamePackages() {
        return customValidationNamePackages;
    }

    public List<Path> getCustomValidatorsPaths() {
        return customValidatorsPaths;
    }
}
