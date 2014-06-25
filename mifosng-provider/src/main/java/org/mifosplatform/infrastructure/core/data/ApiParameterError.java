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
    private List<ApiErrorMessageArg> args = new ArrayList<>();

    public static ApiParameterError generalError(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        return new ApiParameterError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

    public static ApiParameterError resourceIdentifierNotFound(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        return new ApiParameterError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

    public static ApiParameterError parameterError(final String globalisationMessageCode, final String defaultUserMessage,
            final String parameterName, final Object... defaultUserMessageArgs) {
        final ApiParameterError error = new ApiParameterError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
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

        final List<ApiErrorMessageArg> messageArgs = new ArrayList<>();
        if (defaultUserMessageArgs != null) {
            for (final Object object : defaultUserMessageArgs) {
                messageArgs.add(ApiErrorMessageArg.from(object));
            }
        }
        this.args = messageArgs;

        this.parameterName = "id";
    }

    public String getDeveloperMessage() {
        return this.developerMessage;
    }

    public void setDeveloperMessage(final String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getDefaultUserMessage() {
        return this.defaultUserMessage;
    }

    public void setDefaultUserMessage(final String defaultUserMessage) {
        this.defaultUserMessage = defaultUserMessage;
    }

    public String getUserMessageGlobalisationCode() {
        return this.userMessageGlobalisationCode;
    }

    public void setUserMessageGlobalisationCode(final String userMessageGlobalisationCode) {
        this.userMessageGlobalisationCode = userMessageGlobalisationCode;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public void setParameterName(final String parameterName) {
        this.parameterName = parameterName;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public List<ApiErrorMessageArg> getArgs() {
        return this.args;
    }

    public void setArgs(final List<ApiErrorMessageArg> args) {
        this.args = args;
    }
}