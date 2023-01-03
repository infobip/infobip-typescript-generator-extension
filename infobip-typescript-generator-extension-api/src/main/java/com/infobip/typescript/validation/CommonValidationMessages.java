package com.infobip.typescript.validation;

public class CommonValidationMessages {

    public static final String COMMON_VALIDATION_MESSAGES_CLASS_NAME = "CommonValidationMessages";
    public static final String COMMON_VALIDATION_MESSAGES_FILE_NAME = COMMON_VALIDATION_MESSAGES_CLASS_NAME + ".ts";
    public static final String COMMON_VALIDATION_MESSAGES_SOURCE_CODE =
        """
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
            """;
}
