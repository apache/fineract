package org.mifosplatform.infrastructure.jobs.service;

public enum JobName {

    UPDATE_LOAN_SUMMARY("Update loan Summary"), UPDATE_LOAN_ARREARS_AGEING("Update Loan Arrears Ageing"), UPDATE_LOAN_PAID_IN_ADVANCE(
            "Update Loan Paid In Advance"), APPLY_ANNUAL_FEE_FOR_SAVINGS("Apply Annual Fee For Savings"), APPLY_HOLIDAYS_TO_LOANS(
            "Apply Holidays To Loans"), POST_INTEREST_FOR_SAVINGS("Post Interest For Savings"), TRANSFER_FEE_CHARGE_FOR_LOANS(
            "Transfer Fee For Loans From Savings"), ACCOUNTING_RUNNING_BALANCE_UPDATE("Update Accounting Running Balances"), PAY_DUE_SAVINGS_CHARGES(
            "Pay Due Savings Charges");

    private final String name;

    JobName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
