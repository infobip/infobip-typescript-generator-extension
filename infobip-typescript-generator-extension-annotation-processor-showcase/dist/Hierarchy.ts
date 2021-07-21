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
