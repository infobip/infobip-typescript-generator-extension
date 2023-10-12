package com.infobip.typescript.transformer;

import com.infobip.jackson.*;
import com.infobip.typescript.TestBase;
import com.infobip.typescript.TypeScriptFileGenerator;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Settings;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class SealedHierarchiesTransformerDecoratorExtensionTest extends TestBase {

    SealedHierarchiesTransformerDecoratorExtensionTest() {
        super(new ClassTransformerDecoratorExtension(Stream::empty),
              Collections.singletonList("import { Type } from 'class-transformer'"));
    }

    @Test
    void shouldDecorateHierarchiesWithType() {

        // when
        String actual = whenGenerate(Input.from(Animal.class,
                                                Mammal.class,
                                                Human.class,
                                                Bird.class,
                                                Parrot.class,
                                                AnimalContainer.class,
                                                MammalContainer.class,
                                                BirdContainer.class));

        // then
        then(fixNewlines(actual)).isEqualTo(
                """
                        import { Type } from 'class-transformer';

                        export enum MammalType {
                            HUMAN = "HUMAN",
                        }

                        export enum BirdType {
                            PARROT = "PARROT",
                        }

                        export interface Animal {
                        }

                        export interface Mammal extends Animal {
                            type: MammalType;
                        }

                        export class Human implements Mammal {
                            type: MammalType;
                            name: string;
                        }

                        export interface Bird extends Animal {
                            type: BirdType;
                        }

                        export class Parrot implements Bird {
                            type: BirdType;
                        }

                        export class AnimalContainer {
                            @Type(() => Object, {
                                discriminator: {
                                    property: "type", subTypes: [
                                        { value: Human, name: MammalType.HUMAN },
                                        { value: Parrot, name: BirdType.PARROT }
                                    ]
                                }
                            })
                            animal: Animal;
                        }

                        export class MammalContainer {
                            @Type(() => Object, {
                                discriminator: {
                                    property: "type", subTypes: [
                                        { value: Human, name: MammalType.HUMAN }
                                    ]
                                }
                            })
                            mammal: Mammal;
                        }

                        export class BirdContainer {
                            @Type(() => Object, {
                                discriminator: {
                                    property: "type", subTypes: [
                                        { value: Parrot, name: BirdType.PARROT }
                                    ]
                                }
                            })
                            bird: Bird;
                        }""");
    }

    private String fixNewlines(String actual) {
        return actual.trim().replace("\r\n", "\n");
    }

    sealed interface Animal extends SealedSimpleJsonHierarchies {

    }

    sealed interface Bird extends Animal, SimpleJsonHierarchy<BirdType> {

    }

    enum BirdType implements TypeProvider<Bird> {
        PARROT(Parrot.class);

        private final Class<? extends Bird> type;

        BirdType(Class<? extends Bird> type) {
            this.type = type;
        }

        @Override
        public Class<? extends Bird> getType() {
            return type;
        }
    }

    record Parrot() implements Bird {

        @Override
        public BirdType getType() {
            return BirdType.PARROT;
        }
    }

    sealed interface Mammal extends Animal, SimpleJsonHierarchy<MammalType> {

    }

    enum MammalType implements TypeProvider<Mammal> {
        HUMAN(Human.class);

        private final Class<? extends Mammal> type;

        MammalType(Class<? extends Mammal> type) {
            this.type = type;
        }

        @Override
        public Class<? extends Mammal> getType() {
            return type;
        }
    }

    record Human(String name) implements Mammal {

        @Override
        public MammalType getType() {
            return MammalType.HUMAN;
        }
    }

    record AnimalContainer(Animal animal) {

    }

    record MammalContainer(Mammal mammal) {

    }

    record BirdContainer(Bird bird) {

    }

    static class CustomTypeScriptFileGenerator extends TypeScriptFileGenerator {

        protected CustomTypeScriptFileGenerator(Path basePath) {
            super(basePath);
        }

        @Override
        protected Input getInput() {
            return Input.from(Human.class);
        }

        @Override
        protected Path outputFilePath(Path basePath) {
            return null;
        }

        @Override
        protected Settings customizeSettings(Settings settings) {
            settings.setExcludeFilter(List.of(), List.of("com.infobip.jackson**"));
            return settings;
        }
    }
}
