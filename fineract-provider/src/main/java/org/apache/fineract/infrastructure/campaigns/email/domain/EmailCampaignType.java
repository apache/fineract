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
package org.apache.fineract.infrastructure.campaigns.email.domain;

public enum EmailCampaignType {

    DIRECT(1, "emailCampaignStatusType.direct"), //
    SCHEDULE(2, "emailCampaignStatusType.schedule"), //
    TRIGGERED(3, "emailCampaignStatusType.triggered"); //

    private final Integer value;
    private final String code;

    EmailCampaignType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static EmailCampaignType fromInt(final Integer typeValue) {
        return switch (typeValue) {
            case 1 -> DIRECT;
            case 2 -> SCHEDULE;
            case 3 -> TRIGGERED;
            default -> null;
        };
    }

    public boolean isDirect() {
        return this.value.equals(EmailCampaignType.DIRECT.getValue());
    }

    public boolean isSchedule() {
        return this.value.equals(EmailCampaignType.SCHEDULE.getValue());
    }

    public boolean isTriggered() {
        return this.value.equals(EmailCampaignType.TRIGGERED.getValue());
    }
}
