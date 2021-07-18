/* tslint:disable */
/* eslint-disable */
import { IsDefined, IsNotEmpty, MinLength, ValidateNested, MaxLength } from 'class-validator';
import { CommonValidationMessages } from './CommonValidationMessages';

export class Foo {
    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
    @SimpleCustomValidation('bla', 3, { message: CommonValidationMessages.SimpleCustomValidation() })
    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
    @IsDefined({ message: CommonValidationMessages.IsDefined })
    @ValidateNested()
    bar: string;
}