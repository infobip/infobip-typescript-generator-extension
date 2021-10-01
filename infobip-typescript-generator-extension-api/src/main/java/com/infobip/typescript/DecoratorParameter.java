package com.infobip.typescript;

public class DecoratorParameter {

    private final String parameterName;
    private final Object parameterValue;

    public DecoratorParameter(String parameterName, Object parameterValue) {
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Object getParameterValue() {
        return parameterValue;
    }

}
