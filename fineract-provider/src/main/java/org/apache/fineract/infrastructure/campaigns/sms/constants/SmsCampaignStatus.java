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
package org.apache.fineract.infrastructure.campaigns.sms.constants;

public enum SmsCampaignStatus {

    INVALID(-1, "smsCampaignStatus.invalid"), //
    PENDING(100, "smsCampaignStatus.pending"), //
    ACTIVE(300, "smsCampaignStatus.active"), //
    CLOSED(600, "smsCampaignStatus.closed");

    private final Integer value;
    private final String code;

    SmsCampaignStatus(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public static SmsCampaignStatus fromInt(final Integer statusValue) {
        switch (statusValue) {
            case 100:
                return PENDING;
            case 300:
                return ACTIVE;
            case 600:
                return CLOSED;
            default:
                return INVALID;
        }
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isActive() {
        return this.equals(ACTIVE);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isPending() {
        return this.equals(PENDING);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isClosed() {
        return this.equals(CLOSED);
    }
}
