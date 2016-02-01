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

import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

/**
 * An enumeration of on hold transactions that can occur on a
 * {@link SavingsAccount}.
 */
public enum DepositAccountOnHoldTransactionType {

    INVALID(0, "deposutAccountOnHoldTransactionType.invalid"), //
    HOLD(1, "deposutAccountOnHoldTransactionType.hold"), //
    RELEASE(2, "deposutAccountOnHoldTransactionType.release");

    private final Integer value;
    private final String code;

    private DepositAccountOnHoldTransactionType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static DepositAccountOnHoldTransactionType fromInt(final Integer transactionType) {

        if (transactionType == null) { return DepositAccountOnHoldTransactionType.INVALID; }

        DepositAccountOnHoldTransactionType savingsAccountTransactionType = DepositAccountOnHoldTransactionType.INVALID;
        switch (transactionType) {
            case 1:
                savingsAccountTransactionType = DepositAccountOnHoldTransactionType.HOLD;
            break;
            case 2:
                savingsAccountTransactionType = DepositAccountOnHoldTransactionType.RELEASE;
            break;

        }
        return savingsAccountTransactionType;
    }

    public boolean isHold() {
        return this.value.equals(DepositAccountOnHoldTransactionType.HOLD.getValue());
    }

    public boolean isRelease() {
        return this.value.equals(DepositAccountOnHoldTransactionType.RELEASE.getValue());
    }

}