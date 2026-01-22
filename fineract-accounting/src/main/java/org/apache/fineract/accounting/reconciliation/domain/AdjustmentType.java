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
package org.apache.fineract.accounting.reconciliation.domain;

public enum AdjustmentType {

    BANK_CHARGE(1, "adjustmentType.bankCharge"), //
    BANK_ERROR(2, "adjustmentType.bankError"), //
    BOOK_ERROR(3, "adjustmentType.bookError"), //
    TIMING_DIFFERENCE(4, "adjustmentType.timingDifference"), //
    UNRECORDED_DEPOSIT(5, "adjustmentType.unrecordedDeposit"), //
    UNRECORDED_WITHDRAWAL(6, "adjustmentType.unrecordedWithdrawal"), //
    OTHER(99, "adjustmentType.other");

    private final Integer value;
    private final String code;

    AdjustmentType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static AdjustmentType fromInt(final Integer value) {
        if (value == null) {
            return null;
        }
        for (AdjustmentType type : AdjustmentType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    public static AdjustmentType fromString(final String typeString) {
        if (typeString == null) {
            return null;
        }
        for (AdjustmentType type : AdjustmentType.values()) {
            if (type.name().equalsIgnoreCase(typeString)) {
                return type;
            }
        }
        return null;
    }
}
