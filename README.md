# Infobip TypeScript Generator Extension

[![](https://github.com/infobip/infobip-typescript-generator-extension/workflows/maven/badge.svg)](https://github.com/infobip/infobip-typescript-generator-extension/actions?query=workflow%3Amaven)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.infobip/infobip-typescript-generator-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.infobip/infobip-typescript-generator-extension)
[![Coverage Status](https://coveralls.io/repos/github/infobip/infobip-typescript-generator-extension/badge.svg?branch=master)](https://coveralls.io/github/infobip/infobip-typescript-generator-extension?branch=master)
[![Known Vulnerabilities](https://snyk.io/test/github/infobip/infobip-typescript-generator-extension/badge.svg)](https://snyk.io/test/github/infobip/infobip-typescript-generator-extension)

Library which provides new features on top of [TypeScript Generator](https://github.com/vojtechhabarta/typescript-generator): 
annotation processor support (which eliminates the requirement for a maven plugin) and bean validation Java annotations 
to TypeScript decorators translation. 

## Contents

1. [Bean Validation to class-validator translation](#BeanValidationToClassValidatorTranslation)
    * [Simple Object](#SimpleObject)
    * [Hierarchy](#Hierarchy)
1. [Annotation processor](#AnnotationProcessor)
1. [Contributing](#Contributing)
1. [License](#License)

## <a name="BeanValidationToClassValidatorTranslation"></a> Bean Validation to class-validator translation

### <a name="SimpleObject"></a> Simple Object

Input:

```java
@Value
public class Foo {

    @Size(min = 1, max = 2)
    @NotEmpty
    @NotNull
    @Valid
    private final String bar;

}
```

Result:
```typescript
import { IsDefined, IsNotEmpty, MinLength, ValidateNested, MaxLength } from 'class-validator';

export class Foo {
    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
    @IsDefined({ message: CommonValidationMessages.IsDefined })
    @ValidateNested()
    bar: string;
}
```

### <a name="Hierarchy"></a> Hierarchy

[class-transformer](https://github.com/typestack/class-transformer) together with [Infobip Jackson Extension](https://github.com/infobip/infobip-jackson-extension) is used to handle hierarchies.

Example for a hierarchy is a multi level hierarchy for inbound and outbound messages.

Input:

```java
@Getter
@AllArgsConstructor
enum Channel {
    SMS(InboundSmsMessage.class, OutboundSmsMessage.class);

    private final Class<? extends InboundMessage> inboundMessageType;
    private final Class<? extends OutboundMessage> outboundMessageType;
}

@Getter
@AllArgsConstructor
enum Direction implements TypeProvider {
    INBOUND(InboundMessage.class),
    OUTBOUND(OutboundMessage.class);

    private final Class<? extends Message> type;
}

@Getter
@AllArgsConstructor
public enum CommonContentType implements TypeProvider, ContentType {
    TEXT(TextContent.class);

    private final Class<? extends CommonContent> type;
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

@Value
class TextContent implements CommonContent {

   @NotNull
   @NotEmpty
   private final String text;

   @Override
   public CommonContentType getType() {
      return CommonContentType.TEXT;
   }
}

@Value
class InboundSmsMessage implements InboundMessage {

   private final CommonContent content;

   @Override
   public Channel getChannel() {
      return Channel.SMS;
   }
}

@Value
class OutboundSmsMessage implements OutboundMessage {

   private final CommonContent content;

   @Override
   public Channel getChannel() {
      return Channel.SMS;
   }
}
```

Result:
```typescript
import 'reflect-metadata';
import { Type } from 'class-transformer';
import { IsDefined, IsNotEmpty } from 'class-validator';

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

export interface CommonContent extends SimpleJsonHierarchy<CommonContentType>, Content<CommonContentType> {
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

export interface SimpleJsonHierarchy<E> {
    type: E;
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

## <a name="AnnotationProcessor"></a> Annotation processor

Instead of a Maven Plugin annotation processor is available with one big caveat: model classes and generator 
configuration have to be compiled before annotation processor is run. 
In practice this means that they have to be in separate modules.

Most, if not all, options available to TypeScript Generator Maven Plugin are also available with this approach.

Setup:
1. In Maven module where Java model is defined add the following dependency:
   ```xml
   <dependency>
      <groupId>com.infobip</groupId>
      <artifactId>infobip-typescript-generator-extension-api</artifactId>
      <version>${infobip-typescript-generator-extension.version}</version>
   </dependency>
   ```
1. Configure the generator by extending TypeScriptFileGenerator:

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
   }
   ```
   
1. Define a separate module where annotation processing will occur (this module depends on model module) 
   with following dependency:
   ```xml
   <dependency>
      <groupId>com.infobip</groupId>
      <artifactId>infobip-typescript-generator-extension-api</artifactId>
      <version>${infobip-typescript-generator-extension.version}</version>
   </dependency>
   ```
1. Add the annotation configuration class (this is only used to trigger the annotation processing with annotation):
   ```java
   @GenerateTypescript(generator = SimpleTypeScriptFileGenerator.class)
   public class BasicTypeScriptFileGeneratorConfiguration {
   }
   ```
   
For more complex examples look at 
[infobip-typescript-generator-extension-model-showcase](infobip-typescript-generator-extension-model-showcase) and at
[infobip-typescript-generator-extension-annotation-processor-showcase](infobip-typescript-generator-extension-annotation-processor-showcase).
Generated typescript can be seen in [dist folder](infobip-typescript-generator-extension-annotation-processor-showcase/dist).
In production you'd probably add dist to .gitignore, here it's not mainly to be used a an showcase of how the end result looks like.

## <a name="Contributing"></a> Contributing

If you have an idea for a new feature or want to report a bug please use the issue tracker.

Pull requests are welcome!

## <a name="License"></a> License

This library is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
