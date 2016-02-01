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
package org.apache.fineract.batch.domain;

import java.util.Set;

/**
 * Provides an object for separate HTTP responses in the Batch Response for
 * Batch API. It contains all the information about a particular HTTP response
 * in the Batch Response. Getter and Setter functions are also included to
 * access response data fields.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.apache.fineract.batch.api.BatchApiResource
 * @see org.apache.fineract.batch.service.BatchApiService
 * @see Header
 */
public class BatchResponse {

    private Long requestId;
    private Integer statusCode;
    private Set<Header> headers;
    private String body;

    /**
     * Constructs a 'BatchResponse' with requestId, statusCode, headers and body
     * of the HTTP requests.
     * 
     * @param requestId
     * @param statusCode
     * @param headers
     * @param body
     * @see Header
     */
    public BatchResponse(Long requestId, Integer statusCode, Set<Header> headers, String body) {
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Constructs a default constructor of 'BatchResponse'
     */
    public BatchResponse() {

    }

    /**
     * Returns the 'requestId' of an object of this class.
     * 
     * @return requestId of the HTTP request.
     */
    public Long getRequestId() {
        return this.requestId;
    }

    /**
     * Sets the value of 'requestId' of an object of this class.
     * 
     * @param requestId
     */
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    /**
     * Returns the 'statusCode' of an object of this class.
     * 
     * @return statusCode of the HTTP request.
     */
    public Integer getStatusCode() {
        return this.statusCode;
    }

    /**
     * Sets the value of 'statusCode' of an object of this class.
     * 
     * @param statusCode
     */
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the 'headers' of {@link Header} type of an object of this class.
     * 
     * @return headers of the HTTP request.
     * @see Header
     */
    public Set<Header> getHeaders() {
        return this.headers;
    }

    /**
     * Sets the value of 'headers' of {@link Header} type of an object of this
     * class.
     * 
     * @param headers
     *            of {@link Header} Type
     * @see Header
     */
    public void setHeaders(Set<Header> headers) {
        this.headers = headers;
    }

    /**
     * Returns the 'body' of an object of this class.
     * 
     * @return body of the HTTP request.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Sets the value of 'body' of an object of this class.
     * 
     * @param body
     */
    public void setBody(String body) {
        this.body = body;
    }
}
