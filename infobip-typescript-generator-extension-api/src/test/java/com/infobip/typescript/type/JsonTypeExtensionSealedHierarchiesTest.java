package com.infobip.typescript.type;

import com.infobip.jackson.*;
import com.infobip.typescript.TestBase;
import cz.habarta.typescript.generator.Input;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class JsonTypeExtensionSealedHierarchiesTest extends TestBase {

    JsonTypeExtensionSealedHierarchiesTest() {
        super(new JsonTypeExtension(Stream::empty),
              Collections.emptyList());
    }

    @Test
    void shouldAddReadonlyTypeField() {

        // when
        String actual = whenGenerate(Input.from(Animal.class,
                                                Mammal.class,
                                                Bird.class,
                                                Human.class,
                                                Parrot.class));

        // then
        then(actual).isEqualTo(
                """

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
                                                
                        export interface Bird extends Animal {
                            type: BirdType;
                        }
                                                
                        export class Human implements Mammal {
                            readonly type: MammalType = MammalType.HUMAN;
                            name: string;
                        }
                                                
                        export class Parrot implements Bird {
                            readonly type: BirdType = BirdType.PARROT;
                        }
                        """);
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

    record AnimalWrapper(Animal animal) {

    }

    record HumanWrapper(Human human) {

    }

    record BirdWrapper(Bird bird) {

    }
}
