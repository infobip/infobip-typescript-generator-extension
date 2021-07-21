import { localize } from './Localization';

const getMaxLengthMessage = (value: number) => localize('must be less than or equal to {value}', { value });
const getMinLengthMessage = (value: number) => localize('must be greater than or equal to {value}', { value });

export const CommonValidationMessages = {

    IsDefined: localize('must not be null'),
    IsNotEmpty: localize('must not be blank'),
    Max: getMaxLengthMessage,
    MaxLength: getMaxLengthMessage,
    ArrayMaxSize: getMaxLengthMessage,
    Min: getMinLengthMessage,
    MinLength: getMinLengthMessage,
    ArrayMinSize: getMinLengthMessage,
};