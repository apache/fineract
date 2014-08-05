/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.exception;

/**
 * Provides members to hold the basic information about the exceptions raised in
 * commandStrategy classes.
 * 
 * @author Rishabh Shukla
 * 
 * @see ErrorHandler
 */
public final class ErrorInfo {

    private Integer statusCode;
    private Integer errorCode;
    private String message;

    /**
     * Constructor to initialize the members of this class.
     * 
     * @param statusCode
     * @param errorCode
     * @param message
     */
    public ErrorInfo(final Integer statusCode, final Integer errorCode, final String message) {
        super();
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Constructor so JSON serialization will work with out special Serialiazer
     */
    ErrorInfo() {
        super();
    }

    /**
     * Getter method to provide the statusCode for an object of this type.
     * 
     * @return Integer
     */
    public Integer getStatusCode() {
        return this.statusCode;
    }

    /**
     * Setter method to set the statusCode for an object of this type.
     * 
     * @param statusCode
     */
    public void setStatusCode(final Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Getter method to provide the errorCode for an object of this type.
     * 
     * @return Integer
     */
    public Integer getErrorCode() {
        return this.errorCode;
    }

    /**
     * Setter method to set the errorCode for an object of this type.
     * 
     * @param errorCode
     */
    public void setErrorCode(final Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Getter method to provide the message of the error for an object of this
     * type.
     * 
     * @return String
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Setter method to set the message of the error for an object of this type.
     * 
     * @param message
     */
    public void setMessage(final String message) {
        this.message = message;
    }
}
