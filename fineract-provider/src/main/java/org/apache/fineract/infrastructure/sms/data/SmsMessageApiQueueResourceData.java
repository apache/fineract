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
package org.apache.fineract.infrastructure.sms.data;

import java.util.Collection;

import com.google.gson.Gson;

/**
 * Immutable data object representing the API request body sent in the POST
 * request to the "/queue" resource
 **/
public class SmsMessageApiQueueResourceData {

    private Long internalId;
    private String tenantId;
    private String createdOnDate;
    private String sourceAddress;
    private String mobileNumber;
    private String message;
    private Long providerId;

    /**
     * SmsMessageApiQueueResourceData constructor
     **/
    private SmsMessageApiQueueResourceData(Long internalId, String mifosTenantIdentifier, String createdOnDate, String sourceAddress,
            String mobileNumber, String message, Long providerId) {
        this.internalId = internalId;
        this.tenantId = mifosTenantIdentifier;
        this.createdOnDate = createdOnDate;
        this.sourceAddress = sourceAddress;
        this.mobileNumber = mobileNumber;
        this.message = message;
        this.providerId = providerId;
    }

    /**
     * SmsMessageApiQueueResourceData constructor
     **/
    protected SmsMessageApiQueueResourceData() {}

    /**
     * @return a new instance of the SmsMessageApiQueueResourceData class
     **/
    public static final SmsMessageApiQueueResourceData instance(Long internalId, String mifosTenantIdentifier, String createdOnDate,
            String sourceAddress, String mobileNumber, String message, Long providerId) {

        return new SmsMessageApiQueueResourceData(internalId, mifosTenantIdentifier, createdOnDate, sourceAddress, mobileNumber, message,
                providerId);
    }

    /**
     * @return the internalId
     */
    public Long getInternalId() {
        return internalId;
    }

    /**
     * @return the mifosTenantIdentifier
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @return the createdOnDate
     */
    public String getCreatedOnDate() {
        return createdOnDate;
    }

    /**
     * @return the sourceAddress
     */
    public String getSourceAddress() {
        return sourceAddress;
    }

    /**
     * @return the mobileNumber
     */
    public String getMobileNumber() {
        return mobileNumber;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the providerId
     */
    public Long getproviderId() {
        return providerId;
    }

    /**
     * Returns the JSOPN representation of the current object.
     * @return the JSON representation of the current object
     */
    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * @return JSON representation of the object
     **/
    public static String toJsonString(Collection<SmsMessageApiQueueResourceData> smsResourceData) {
        Gson gson = new Gson();

        return gson.toJson(smsResourceData);
    }
}
