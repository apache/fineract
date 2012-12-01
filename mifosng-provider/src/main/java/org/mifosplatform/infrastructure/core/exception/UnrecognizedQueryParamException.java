package org.mifosplatform.infrastructure.core.exception;

public class UnrecognizedQueryParamException extends RuntimeException {

    private final String queryParamKey;
    private final String queryParamValue;

    public UnrecognizedQueryParamException(String queryParamKey, String queryParamValue) {
        this.queryParamKey = queryParamKey;
        this.queryParamValue = queryParamValue;
    }

    public String getQueryParamKey() {
        return queryParamKey;
    }

    public String getQueryParamValue() {
        return queryParamValue;
    }
}