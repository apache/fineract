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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object representing a SMS message.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class SmsData {

    private Long id;
    private Long groupId;
    private Long clientId;
    private Long staffId;
    private EnumOptionData status;
    private String mobileNo;
    private String message;
    private Long providerId;
    private String campaignName;

    public static SmsData instance(final Long id, final Long groupId, final Long clientId, final Long staffId, final EnumOptionData status,
            final String mobileNo, final String message, final Long providerId, final String camapignName) {
        return new SmsData().setId(id).setGroupId(groupId).setClientId(clientId).setStaffId(staffId).setStatus(status).setMobileNo(mobileNo)
                .setMessage(message).setProviderId(providerId).setCampaignName(camapignName);
    }
}
