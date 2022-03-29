package com.infobip.typescript.transformer;

import java.util.Optional;

class ParameterizedTypeClasses {

    private final Class<?> type;
    private final Optional<Class<?>> typeArgument;

    ParameterizedTypeClasses(Class<?> type, Optional<Class<?>> typeArgument) {
        this.type = type;
        this.typeArgument = typeArgument;
    }

    Class<?> getType() {
        return type;
    }

    Optional<Class<?>> getTypeArgument() {
        return typeArgument;
    }
}
