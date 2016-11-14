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

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object representing a SMS message.
 */
public class SmsData {

    private final Long id;
    private final Long groupId;
    private final Long clientId;
    private final Long staffId;
    private final EnumOptionData status;
    private final String mobileNo;
    private final String message;
    private final Long providerId;
    private final String campaignName;

    public static SmsData instance(final Long id, final Long groupId, final Long clientId, final Long staffId, final EnumOptionData status,
            final String mobileNo, final String message, final Long providerId, final String camapignName) {
        return new SmsData(id, groupId, clientId, staffId, status, mobileNo, message, providerId, camapignName);
    }

    private SmsData(final Long id, final Long groupId, final Long clientId, final Long staffId, final EnumOptionData status,
            final String mobileNo, final String message, final Long providerId, final String camapignName) {
        this.id = id;
        this.groupId = groupId;
        this.clientId = clientId;
        this.staffId = staffId;
        this.status = status;
        this.mobileNo = mobileNo;
        this.message = message;
        this.providerId = providerId;
        this.campaignName = camapignName;
    }

    public Long getId() {
        return this.id;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public EnumOptionData getStatus() {
        return this.status;
    }

    public String getMobileNo() {
        return this.mobileNo;
    }

    public String getMessage() {
        return this.message;
    }

    public Long getProviderId() {
        return this.providerId;
    }

    public String getCampaignName() {
        return this.campaignName;
    }

}