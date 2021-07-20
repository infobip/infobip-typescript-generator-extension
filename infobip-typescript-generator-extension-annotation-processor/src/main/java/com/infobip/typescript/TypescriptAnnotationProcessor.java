package com.infobip.typescript;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class TypescriptAnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(GenerateTypescript.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<Class<? extends Annotation>> customValidations = getCustomValidations(roundEnv);
        generateTypescript(roundEnv, customValidations);
        return true;
    }

    private List<Class<? extends Annotation>> getCustomValidations(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(CustomTSDecorator.class)
                .stream()
                .filter(element -> element.getKind().equals(ElementKind.ANNOTATION_TYPE))
                .map(element -> getCustomValidationClass(element.asType()))
                .collect(Collectors.toList());

    }

    private void generateTypescript(RoundEnvironment roundEnv, List<Class<? extends Annotation>> customAnnotations) {
        roundEnv.getElementsAnnotatedWith(GenerateTypescript.class)
                .stream()
                .filter(element -> element.getKind().equals(ElementKind.CLASS))
                .map(element -> (TypeElement) element)
                .forEach(this::generateTypeScript);
    }

    private void generateTypeScript(TypeElement element) {
        GenerateTypescript generateTypescript = element.getAnnotation(GenerateTypescript.class);
        TypeScriptFileGenerator typeScriptFileGenerator = createTypeScriptFileGenerator(element, generateTypescript);
        typeScriptFileGenerator.generate();
    }

    private TypeScriptFileGenerator createTypeScriptFileGenerator(TypeElement element,
                                                                  GenerateTypescript generateTypescript) {
        Path basePath = getBasePath(element);
        Class<? extends TypeScriptFileGenerator> typeScriptGeneratorFactory = getGeneratorClass(generateTypescript);
        try {
            return typeScriptGeneratorFactory.getDeclaredConstructor(Path.class).newInstance(basePath);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to create new instance of " + typeScriptGeneratorFactory, e);
        }
    }

    private Class<? extends TypeScriptFileGenerator> getGeneratorClass(GenerateTypescript generateTypescript) {

        try {
            return generateTypescript.generator();
        } catch (MirroredTypeException e) {
            return getGeneratorClass(e.getTypeMirror());
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends TypeScriptFileGenerator> getGeneratorClass(TypeMirror typeMirror) {

        try {
            Class<?> aClass = getClass().getClassLoader()
                                        .loadClass(typeMirror.toString());
            return (Class<? extends TypeScriptFileGenerator>) aClass;
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IllegalStateException(classNotFoundException);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> getCustomValidationClass(TypeMirror typeMirror) {

        try {
            Class<?> aClass = getClass().getClassLoader()
                                        .loadClass(typeMirror.toString());
            return (Class<? extends Annotation>) aClass;
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IllegalStateException(classNotFoundException);
        }
    }

    private Path getBasePath(TypeElement element) {
        return Paths.get(getResource(element).toUri()).getParent();
    }

    private FileObject getResource(TypeElement element) {
        Filer filer = processingEnv.getFiler();
        try {
            return filer.getResource(StandardLocation.CLASS_OUTPUT, "", element.getQualifiedName());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
