/* tslint:disable */
/* eslint-disable */
import { IsDefined, IsNotEmpty, MaxLength, MinLength, ValidateNested } from 'class-validator';
import { CommonValidationMessages } from './CommonValidationMessages';
import { SimpleCustomValidation } from './validators/SimpleCustomValidation';

export class Foo {
    @SimpleCustomValidation({ message: 'must be valid element' })
    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
    @IsDefined({ message: CommonValidationMessages.IsDefined })
    @ValidateNested()
    bar: string;
}