import * as _ from 'lodash';

import { I18n, Localization } from 'ib-i18n';

const __: Localization = _.partial(I18n.__, 'Global');
const getMaxLengthMessage = (value: number) => __('must be less than or equal to {value}', {value});
const getMinLengthMessage = (value: number) => __('must be greater than or equal to {value}', {value});

export const CommonValidationMessages = {

    IsDefined: __('must not be null'),
    IsNotEmpty: __('must not be blank'),
    Max: getMaxLengthMessage,
    MaxLength: getMaxLengthMessage,
    ArrayMaxSize: getMaxLengthMessage,
    Min: getMinLengthMessage,
    MinLength: getMinLengthMessage,
    ArrayMinSize: getMinLengthMessage,
};
