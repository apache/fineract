package org.mifosplatform.portfolio.savingsaccount.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.savingsaccount.domain.DepositScheduleDateGenerator;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;

public class DefaultDepositScheduleDateGenerator implements DepositScheduleDateGenerator {

    @Override
    public List<LocalDate> generate(LocalDate scheuleStartDate, Integer paymentPeriods, Integer depositFrequency,
    		SavingFrequencyType savingFrequencyType) {

        List<LocalDate> duePaymentPeriodDates = new ArrayList<LocalDate>(depositFrequency);
        LocalDate startDate = scheuleStartDate;

        for (int period = 1; period <= paymentPeriods; period++) {

            LocalDate duePaymentPeriodDate = startDate;
            if (period == 1) {
                duePaymentPeriodDate = startDate;
            } else {
                switch (savingFrequencyType) {
                    case DAILY:
                        duePaymentPeriodDate = startDate.plusDays(depositFrequency);
                    break;
                    case MONTHLY:
                        duePaymentPeriodDate = startDate.plusMonths(depositFrequency);
                    break;
                    case QUATERLY:
                        duePaymentPeriodDate = startDate.plusMonths(3*depositFrequency);
                    break;
                    case HALFYEARLY:
                        duePaymentPeriodDate = startDate.plusMonths(6*depositFrequency);
                    break;
                    case YEARLY:
                    	duePaymentPeriodDate = startDate.plusYears(depositFrequency);
                    break;
                    default:
					break;	
                }
            }
            duePaymentPeriodDates.add(duePaymentPeriodDate);
            startDate = duePaymentPeriodDate;
        }

        return duePaymentPeriodDates;
    }

}
