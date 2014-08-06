package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

public class RecalculatedSchedule {

    private final LoanScheduleModel loanScheduleModel;
    private final Integer installmentNumber;

    public RecalculatedSchedule(final LoanScheduleModel loanScheduleModel, final Integer installmentNumber) {
        this.loanScheduleModel = loanScheduleModel;
        this.installmentNumber = installmentNumber;
    }

    public LoanScheduleModel getLoanScheduleModel() {
        return this.loanScheduleModel;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }
}
