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
package org.apache.fineract.infrastructure.campaigns.sms.domain;

import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class SmsCampaignStatusEnumerations {
    public static EnumOptionData status(final Integer statusId) {
        return status(SmsCampaignStatus.fromInt(statusId));
    }

    public static EnumOptionData status(final SmsCampaignStatus status) {
        EnumOptionData optionData = new EnumOptionData(SmsCampaignStatus.INVALID.getValue().longValue(),
                SmsCampaignStatus.INVALID.getCode(), "Invalid");
        switch (status) {
            case INVALID:
                optionData = new EnumOptionData
                (SmsCampaignStatus.INVALID.getValue().longValue(),
                        SmsCampaignStatus.INVALID.getCode(), "Invalid");
                break;
            case PENDING:
                optionData = new EnumOptionData(SmsCampaignStatus.PENDING.getValue().longValue(),
                        SmsCampaignStatus.PENDING.getCode(), "Pending");
                break;
            case ACTIVE:
                optionData = new EnumOptionData(SmsCampaignStatus.ACTIVE.getValue().longValue(), SmsCampaignStatus.ACTIVE.getCode(),
                        "active");
                break;
            case CLOSED:
                optionData = new EnumOptionData(SmsCampaignStatus.CLOSED.getValue().longValue(),
                        SmsCampaignStatus.CLOSED.getCode(), "closed");
                break;

        }

        return optionData;
    }
}
