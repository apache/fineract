/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private final transient SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

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
                if(object instanceof Date){
                    final String formattedDate = dateFormatter.format(object);
                    messageArgs.add(ApiErrorMessageArg.from(formattedDate));
                } else {
                    messageArgs.add(ApiErrorMessageArg.from(object));
                }
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