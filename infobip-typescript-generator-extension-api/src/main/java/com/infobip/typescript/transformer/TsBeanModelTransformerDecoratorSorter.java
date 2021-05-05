package com.infobip.typescript.transformer;

import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsDeclarationModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TsBeanModelTransformerDecoratorSorter {

    private final Map<Class<?>, TsBeanModel> originToModel;

    TsBeanModelTransformerDecoratorSorter(Map<Class<?>, TsBeanModel> originToModel) {
        this.originToModel = originToModel;
    }

    List<TsBeanModel> sort(Map<Class<?>, List<Class<?>>> parentToChildren,
                           List<TsBeanModel> models) {

        Map<Boolean, List<Class<?>>> partitioned = models.stream()
                                                         .collect(Collectors.partitioningBy(
                                                                 this::containsTypeIdentifier))
                                                         .entrySet()
                                                         .stream()
                                                         .collect(Collectors.toMap(Map.Entry::getKey,
                                                                                   entry -> entry.getValue()
                                                                                                 .stream()
                                                                                                 .map(TsDeclarationModel::getOrigin)
                                                                                                 .collect(
                                                                                                         Collectors.toList())));

        List<Class<?>> modelsWithTypeIdentifier = partitioned.get(true);
        Map<Class<?>, List<Class<?>>> filteredParentToChildren = filter(parentToChildren, modelsWithTypeIdentifier);
        return Stream.concat(partitioned.get(false).stream(), sortDecoratedModels(filteredParentToChildren,
                                                                                  modelsWithTypeIdentifier))
                     .map(originToModel::get)
                     .collect(Collectors.toList());
    }

    private boolean containsTypeIdentifier(TsBeanModel first) {
        return first.getProperties()
                    .stream()
                    .flatMap(tsPropertyModel -> tsPropertyModel.getDecorators().stream())
                    .anyMatch(
                            tsDecorator -> tsDecorator.getIdentifierReference().equals(
                                    ClassTransformerDecoratorExtension.TYPE));
    }

    private Stream<Class<?>> sortDecoratedModels(Map<Class<?>, List<Class<?>>> parentToChildren,
                                                 List<Class<?>> types) {

        if (types.isEmpty()) {
            return Stream.of();
        }

        List<Class<?>> leafModels = getLeafTypes(parentToChildren, types);

        if (leafModels.isEmpty()) {
            return types.stream();
        }

        List<Class<?>> nonLeafModels = types.stream().filter(model -> !leafModels.contains(model)).collect(Collectors.toList());
        return Stream.concat(leafModels.stream(),
                             sortDecoratedModels(filter(parentToChildren, nonLeafModels), nonLeafModels));
    }

    private List<Class<?>> getLeafTypes(Map<Class<?>, List<Class<?>>> parentToChildren,
                                        List<Class<?>> types) {
        List<Class<?>> nonLeafModels = types.stream()
                                            .filter(type -> getModelsReferencedInDecorator(parentToChildren, type).isEmpty())
                                            .collect(Collectors.toList());

        return types.stream().filter(nonLeafModels::contains).collect(Collectors.toList());
    }

    private List<Class<?>> getModelsReferencedInDecorator(Map<Class<?>, List<Class<?>>> parentToChildren,
                                                          Class<?> target) {

        List<Class<?>> firstLevel = parentToChildren.getOrDefault(target, Collections.emptyList());

        if (firstLevel.isEmpty()) {
            return Collections.emptyList();
        }

        List<Class<?>> nested = firstLevel.stream()
                                          .filter(type -> !type.equals(target))
                                          .flatMap(type -> getModelsReferencedInDecorator(parentToChildren, type).stream())
                                          .collect(Collectors.toList());

        return Stream.concat(firstLevel.stream(), nested.stream()).collect(Collectors.toList());
    }

    private Map<Class<?>, List<Class<?>>> filter(Map<Class<?>, List<Class<?>>> parentToChildren,
                                                 List<Class<?>> types) {
        return parentToChildren
                .entrySet()
                .stream()
                .filter(entry -> types.stream().anyMatch(type -> type.equals(entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          entry -> entry.getValue()
                                                        .stream()
                                                        .filter(types::contains)
                                                        .filter(type -> !type.equals(entry.getKey()))
                                                        .collect(
                                                                Collectors.toList())));
    }
}
