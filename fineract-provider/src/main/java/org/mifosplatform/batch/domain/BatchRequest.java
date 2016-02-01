/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.domain;

import java.util.Set;

/**
 * Provides an object for separate HTTP requests in the Batch Request for Batch
 * API. A requestId is also included as data field which takes care of
 * dependency issues among various requests. This class also provides getter and
 * setter functions to access Batch Request data fields.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.api.BatchApiResource
 * @see Header
 */
public class BatchRequest {

    private Long requestId;
    private String relativeUrl;
    private String method;
    private Set<Header> headers;
    private Long reference;
    private String body;

    /**
     * Constructs a 'BatchRequest' with requestId, relativeUrl, method, headers,
     * reference and body of the incoming request.
     * 
     * @param requestId
     *            of HTTP request.
     * @param relativeUrl
     *            of HTTP request.
     * @param method
     *            of HTTP request.
     * @param headers
     *            of HTTP request.
     * @param reference
     *            of HTTP request.
     * @param body
     *            of HTTP request.
     * 
     * @see Header
     */
    public BatchRequest(Long requestId, String relativeUrl, String method, Set<Header> headers, Long reference, String body) {

        this.requestId = requestId;
        this.relativeUrl = relativeUrl;
        this.method = method;
        this.headers = headers;
        this.reference = reference;
        this.body = body;
    }

    /**
     * Constructs a default constructor of 'BatchRequest'
     */
    public BatchRequest() {

    }

    /**
     * Returns the value of 'requestId' of an object of this class.
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
     * Returns the value of 'relativeUrl' of an object of this class.
     * 
     * @return relativeUrl of the HTTP request.
     */
    public String getRelativeUrl() {
        return this.relativeUrl;
    }

    /**
     * Sets the value of 'relativeUrl' of an object of this class.
     * 
     * @param relativeUrl
     */
    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }

    /**
     * Returns the value of 'method' of an object of this class.
     * 
     * @return method of the HTTP request.
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Sets the value of 'method' of the object of this class.
     * 
     * @param method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the values of 'headers' of {@link Header} type of an object of
     * this class.
     * 
     * @return headers of the HTTP request.
     * @see Header
     */
    public Set<Header> getHeaders() {
        return this.headers;
    }

    /**
     * Sets the values of 'headers' of {@link Header} type of an object of this
     * class.
     * 
     * @param headers
     * @see Header
     */
    public void setHeaders(Set<Header> headers) {
        this.headers = headers;
    }

    /**
     * Returns the value of 'reference' of an object of this class
     * 
     * @return reference of the HTTP request
     */
    public Long getReference() {
        return this.reference;
    }

    /**
     * Sets the value of 'reference' of an object of this class.
     * 
     * @param reference
     */
    public void setReference(Long reference) {
        this.reference = reference;
    }

    /**
     * Returns the value of 'body' of an object of this class.
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
