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

import java.util.Arrays;

/**
 * An enumeration of different options available on account closure {@link FixedDepositAccount} &amp;
 * {@link RecurringDepositAccount}.
 */
public enum DepositAccountOnClosureType {

    INVALID(0, "depositAccountClosureType.invalid"), //
    WITHDRAW_DEPOSIT(100, "depositAccountClosureType.withdrawDeposit"), //
    TRANSFER_TO_SAVINGS(200, "depositAccountClosureType.transferToSavings"), //
    REINVEST_PRINCIPAL_AND_INTEREST(300, "depositAccountClosureType.reinvestPrincipalAndInterest"), REINVEST_PRINCIPAL_ONLY(400,
            "depositAccountClosureType.reinvestPrincipalOnly"); //

    private final Integer value;
    private final String code;

    DepositAccountOnClosureType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static DepositAccountOnClosureType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 100:
                return WITHDRAW_DEPOSIT;
            case 200:
                return TRANSFER_TO_SAVINGS;
            case 300:
                return REINVEST_PRINCIPAL_AND_INTEREST;
            case 400:
                return REINVEST_PRINCIPAL_ONLY;
            default:
                return INVALID;
        }
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isWithdarwDeposit() {
        return this.equals(WITHDRAW_DEPOSIT);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isTransferToSavings() {
        return this.equals(TRANSFER_TO_SAVINGS);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isReinvest() {
        return this.equals(REINVEST_PRINCIPAL_AND_INTEREST) || this.equals(REINVEST_PRINCIPAL_ONLY);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isReinvestPrincipal() {
        return this.equals(REINVEST_PRINCIPAL_ONLY);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isReinvestPrincipalAndInterest() {
        return this.equals(REINVEST_PRINCIPAL_AND_INTEREST);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isInvalid() {
        return this.equals(INVALID);
    }

    // TODO: do we really need this?!?
    public static Object[] integerValues() {
        return Arrays.stream(values()).filter(value -> !INVALID.equals(value)).map(value -> value.value).toList().toArray();
    }
}
