/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

public class SavingsTransactionBooleanValues {

    private final boolean isAccountTransfer;
    private final boolean isRegularTransaction;
    private final boolean isApplyWithdrawFee;
    private final boolean isInterestTransfer;
    private final boolean isExceptionForBalanceCheck;

    public SavingsTransactionBooleanValues(final boolean isAccountTransfer, final boolean isRegularTransaction,
            final boolean isApplyWithdrawFee, final boolean isInterestTransfer, final boolean isExceptionForBalanceCheck) {

        this.isAccountTransfer = isAccountTransfer;
        this.isRegularTransaction = isRegularTransaction;
        this.isApplyWithdrawFee = isApplyWithdrawFee;
        this.isInterestTransfer = isInterestTransfer;
        this.isExceptionForBalanceCheck = isExceptionForBalanceCheck;
    }

    public boolean isAccountTransfer() {
        return this.isAccountTransfer;
    }

    public boolean isRegularTransaction() {
        return this.isRegularTransaction;
    }

    public boolean isApplyWithdrawFee() {
        return this.isApplyWithdrawFee;
    }

    public boolean isInterestTransfer() {
        return this.isInterestTransfer;
    }

    public boolean isExceptionForBalanceCheck() {
        return this.isExceptionForBalanceCheck;
    }

}
