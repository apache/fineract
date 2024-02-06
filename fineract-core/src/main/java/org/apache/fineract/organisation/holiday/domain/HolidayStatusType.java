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
package org.apache.fineract.organisation.holiday.domain;

/**
 * Enum representation of {@link Holiday} status states.
 */
public enum HolidayStatusType {

    INVALID(0, "holidayStatusType.invalid"), //
    PENDING_FOR_ACTIVATION(100, "holidayStatusType.pending.for.activation"), //
    ACTIVE(300, "holidayStatusType.active"), //
    DELETED(600, "savingsAccountStatusType.transfer.in.progress");

    private final Integer value;
    private final String code;

    public static HolidayStatusType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 100:
                return PENDING_FOR_ACTIVATION;
            case 300:
                return ACTIVE;
            case 600:
                return DELETED;
            default:
                return INVALID;
        }
    }

    HolidayStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isPendingActivation() {
        return this.value.equals(HolidayStatusType.PENDING_FOR_ACTIVATION.getValue());
    }

    public boolean isActive() {
        return this.value.equals(HolidayStatusType.ACTIVE.getValue());
    }

    public boolean isDeleted() {
        return this.value.equals(HolidayStatusType.DELETED.getValue());
    }
}
