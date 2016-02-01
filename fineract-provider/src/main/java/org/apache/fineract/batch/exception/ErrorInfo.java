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
package org.apache.fineract.batch.exception;

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
