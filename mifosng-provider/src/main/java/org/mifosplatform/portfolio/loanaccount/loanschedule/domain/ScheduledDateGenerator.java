/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

public interface ScheduledDateGenerator {

    LocalDate getLastRepaymentDate(LoanApplicationTerms loanApplicationTerms, boolean isHolidayEnabled, List<Holiday> holidays,
            final WorkingDays workingDays);

    LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(PeriodFrequencyType repaymentPeriodFrequencyType, int repaidEvery,
            final LocalDate firstRepaymentDate);

    LocalDate generateNextRepaymentDate(LocalDate lastRepaymentDate, LoanApplicationTerms loanApplicationTerms, boolean isFirstRepayment);

    LocalDate adjustRepaymentDate(LocalDate dueRepaymentPeriodDate, LoanApplicationTerms loanApplicationTerms, boolean isHolidayEnabled,
            List<Holiday> holidays, WorkingDays workingDays);

    LocalDate getRepaymentPeriodDate(PeriodFrequencyType frequency, int repaidEvery, LocalDate startDate);

    Boolean isDateFallsInSchedule(PeriodFrequencyType frequency, int repaidEvery, LocalDate startDate, LocalDate date);

}
