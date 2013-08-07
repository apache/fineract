package org.mifosplatform.infrastructure.jobs.service;

public enum JobName {

    UPDATE_LOAN_SUMMARY("Update loan Summary"), UPDATE_LOAN_ARREARS_AGEING("Update Loan Arrears Ageing"), UPDATE_LOAN_PAID_IN_ADVANCE(
            "Update Loan Paid In Advance"), APPLY_ANNUAL_FEE_FOR_SAVINGS("Apply Annual Fee For Savings"), APPLY_HOLIDAYS_TO_LOANS(
            "Apply Holidays To Loans"), POST_INTEREST_FOR_SAVINGS("Post Interest For Savings");

    private final String name;

    JobName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
