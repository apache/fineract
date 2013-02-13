/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.data;

import java.util.ArrayList;
import java.util.List;

public class ApiParameterError {

    /**
     * A developer friendly plain English description of why the HTTP error
     * response was returned from the API.
     */
    private String developerMessage;

    /**
     * A user friendly plain English description of why the HTTP error response
     * was returned from the API that can be presented to end users.
     */
    private String defaultUserMessage;

    /**
     * A code that can be used for globalisation support by client applications
     * of the API.
     */
    private String userMessageGlobalisationCode;

    /**
     * The name of the field or parameter passed to the API that this error
     * relates to.
     */
    private String parameterName;

    /**
     * The actual value of the parameter (if any) as passed to API.
     */
    private Object value;

    /**
     * Arguments related to the user error message.
     */
    private List<ApiErrorMessageArg> args = new ArrayList<ApiErrorMessageArg>();

    public static ApiParameterError generalError(String globalisationMessageCode, String defaultUserMessage,
            Object... defaultUserMessageArgs) {
        return new ApiParameterError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

    public static ApiParameterError resourceIdentifierNotFound(String globalisationMessageCode, String defaultUserMessage,
            Object... defaultUserMessageArgs) {
        return new ApiParameterError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

    public static ApiParameterError parameterError(final String globalisationMessageCode, final String defaultUserMessage,
            final String parameterName, final Object... defaultUserMessageArgs) {
        ApiParameterError error = new ApiParameterError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
        error.setParameterName(parameterName);
        return error;
    }

    protected ApiParameterError() {
        //
    }

    private ApiParameterError(final String globalisationMessageCode, final String defaultUserMessage, final Object[] defaultUserMessageArgs) {
        this.userMessageGlobalisationCode = globalisationMessageCode;
        this.developerMessage = defaultUserMessage;
        this.defaultUserMessage = defaultUserMessage;

        List<ApiErrorMessageArg> messageArgs = new ArrayList<ApiErrorMessageArg>();
        if (defaultUserMessageArgs != null) {
            for (Object object : defaultUserMessageArgs) {
                messageArgs.add(ApiErrorMessageArg.from(object));
            }
        }
        this.args = messageArgs;

        this.parameterName = "id";
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getDefaultUserMessage() {
        return defaultUserMessage;
    }

    public void setDefaultUserMessage(String defaultUserMessage) {
        this.defaultUserMessage = defaultUserMessage;
    }

    public String getUserMessageGlobalisationCode() {
        return userMessageGlobalisationCode;
    }

    public void setUserMessageGlobalisationCode(String userMessageGlobalisationCode) {
        this.userMessageGlobalisationCode = userMessageGlobalisationCode;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<ApiErrorMessageArg> getArgs() {
        return args;
    }

    public void setArgs(List<ApiErrorMessageArg> args) {
        this.args = args;
    }
}