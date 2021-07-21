/* tslint:disable */
/* eslint-disable */
import { IsDefined, IsNotEmpty, MinLength, ValidateNested, MaxLength } from 'class-validator';
import { CommonValidationMessages } from './CommonValidationMessages';
import { ComplexValidator } from './validators/ComplexValidator';

export class Foo {
    @MaxLength(2, { message: CommonValidationMessages.MaxLength(2) })
    @MinLength(1, { message: CommonValidationMessages.MinLength(1) })
    @ComplexValidator(100, { message: CommonValidationMessages.ComplexValidator() })
    @IsNotEmpty({ message: CommonValidationMessages.IsNotEmpty })
    @IsDefined({ message: CommonValidationMessages.IsDefined })
    @ValidateNested()
    bar: string;
}