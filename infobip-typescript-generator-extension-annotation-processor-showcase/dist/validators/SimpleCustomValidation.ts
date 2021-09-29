import { registerDecorator, ValidationArguments, ValidationOptions } from 'class-validator';

export function SimpleCustomValidation(validationOptions?: ValidationOptions) {
    return function (object: Object, propertyName: string) {
        registerDecorator({
            name: 'simpleCustomValidator',
            target: object.constructor,
            propertyName: propertyName,
            constraints: [],
            options: validationOptions,
            validator: {
                validate(value: any, args: ValidationArguments) {
                    return typeof value === 'string' && value.length > 100;
                }
            }
        });
    };
}