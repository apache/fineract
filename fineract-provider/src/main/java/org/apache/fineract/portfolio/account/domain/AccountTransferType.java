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
public enum AccountTransferType {

    INVALID(0, "accountTransferType.invalid"), //
    ACCOUNT_TRANSFER(1, "accountTransferType.account.transfer"), //
    LOAN_REPAYMENT(2, "accountTransferType.loan.repayment"), //
    CHARGE_PAYMENT(3, "accountTransferType.charge.payment"), //
    INTEREST_TRANSFER(4, "accountTransferType.interest.transfer"); //

    private final Integer value;
    private final String code;

    public static AccountTransferType fromInt(final Integer statusValue) {

        AccountTransferType enumeration = AccountTransferType.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = AccountTransferType.ACCOUNT_TRANSFER;
            break;
            case 2:
                enumeration = AccountTransferType.LOAN_REPAYMENT;
            break;
            case 3:
                enumeration = AccountTransferType.CHARGE_PAYMENT;
            break;
            case 4:
                enumeration = AccountTransferType.INTEREST_TRANSFER;
            break;
        }
        return enumeration;
    }

    private AccountTransferType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final AccountTransferType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isAccountTransfer() {
        return this.value.equals(AccountTransferType.ACCOUNT_TRANSFER.getValue());
    }

    public boolean isLoanRepayment() {
        return this.value.equals(AccountTransferType.LOAN_REPAYMENT.getValue());
    }

    public boolean isChargePayment() {
        return this.value.equals(AccountTransferType.CHARGE_PAYMENT.getValue());
    }

    public boolean isInterestTransfer() {
        return this.value.equals(AccountTransferType.INTEREST_TRANSFER.getValue());
    }
}