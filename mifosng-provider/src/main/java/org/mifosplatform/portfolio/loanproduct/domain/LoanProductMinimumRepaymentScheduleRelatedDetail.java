package org.mifosplatform.portfolio.loanproduct.domain;

import java.io.Serializable;


/**
 * Represents the bare minimum repayment details needed for activities related
 * to generating repayment schedules.
 */
public interface LoanProductMinimumRepaymentScheduleRelatedDetail extends Serializable {

    Integer getRepayEvery();

    PeriodFrequencyType getRepaymentPeriodFrequencyType();

    Integer getNumberOfRepayments();
}
