package com.infobip.typescript.custom.validation;

import com.infobip.typescript.CustomValidationSettings;
import cz.habarta.typescript.generator.util.Utils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnnotationExtractor {

    private final List<Pattern> customValidationNameRegexPatterns;
    private final ClassGraph classGraph;

    public AnnotationExtractor(CustomValidationSettings customValidationSettings) {
        this.customValidationNameRegexPatterns = Utils.globsToRegexps(
                customValidationSettings.getCustomValidationNamePatterns());
        this.classGraph = new ClassGraph().enableClassInfo()
                                          .enableAnnotationInfo()
                                          .acceptPackages(customValidationSettings.getCustomValidationNamePackages()
                                                                                  .toArray(new String[0]));
    }

    public List<CustomValidation> extract() {
        ScanResult scanResult = classGraph.scan();
        return scanResult.getAllAnnotations().stream()
                         .filter(classInfo -> filterClassNames(classInfo.getName()))
                         .map(classInfo -> classInfo.loadClass())
                         .filter(Class::isAnnotation)
                         .map(this::convert)
                         .collect(Collectors.toList());
    }

    private boolean filterClassNames(String className) {
        // TODO should we have our own utils
        return Utils.classNameMatches(className, customValidationNameRegexPatterns);
    }

    private CustomValidation convert(Class annotationClass) {
        List<Annotation> additionalAnnotations = Arrays.stream(annotationClass.getAnnotations())
                                                       .collect(Collectors.toList());

        return new CustomValidation(annotationClass, additionalAnnotations);
    }
}
