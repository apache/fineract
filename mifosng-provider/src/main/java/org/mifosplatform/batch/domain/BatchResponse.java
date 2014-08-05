/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.domain;

import java.util.Set;

/**
 * Provides an object for separate HTTP responses in the Batch Response for
 * Batch API. It contains all the information about a particular HTTP response
 * in the Batch Response. Getter and Setter functions are also included to
 * access response data fields.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.api.BatchApiResource
 * @see org.mifosplatform.batch.service.BatchApiService
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
