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

import org.apache.commons.lang3.StringUtils;

/**
 * An enumeration of different transactions that can occur on a {@link SavingsAccount}.
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

    DepositAccountType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static DepositAccountType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 100:
                return SAVINGS_DEPOSIT;
            case 200:
                return FIXED_DEPOSIT;
            case 300:
                return RECURRING_DEPOSIT;
            case 400:
                return CURRENT_DEPOSIT;
            default:
                return INVALID;
        }
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isSavingsDeposit() {
        return this.equals(SAVINGS_DEPOSIT);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isFixedDeposit() {
        return this.equals(FIXED_DEPOSIT);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isRecurringDeposit() {
        return this.equals(RECURRING_DEPOSIT);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isCurrentDeposit() {
        return this.equals(CURRENT_DEPOSIT);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isInvalid() {
        return this.equals(INVALID);
    }

    @Override
    public String toString() {
        return StringUtils.replace(code, "_", SPACE);
    }

    public String resourceName() {
        switch (this) {
            case FIXED_DEPOSIT:
                return DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME;
            case RECURRING_DEPOSIT:
                return DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME;
            case SAVINGS_DEPOSIT:
                return DepositsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
            default:
                return "INVALID";
        }
    }
}
