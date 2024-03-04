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

import static lombok.AccessLevel.PROTECTED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(PROTECTED)
public class ApiGlobalErrorResponse {

    /**
     * A developer friendly plain English description of why the HTTP error response was returned from the API.
     */
    private String developerMessage;

    /**
     * The HTTP status code of the response.
     */
    private String httpStatusCode;

    /**
     * A user friendly plain English description of why the HTTP error response was returned from the API that can be
     * presented to end users.
     */
    private String defaultUserMessage;

    /**
     * A code that can be used for globalisation support by client applications of the API.
     */
    private String userMessageGlobalisationCode;

    /**
     * A list of zero or more of the actual reasons for the HTTP error should they be needed. Typically used to express
     * data validation errors with parameters passed to API.
     */

    private List<ApiParameterError> errors = new ArrayList<>();

    protected ApiGlobalErrorResponse() {}

    public static ApiGlobalErrorResponse create(int statusCode, String msgCode, String developerMessage, String defaultUserMessage,
            List<ApiParameterError> errors) {
        ApiGlobalErrorResponse response = new ApiGlobalErrorResponse();
        response.setHttpStatusCode(String.valueOf(statusCode));
        response.setUserMessageGlobalisationCode(msgCode);
        response.setDeveloperMessage(developerMessage);
        response.setDefaultUserMessage(defaultUserMessage);
        response.setErrors(errors);
        return response;
    }

    public static ApiGlobalErrorResponse create(int statusCode, String msgCode, String developerMessage, String defaultUserMessage) {
        return create(statusCode, msgCode, developerMessage, defaultUserMessage, null);
    }

    public static ApiGlobalErrorResponse unAuthenticated() {
        return create(SC_UNAUTHORIZED, "error.msg.not.authenticated", "Invalid authentication details were passed in api request.",
                "Unauthenticated. Please login.");
    }

    public static ApiGlobalErrorResponse invalidTenantIdentifier() {
        return create(SC_UNAUTHORIZED, "error.msg.invalid.tenant.identifier", "Invalid tenant details were passed in api request.",
                "Invalid tenant identifier provided with request.");
    }

    public static ApiGlobalErrorResponse invalidInstanceTypeMethod(final String method) {
        return create(SC_METHOD_NOT_ALLOWED, "error.msg.invalid.instance.type",
                "Invalid instance type called in api request for the method " + method,
                "Invalid method " + method + " used with request to this instance type.");
    }

    public static ApiGlobalErrorResponse loanIsLocked(final Long loanId) {
        String msg = "Loan is locked by the COB job. Loan ID: " + loanId;
        return create(SC_CONFLICT, "error.msg.loan.locked", msg, msg);
    }

    public static ApiGlobalErrorResponse conflict(String type, String identifier) {
        String details = "";
        if (type == null) {
            type = "unknown";
        } else {
            details = " on " + type;
        }
        if (identifier != null) {
            details += " [" + identifier + ']';
        }
        String msg = "The server is currently unable to handle the request due to concurrent modification " + details
                + ", please try again";
        return create(SC_CONFLICT, "error.msg.platform.service." + type + ".conflict", msg, msg);
    }

    public static ApiGlobalErrorResponse unAuthorized(final String defaultUserMessage) {
        String msgCode = "error.msg.not.authorized";
        final List<ApiParameterError> errors = List.of(ApiParameterError.generalError(msgCode, defaultUserMessage));

        return create(SC_FORBIDDEN, msgCode,
                "The user associated with credentials passed on this request does not have sufficient privileges to perform this action.",
                "Insufficient privileges to perform this action.", errors);
    }

    public static ApiGlobalErrorResponse domainRuleViolation(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        final List<ApiParameterError> errors = new ArrayList<>();
        errors.add(ApiParameterError.generalError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs));

        return create(SC_FORBIDDEN, "validation.msg.domain.rule.violation", "Request was understood but caused a domain rule violation.",
                "Errors contain reason for domain rule violation.", errors);
    }

    public static ApiGlobalErrorResponse dataIntegrityError(final String globalisationMessageCode, final String defaultUserMessage,
            final String parameterName, final Object... defaultUserMessageArgs) {
        final List<ApiParameterError> errors = new ArrayList<>();
        errors.add(ApiParameterError.parameterError(globalisationMessageCode, defaultUserMessage, parameterName, defaultUserMessageArgs));

        return create(SC_FORBIDDEN, globalisationMessageCode, "The request caused a data integrity issue to be fired by the database.",
                defaultUserMessage, errors);
    }

    public static ApiGlobalErrorResponse notFound(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        String msg = "The requested resource is not available.";
        final List<ApiParameterError> errors = new ArrayList<>();
        errors.add(ApiParameterError.resourceIdentifierNotFound(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs));

        return create(SC_NOT_FOUND, "error.msg.resource.not.found", msg, msg, errors);
    }

    public static ApiGlobalErrorResponse badClientRequest(final String globalisationMessageCode, final String defaultUserMessage,
            final List<ApiParameterError> errors) {
        return create(SC_BAD_REQUEST, globalisationMessageCode,
                "The request was invalid. This typically will happen due to validation errors which are provided.", defaultUserMessage,
                errors);
    }

    public static ApiGlobalErrorResponse badClientRequest(final String globalisationMessageCode, final String defaultUserMessage) {
        return badClientRequest(globalisationMessageCode, defaultUserMessage, Collections.emptyList());
    }

    public static ApiGlobalErrorResponse jobIsDisabled(final String globalisationMessageCode, final String defaultUserMessage) {
        return create(SC_FORBIDDEN, globalisationMessageCode, defaultUserMessage, defaultUserMessage);
    }

    public static ApiGlobalErrorResponse serverSideError(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        String msg = "An unexpected error occured on the platform server.";
        final List<ApiParameterError> errors = new ArrayList<>();
        errors.add(ApiParameterError.generalError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs));

        return create(SC_INTERNAL_SERVER_ERROR, "error.msg.platform.server.side.error", msg, msg, errors);
    }

    public static ApiGlobalErrorResponse serviceUnavailable(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        String msg = "The server is currently unable to handle the request , please try after some time.";
        final List<ApiParameterError> errors = new ArrayList<>();
        errors.add(ApiParameterError.generalError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs));

        return create(SC_SERVICE_UNAVAILABLE, "error.msg.platform.service.unavailable", msg, msg, errors);
    }

    @JsonProperty("errors")
    public List<ApiParameterError> getErrors() {
        return this.errors;
    }

    public String toJson() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }
}
