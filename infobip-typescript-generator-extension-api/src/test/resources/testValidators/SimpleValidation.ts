import { registerDecorator, ValidationOptions, ValidationArguments } from 'class-validator';

export function SimpleValidation(validationOptions?: ValidationOptions) {
    return function (object: Object, propertyName: string) {
        registerDecorator({
            name: 'simpleValidation',
            target: object.constructor,
            propertyName: propertyName,
            constraints: [],
            options: validationOptions,
            validator: {
                validate(value: any, args: ValidationArguments) {;
                    return typeof value === 'string' && value.length > 100;
                },
            },
        });
    };
}