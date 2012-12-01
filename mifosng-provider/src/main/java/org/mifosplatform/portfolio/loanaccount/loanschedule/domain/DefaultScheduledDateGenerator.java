package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;

public class DefaultScheduledDateGenerator implements ScheduledDateGenerator {

    @Override
    public List<LocalDate> generate(final LoanProductMinimumRepaymentScheduleRelatedDetail loanScheduleDateInfo, final LocalDate disbursementDate, final LocalDate firstRepaymentPeriodDate) {

        List<LocalDate> dueRepaymentPeriodDates = new ArrayList<LocalDate>(loanScheduleDateInfo.getNumberOfRepayments());

        LocalDate startDate = disbursementDate;

        for (int repaymentPeriod = 1; repaymentPeriod <= loanScheduleDateInfo.getNumberOfRepayments(); repaymentPeriod++) {

            LocalDate dueRepaymentPeriodDate = startDate;
            
			if (repaymentPeriod == 1 && firstRepaymentPeriodDate != null) {
				dueRepaymentPeriodDate = firstRepaymentPeriodDate;
			} else {
				switch (loanScheduleDateInfo.getRepaymentPeriodFrequencyType()) {
				case DAYS:
					dueRepaymentPeriodDate = startDate.plusDays(loanScheduleDateInfo
							.getRepayEvery());
					break;
				case WEEKS:
					dueRepaymentPeriodDate = startDate.plusWeeks(loanScheduleDateInfo
							.getRepayEvery());
					break;
				case MONTHS:
					dueRepaymentPeriodDate = startDate.plusMonths(loanScheduleDateInfo
							.getRepayEvery());
					break;
				case YEARS:
					dueRepaymentPeriodDate = startDate.plusYears(loanScheduleDateInfo
							.getRepayEvery());
					break;
				case INVALID:
					break;
				}
			}

            dueRepaymentPeriodDates.add(dueRepaymentPeriodDate);
            startDate = dueRepaymentPeriodDate;
        }

        return dueRepaymentPeriodDates;
    }

	@Override
	public LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(
			LoanProductRelatedDetail loanScheduleDateInfo,
			List<LocalDate> scheduledDates) {
		
		LocalDate firstRepaymentDate = scheduledDates.get(0);
		
		LocalDate idealDisbursementDate = null;
		
		switch (loanScheduleDateInfo.getRepaymentPeriodFrequencyType()) {
		case DAYS:
			idealDisbursementDate = firstRepaymentDate.minusDays(loanScheduleDateInfo.getRepayEvery());
			break;
		case WEEKS:
			idealDisbursementDate = firstRepaymentDate.minusWeeks(loanScheduleDateInfo
					.getRepayEvery());
			break;
		case MONTHS:
			idealDisbursementDate = firstRepaymentDate.minusMonths(loanScheduleDateInfo
					.getRepayEvery());
			break;
		case YEARS:
			idealDisbursementDate = firstRepaymentDate.minusYears(loanScheduleDateInfo
					.getRepayEvery());
			break;
		case INVALID:
			break;
		}
		
		return idealDisbursementDate;
	}
}