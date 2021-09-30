package com.infobip.typescript.custom.validation;

import com.infobip.typescript.CustomTypeScriptDecorator;
import io.github.classgraph.ClassGraph;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotationExtractor {

    private final ClassGraph classGraph;

    public AnnotationExtractor(String rootPackage) {
        this.classGraph = new ClassGraph().enableClassInfo()
                                          .enableAnnotationInfo()
                                          .enableMethodInfo()
                                          .acceptPackages(rootPackage);
    }

    public List<Class<? extends Annotation>> extract() {
        return classGraph.scan()
                         .getAllAnnotations()
                         .stream()
                         .map(annotation -> (Class<? extends Annotation>) annotation.loadClass())
                         .filter(annotation -> Optional.ofNullable(annotation.getAnnotation(CustomTypeScriptDecorator.class))
                                                       .isPresent())
                         .collect(Collectors.toList());

    }
}
