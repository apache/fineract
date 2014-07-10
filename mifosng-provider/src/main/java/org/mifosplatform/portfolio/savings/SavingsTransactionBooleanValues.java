package org.mifosplatform.portfolio.savings;

public class SavingsTransactionBooleanValues {

    private final boolean isAccountTransfer;
    private final boolean isRegularTransaction;
    private final boolean isApplyWithdrawFee;
    private final boolean isInterestTransfer;
    private final boolean isWithdrawBalance;

    public SavingsTransactionBooleanValues(final boolean isAccountTransfer, final boolean isRegularTransaction,
            final boolean isApplyWithdrawFee, final boolean isInterestTransfer, final boolean isWithdrawBalance) {

        this.isAccountTransfer = isAccountTransfer;
        this.isRegularTransaction = isRegularTransaction;
        this.isApplyWithdrawFee = isApplyWithdrawFee;
        this.isInterestTransfer = isInterestTransfer;
        this.isWithdrawBalance = isWithdrawBalance;
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

    public boolean isWithdrawBalance() {
        return this.isWithdrawBalance;
    }

}
