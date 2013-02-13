/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

/**
 * Represents the bare minimum repayment details needed for activities related
 * to generating repayment schedules.
 */
public interface LoanProductMinimumRepaymentScheduleRelatedDetail {

    Integer getRepayEvery();

    PeriodFrequencyType getRepaymentPeriodFrequencyType();

    Integer getNumberOfRepayments();
}
