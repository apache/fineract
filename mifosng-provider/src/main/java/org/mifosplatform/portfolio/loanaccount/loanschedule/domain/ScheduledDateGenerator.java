/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;

public interface ScheduledDateGenerator {

	List<LocalDate> generate(
			LoanProductMinimumRepaymentScheduleRelatedDetail loanScheduleDateInfo,
			LocalDate disbursementDate, LocalDate firstRepaymentDate);

	LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(
			LoanProductRelatedDetail loanScheduleInfo,
			List<LocalDate> scheduledDates);

}
