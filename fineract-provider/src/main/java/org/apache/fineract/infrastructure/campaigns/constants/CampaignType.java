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
package org.apache.fineract.infrastructure.campaigns.constants;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum CampaignType {
    INVALID(0, "campaignType.invalid"), SMS(1, "campaignType.sms"), NOTIFICATION(2, "campaignType.notification");

    private Integer value;
    private String code;

    private CampaignType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static CampaignType fromInt(final Integer typeValue) {
        CampaignType type = null;
        switch (typeValue) {
            case 0:
                type = INVALID;
            break;
            case 1:
                type = SMS;
            break;
            case 2:
                type = NOTIFICATION;
            break;
        }
        return type;
    }
    
    public static EnumOptionData campaignType(final Integer campaignTypeId) {
        return campaignType(CampaignType.fromInt(campaignTypeId));
    }

    public static EnumOptionData campaignType(final CampaignType campaignType) {
        EnumOptionData optionData = new EnumOptionData(CampaignType.INVALID.getValue().longValue(), CampaignType.INVALID.getCode(),
                "Invalid");
        switch (campaignType) {
            case INVALID:
                optionData = new EnumOptionData(CampaignType.INVALID.getValue().longValue(), CampaignType.INVALID.getCode(), "Invalid");
            break;
            case SMS:
                optionData = new EnumOptionData(CampaignType.SMS.getValue().longValue(), CampaignType.SMS.getCode(), "SMS");
            break;
            case NOTIFICATION:
                optionData = new EnumOptionData(CampaignType.NOTIFICATION.getValue().longValue(), CampaignType.NOTIFICATION.getCode(), "NOTIFICATION");
            break;
        }
        return optionData;
    }

    public boolean isSms() {
        return this.value.equals(CampaignType.SMS.getValue());
    }

    public boolean isNotificaion() {
        return this.value.equals(CampaignType.NOTIFICATION.getValue());
    }
}
