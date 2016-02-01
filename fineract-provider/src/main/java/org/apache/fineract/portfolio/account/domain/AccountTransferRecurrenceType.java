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
package org.apache.fineract.portfolio.account.domain;

/**
 * Enum representation of loan status states.
 */
public enum AccountTransferRecurrenceType {

    INVALID(0, "accountTransferRecurrenceType.invalid"), //
    PERIODIC(1, "accountTransferRecurrenceType.periodic"), //
    AS_PER_DUES(2, "accountTransferRecurrenceType.as.per.dues"); //

    private final Integer value;
    private final String code;

    public static AccountTransferRecurrenceType fromInt(final Integer statusValue) {

        AccountTransferRecurrenceType enumeration = AccountTransferRecurrenceType.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = AccountTransferRecurrenceType.PERIODIC;
            break;
            case 2:
                enumeration = AccountTransferRecurrenceType.AS_PER_DUES;
            break;
        }
        return enumeration;
    }

    private AccountTransferRecurrenceType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final AccountTransferRecurrenceType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isPeriodicRecurrence() {
        return this.value.equals(AccountTransferRecurrenceType.PERIODIC.getValue());
    }

    public boolean isDuesRecurrence() {
        return this.value.equals(AccountTransferRecurrenceType.AS_PER_DUES.getValue());
    }

}