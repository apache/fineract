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
package org.apache.fineract.portfolio.savings;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

/**
 * An enumeration of different transactions that can occur on a
 * {@link SavingsAccount}.
 */
public enum DepositAccountType {

    INVALID(0, "depositAccountType.invalid"), //
    SAVINGS_DEPOSIT(100, "depositAccountType.savingsDeposit"), //
    FIXED_DEPOSIT(200, "depositAccountType.fixedDeposit"), //
    RECURRING_DEPOSIT(300, "depositAccountType.recurringDeposit"), //
    CURRENT_DEPOSIT(400, "depositAccountType.currentDeposit");

    private final Integer value;
    private final String code;
    private static final String SPACE = " ";

    private DepositAccountType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static DepositAccountType fromInt(final Integer transactionType) {

        if (transactionType == null) { return DepositAccountType.INVALID; }

        DepositAccountType depositAccountType = DepositAccountType.INVALID;
        switch (transactionType) {
            case 100:
                depositAccountType = DepositAccountType.SAVINGS_DEPOSIT;
            break;
            case 200:
                depositAccountType = DepositAccountType.FIXED_DEPOSIT;
            break;
            case 300:
                depositAccountType = DepositAccountType.RECURRING_DEPOSIT;
            break;
            case 400:
                depositAccountType = DepositAccountType.CURRENT_DEPOSIT;
            break;
        }
        return depositAccountType;
    }

    public boolean isSavingsDeposit() {
        return this.value.equals(DepositAccountType.SAVINGS_DEPOSIT.getValue());
    }

    public boolean isFixedDeposit() {
        return this.value.equals(DepositAccountType.FIXED_DEPOSIT.getValue());
    }

    public boolean isRecurringDeposit() {
        return this.value.equals(DepositAccountType.RECURRING_DEPOSIT.getValue());
    }

    public boolean isCurrentDeposit() {
        return this.value.equals(DepositAccountType.CURRENT_DEPOSIT.getValue());
    }

    @Override
    public String toString() {
        return StringUtils.replace(code, "_", SPACE);
    }

    public String resourceName() {

        String resourceName = "INVALID";

        switch (this) {
            case FIXED_DEPOSIT:
                resourceName = DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME;
            break;
            case RECURRING_DEPOSIT:
                resourceName = DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME;
            break;
            case SAVINGS_DEPOSIT:
                resourceName = DepositsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
            break;
            default:
                resourceName = "INVALID";
            break;
        }

        return resourceName;
    }

    public boolean isInvalid() {
        return this.value.equals(DepositAccountType.INVALID.value);
    }
}