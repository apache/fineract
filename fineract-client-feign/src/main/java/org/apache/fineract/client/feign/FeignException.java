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
package org.apache.fineract.client.feign;

import feign.Request;
import java.nio.charset.Charset;

/**
 * Base exception class for Feign client exceptions.
 */
public class FeignException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final Request request;
    private final byte[] responseBody;
    private final String developerMessage;
    private final String userMessage;

    protected FeignException(int status, String message, Request request) {
        this(status, message, request, (byte[]) null);
    }

    protected FeignException(int status, String message, Request request, Throwable cause) {
        this(status, message, request, null, cause);
    }

    protected FeignException(int status, String message, Request request, byte[] responseBody) {
        super(message);
        this.status = status;
        this.request = request;
        this.responseBody = responseBody;
        this.developerMessage = null;
        this.userMessage = null;
    }

    protected FeignException(int status, String message, Request request, byte[] responseBody, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.request = request;
        this.responseBody = responseBody;
        this.developerMessage = null;
        this.userMessage = null;
    }

    public FeignException(int status, String message, Request request, byte[] responseBody, String developerMessage, String userMessage) {
        super(message);
        this.status = status;
        this.request = request;
        this.responseBody = responseBody;
        this.developerMessage = developerMessage;
        this.userMessage = userMessage;
    }

    public int status() {
        return status;
    }

    public Request request() {
        return request;
    }

    public byte[] responseBody() {
        return responseBody;
    }

    public String responseBodyAsString() {
        return responseBody != null ? new String(responseBody, Charset.defaultCharset()) : null;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("status ").append(status);

        if (userMessage != null) {
            sb.append(": ").append(userMessage);
        }

        if (developerMessage != null) {
            sb.append(" (").append(developerMessage).append(")");
        }

        if (super.getMessage() != null && userMessage == null && developerMessage == null) {
            sb.append(": ").append(super.getMessage());
        }

        return sb.toString();
    }
}
