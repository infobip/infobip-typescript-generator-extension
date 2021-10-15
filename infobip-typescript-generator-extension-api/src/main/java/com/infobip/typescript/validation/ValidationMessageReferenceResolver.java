package com.infobip.typescript.validation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

class ValidationMessageReferenceResolver {

    private final String customMessageSource;

    ValidationMessageReferenceResolver(String customMessageSource) {
        this.customMessageSource = customMessageSource;
    }

    String getMessageReference(Supplier<String> messageProvider, String identifier) {
        String message = messageProvider.get();

        if (message.startsWith("{") && !message.startsWith("{javax")) {
            return handleCustomMessage(message);
        }

        return "CommonValidationMessages." + identifier;
    }

    Optional<String> getCustomMessageReferenceIfExist(Supplier<String> messageProvider) {
        String message = messageProvider.get();

        if (message.startsWith("{") && !message.startsWith("{javax")) {
            return Optional.of(handleCustomMessage(message));
        }

        return Optional.empty();
    }

    private String handleCustomMessage(String message) {
        if (Objects.isNull(customMessageSource)) {
            throw new IllegalStateException(
                "CustomMessageSource must not be null. Specify customMessageSource in ClassValidatorDecoratorExtension constructor");
        }

        return customMessageSource + "." + message.substring(1, message.length() - 1);
    }

}
