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

import com.google.gson.Gson;
import java.util.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable data object representing the API request body sent in the POST request to the "/queue" resource
 **/
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SmsMessageApiQueueResourceData {

    private Long internalId;
    private String tenantId;
    private String createdOnDate;
    private String sourceAddress;
    private String mobileNumber;
    private String message;
    private Long providerId;

    /**
     * @return a new instance of the SmsMessageApiQueueResourceData class
     **/
    public static final SmsMessageApiQueueResourceData instance(Long internalId, String mifosTenantIdentifier, String createdOnDate,
            String sourceAddress, String mobileNumber, String message, Long providerId) {

        return new SmsMessageApiQueueResourceData().setInternalId(internalId).setTenantId(mifosTenantIdentifier)
                .setCreatedOnDate(createdOnDate).setSourceAddress(sourceAddress).setMobileNumber(mobileNumber).setMessage(message)
                .setProviderId(providerId);
    }

    /**
     * Returns the JSOPN representation of the current object.
     *
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
