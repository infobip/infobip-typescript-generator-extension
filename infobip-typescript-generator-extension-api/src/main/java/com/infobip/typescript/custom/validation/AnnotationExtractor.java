package com.infobip.typescript.custom.validation;

import com.infobip.typescript.CustomValidationSettings;
import com.infobip.typescript.custom.validation.extractor.ValidatorsNameExtractor;
import cz.habarta.typescript.generator.util.Utils;
import io.github.classgraph.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class AnnotationExtractor {

    private static final Set<String> BLACK_LISTED_METHODS = Stream.of("groups",
                                                                      "payload")
                                                                  .collect(Collectors.toSet());

    private final List<Pattern> customValidationNameRegexPatterns;
    private final ClassGraph classGraph;
    private final ValidatorsNameExtractor validatorsNameExtractor;

    public AnnotationExtractor(CustomValidationSettings customValidationSettings) {
        this.customValidationNameRegexPatterns = Utils.globsToRegexps(
                customValidationSettings.getCustomValidationNamePatterns());
        this.validatorsNameExtractor = new ValidatorsNameExtractor(customValidationSettings.getCustomValidatorsPaths());
        this.classGraph = new ClassGraph().enableClassInfo()
                                          .enableAnnotationInfo()
                                          .enableMethodInfo()
                                          .acceptPackages(customValidationSettings.getCustomValidationNamePackages()
                                                                                  .toArray(new String[0]));
    }

    public CustomValidationData extract() {
        ScanResult scanResult = classGraph.scan();
        Map<Class<? extends Annotation>, CustomValidationAnnotation> customValidationAnnotations = getCustomValidationAnnotations(scanResult);
        return new CustomValidationData(customValidationAnnotations, validatorsNameExtractor.extract());
    }

    private boolean filterClassNames(String className) {
        // TODO should we have our own utils
        return Utils.classNameMatches(className, customValidationNameRegexPatterns);
    }

    private Map<Class<? extends Annotation>, CustomValidationAnnotation> getCustomValidationAnnotations(ScanResult scanResult) {
        return scanResult.getAllAnnotations().stream()
                         .filter(classInfo -> filterClassNames(
                                 classInfo.getName()))
                         .filter(ClassInfo::isAnnotation)
                         .map(this::convert)
                         .collect(Collectors.toMap(CustomValidationAnnotation::getAnnotation, identity()));
    }

    private CustomValidationAnnotation convert(ClassInfo classInfo) {
        Class annotationClass = classInfo.loadClass();
        List<Annotation> additionalAnnotations = Arrays.stream(annotationClass.getAnnotations())
                                                       .collect(Collectors.toList());
        List<String> methods = extractMethods(classInfo.getMethodInfo());
        return new CustomValidationAnnotation(annotationClass,
                                              methods,
                                              additionalAnnotations);

    }

    private List<String> extractMethods(MethodInfoList methodInfos) {
        return methodInfos.stream()
                .filter(methodInfo -> !BLACK_LISTED_METHODS.contains(methodInfo.getName()))
                .map(MethodInfo::getName)
                .collect(Collectors.toList());
    }
}
