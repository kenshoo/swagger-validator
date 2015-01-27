package com.kenshoo.swagger.validator;

/**
 */
public class ErrorInfo {

    private final String errorCode;
    private final String errorMessage;

    public ErrorInfo(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
