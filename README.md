# Infobip TypeScript Generator Extension

[![](https://github.com/infobip/infobip-typescript-generator-extension/workflows/maven/badge.svg)](https://github.com/infobip/infobip-typescript-generator-extension/actions?query=workflow%3Amaven)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.infobip/infobip-typescript-generator-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.infobip/infobip-typescript-generator-extension)
[![Coverage Status](https://coveralls.io/repos/github/infobip/infobip-typescript-generator-extension/badge.svg?branch=main)](https://coveralls.io/github/infobip/infobip-typescript-generator-extension?branch=main)
[![Known Vulnerabilities](https://snyk.io/test/github/infobip/infobip-typescript-generator-extension/badge.svg)](https://snyk.io/test/github/infobip/infobip-typescript-generator-extension)

Library which provides new features on top of [TypeScript Generator](https://github.com/vojtechhabarta/typescript-generator):
annotation processor support (which eliminates the requirement for a maven plugin) and bean validation Java annotations to TypeScript decorators translation.

## Contents

1. [Bean Validation to class-validator translation](#BeanValidationToClassValidatorTranslation)
    * [Simple Object](#SimpleObject)
    * [Hierarchy](#Hierarchy)
2. [Custom Validation to class-validator translation](#CustomValidationToClassValidatorTranslation)
    * [@CustomTypeScriptDecorator](#CustomTypeScriptDecoratorAnnotation)
    * [Limitations](#CustomValidationLimitations)
    * [Example](#CustomValidationExample)
3. [Localization.ts & CommonValidationMessages.ts](#Localization&CommonValidationMessages)
4. [Time type mappings](#TimeTypeMappings)
5. [Annotation processor](#AnnotationProcessor)
6. [Contributing](#Contributing)
7. [License](#License)

<a id="BeanValidationToClassValidatorTranslation"></a>
##  Bean Validation to class-validator translation

<a id="SimpleObject"></a>
### Simple Object

Input:

```java

public record Foo(

        @Size(min = 1, max = 2)
        @NotEmpty
        @NotNull
        @Valid
        String bar

) {

}
```

Result:

```typescript
/* tslint:disable */
/* eslint-disable */
import { IsDefined, IsNotEmpty, MinLength, ValidateNested, MaxLength } from 'class-validator';
import { CommonValidationMessages } from './CommonValidationMessages';

export class Foo {
    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
    @IsDefined({ message: CommonValidationMessages.IsDefined })
    @ValidateNested()
    bar: string;
}
```

<a id="Hierarchy"></a>
###  Hierarchy

[class-transformer](https://github.com/typestack/class-transformer) together
with [Infobip Jackson Extension](https://github.com/infobip/infobip-jackson-extension) is used to handle hierarchies.

Example for a hierarchy is a multi level hierarchy for inbound and outbound messages.

Input:

```java

enum Channel {
    SMS(InboundSmsMessage.class, OutboundSmsMessage.class);

    private final Class<? extends InboundMessage> inboundMessageType;
    private final Class<? extends OutboundMessage> outboundMessageType;

    Channel(Class<? extends InboundMessage> inboundMessageType,
            Class<? extends OutboundMessage> outboundMessageType) {
        this.inboundMessageType = inboundMessageType;
        this.outboundMessageType = outboundMessageType;
    }

    public Class<? extends InboundMessage> getInboundMessageType() {
        return inboundMessageType;
    }

    public Class<? extends OutboundMessage> getOutboundMessageType() {
        return outboundMessageType;
    }
}

enum Direction implements TypeProvider<Message> {
    INBOUND(InboundMessage.class),
    OUTBOUND(OutboundMessage.class);

    private final Class<? extends Message> type;

    Direction(Class<? extends Message> type) {
        this.type = type;
    }

    @Override
    public Class<? extends Message> getType() {
        return type;
    }
}

public enum CommonContentType implements TypeProvider<CommonContent>, ContentType {
    TEXT(TextContent.class);

    private final Class<? extends CommonContent> type;

    CommonContentType(Class<? extends CommonContent> type) {
        this.type = type;
    }

    @Override
    public Class<? extends CommonContent> getType() {
        return type;
    }
}

@JsonTypeResolveWith(InboundMessageJsonTypeResolver.class)
interface InboundMessage extends Message {

    @Override
    default Direction getDirection() {
        return Direction.INBOUND;
    }

}

@JsonTypeResolveWith(MessageJsonTypeResolver.class)
interface Message {

    Direction getDirection();

    Channel getChannel();

}

@JsonTypeResolveWith(OutboundMessageJsonTypeResolver.class)
interface OutboundMessage extends Message {

    @Override
    default Direction getDirection() {
        return Direction.OUTBOUND;
    }

}

public interface CommonContent extends SimpleJsonHierarchy<CommonContentType>, Content<CommonContentType> {

}

public interface Content<T extends ContentType> {

    T getType();

}

public interface ContentType {

}

record TextContent(

        @NotNull
        @NotEmpty
        String text

) implements CommonContent {

    @Override
    public CommonContentType getType() {
        return CommonContentType.TEXT;
    }
}

record InboundSmsMessage(

        CommonContent content

) implements InboundMessage {

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
}

record OutboundSmsMessage(

        CommonContent content

) implements OutboundMessage {

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
}
```

Result:

```typescript
/* tslint:disable */
/* eslint-disable */
import 'reflect-metadata';
import { Type } from 'class-transformer';
import { IsDefined, IsNotEmpty } from 'class-validator';
import { CommonValidationMessages } from './CommonValidationMessages';

export enum Channel {
    SMS = 'SMS',
}

export enum Direction {
    INBOUND = 'INBOUND',
    OUTBOUND = 'OUTBOUND',
}

export enum CommonContentType {
    TEXT = 'TEXT',
}

export interface InboundMessage extends Message {
}

export interface Message {
    channel: Channel;
    direction: Direction;
}

export interface OutboundMessage extends Message {
}

export interface CommonContent extends Content<CommonContentType> {
    type: CommonContentType;
}

export interface Content<T> {
    type: T;
}

export interface ContentType {
}

export class TextContent implements CommonContent {
    readonly type: CommonContentType = CommonContentType.TEXT;
    @IsDefined({ message: CommonValidationMessages.IsDefined })
    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
    text: string;
}

export class InboundSmsMessage implements InboundMessage {
    readonly channel: Channel = Channel.SMS;
    direction: Direction;
    @Type(() => Object, {
        discriminator: {
            property: 'type', subTypes: [
                { value: TextContent, name: CommonContentType.TEXT }
            ]
        }
    })
    content: CommonContent;
}

export class OutboundSmsMessage implements OutboundMessage {
    readonly channel: Channel = Channel.SMS;
    direction: Direction;
    @Type(() => Object, {
        discriminator: {
            property: 'type', subTypes: [
                { value: TextContent, name: CommonContentType.TEXT }
            ]
        }
    })
    content: CommonContent;
}
```

<a id="CustomValidationToClassValidatorTranslation"></a>
### Custom Validation to class-validator translation

<a id="CustomTypeScriptDecoratorAnnotation"></a>
#### @CustomTypeScriptDecorator

In order to link custom java validation annotation with appropriate decorator, java validation annotation must be marked **@CustomTypeScriptDecorator**
annotation.

+ **@CustomTypeScriptDecorator** has:
    * **typeScriptDecorator** optional parameter:
        * if a provided annotation is going to be linked to a decorator with a specified name
        * else it is going to be linked to a decorator with the same name as an annotation
    * **decoratorParameterListExtractor** optional parameter:
        * provides an implementation of a class which extracts annotation parameters
        * provided class must implement **DecoratorParameterListExtractor** interface

Also in class which extends from **TypeScriptFileGenerator** two methods must be overridden:

* **getCustomValidationAnnotationSettings**:
    * Which will provide settings needed for locating custom java annotations. Setting will provide name of java package from where annotation scanning will
      begin.
* **getCustomDecoratorBasePath**:
    * Which will provide base path to TypeScript decorators

After providing the above information, **TypeScriptFileGenerator** will take a scan project for custom annotations and will perform logic to link annotations wit
appropriate TypeScript decorators.

<a id="CustomValidationLimitations"></a>
#### Limitations

1. From class-validation only [Custom Validation Decorators](https://github.com/typestack/class-validator#custom-validation-decorators) are supported, reason
   behind supporting only [Custom Validation Decorators](https://github.com/typestack/class-validator#custom-validation-decorators) and not
   supporting [Custom Validation Classes]([Custom Validation Decorators](https://github.com/typestack/class-validator#custom-validation-decorators)) as well, is
   that in first approach we are able to link custom java annotation with TypescriptDecorator by just looking at decorator's file name, while in second approach
   we would need to parse a whole file in order to find where is a decorator defined.
2. TypeScript decorator file name and decorator must be the same, reason behind this is similar to previous point, if we decide to not follow given convention we
   would need to parse the whole Typescript file in order to find a given decorator. This would introduce additional complexity. This also means that one decorator is
   located in one TypeScript file.
3. All decorators will be copied in **dist** location under **validation** folder.
4. **getCustomValidationAnnotationSettings** must be overridden in class which extends from **TypeScriptFileGenerator**. This is done to restrict scope of **
   ClassGraph** annotation scanning. By default, **ClassGraph** will scan all classes in the class path and will try to extract annotations from them, if
   restriction is not performed given operation could result in **OutOfMemoryError**.

<a id="CustomValidationExample"></a>
#### Example

Annotation implementation:

```java

@CustomTypeScriptDecorator(
    typeScriptDecorator = "ComplexValidator",
    decoratorParameterListExtractor = DecoratorParameterListExtractorImpl.class,
    type = ComplexCustomValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ComplexCustomValidator.class)
public @interface ComplexCustomValidation {

    String message() default "must be valid element";

    int length();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
```

Input:

```java

public record Foo(

        @ComplexCustomValidation(length = 100)
        String bar

) {

}
```

Result:

```typescript
/* tslint:disable */
/* eslint-disable */
import { CommonValidationMessages } from './CommonValidationMessages';
import { localize } from './Localization';

export class Foo {
    @ComplexValidator(100, { message: localize('must be valid element') })
    bar: string;
}
```

<a id="Localization&CommonValidationMessages"></a>
### Localization.ts & CommonValidationMessages.ts

Running **TypeScript Generator Extension** will result in two additional files:

* **Localization.ts**
    * Blueprint for Localization functionality
* **CommonValidationMessages.ts**
    * Blueprint for common validation messages

After initial generation, this two files can be changed in order to adjust custom validation messages, or to implement a way how localization is supported.
After manually making changes, you can tell **TypeScript Generator Extension** not to generate this files and more in the future. You can achieve this by
overriding:

* **writeCommonValidationMessagesTypeScriptFile** for **CommonValidationMessages.ts**:
  * ```java
    @Override
    protected void writeCommonValidationMessagesTypeScriptFile(String code, Path filePath) {}
    ```
* **writeLocalization** **Localization.ts**:
  * ```java
    @Override
    protected void writeLocalization(String code, Path filePath) {}
    ```

<a id="TimeTypeMappings"></a>
## Time type mappings

Time type mappings are mapped to string by default.

```java
import java.time.*;

public record Foo(

        Instant instant,

        LocalDateTime localDateTime,

        ZonedDateTime zonedDateTime,

        Duration duration

) {


}
```

```typescript
/* tslint:disable */
/* eslint-disable */

export class Foo {
    instant: string;
    localDateTime: string;
    zonedDateTime: string;
    duration: string;
}
```

<a id="AnnotationProcessor"></a>
## Annotation processor

Disclaimer: in order for annotation processor to work model classes and generator configuration have to be compiled before annotation processor is run. In
practice this means that they have to be in separate modules.

Main advantage of this approach: easier extension, reusability and no requirement to run Maven to generate TypeScript!

Most, if not all, options available to TypeScript Generator Maven Plugin are also available to the annotation processor.

Setup:

1. In Maven module where Java model is defined add the following dependency:
   ```xml
   <dependency>
      <groupId>com.infobip</groupId>
      <artifactId>infobip-typescript-generator-extension-api</artifactId>
      <version>${infobip-typescript-generator-extension.version}</version>
   </dependency>
   ```
2. Configure the generator by extending TypeScriptFileGenerator:

   ```java
   public class SimpleTypeScriptFileGenerator extends TypeScriptFileGenerator {
   
       public SimpleTypeScriptFileGenerator(Path basePath) {
           super(basePath);
       }
   
       @Override
       public Input getInput() {
           return Input.from(Foo.class);
       }
   
       @Override
       public Path outputFilePath(Path basePath) {
           Path lib = basePath.getParent().getParent().resolve("dist");
   
           try {
               Files.createDirectories(lib);
           } catch (IOException e) {
               throw new UncheckedIOException(e);
           }
   
           return lib.resolve("Simple.ts");
       }
   
       @Override
        protected Path getDecoratorBasePath() {
            return getBasePath().getParent().getParent().resolve("src/main/typescript/decorators");
        }
   }
   ```
3. Custom java validation annotations must be marked with **@CustomTSDecorator** annotation. If a custom validation annotation name is not the same as a
   decorator name, you can specify decorator by using **typeScriptDecorator** annotation property.

4. Project only supports class-validator custom decorators [custom decorators](https://github.com/typestack/class-validator#custom-validation-decorators)

5. By overriding **getDecoratorBasePath()** you are specifying path to typescript decorators which relates to custom java validations:
   ```java
        @Override
        protected Path getDecoratorBasePath() {
            return getBasePath().getParent().getParent().resolve("src/main/typescript/decorators");
        }
   ```

6. Define a separate module where annotation processing will occur (this module depends on model module)
   with following dependency:
   ```xml
   <dependency>
      <groupId>com.infobip</groupId>
      <artifactId>infobip-typescript-generator-extension-api</artifactId>
      <version>${infobip-typescript-generator-extension.version}</version>
   </dependency>
   ```
7. Add the annotation configuration class (this is only used to trigger the annotation processing with annotation):
   ```java
   @GenerateTypescript(generator = SimpleTypeScriptFileGenerator.class)
   public class SimpleTypeScriptFileGeneratorConfiguration {
   }
   ```

For more complex examples look at
[infobip-typescript-generator-extension-model-showcase](infobip-typescript-generator-extension-model-showcase) and at
[infobip-typescript-generator-extension-annotation-processor-showcase](infobip-typescript-generator-extension-annotation-processor-showcase).

Generated typescript can be seen in [dist folder](infobip-typescript-generator-extension-annotation-processor-showcase/dist). In production you'd probably add
dist to .gitignore, here it's not mainly to be used a an showcase of how the end result looks like.

Since there's no maven plugin it's possible to run TypeScript Generator with multiple different configurations in same project!
Aforementioned showcase folders use this to test and showcase different parts of functionality.

<a id="Contributing"></a>
## Contributing

If you have an idea for a new feature or want to report a bug please use the issue tracker.

Pull requests are welcome!

<a id="License"></a>
## License

This library is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
