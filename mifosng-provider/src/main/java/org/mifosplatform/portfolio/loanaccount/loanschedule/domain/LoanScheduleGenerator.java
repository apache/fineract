/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.MathContext;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;

public interface LoanScheduleGenerator {

    LoanScheduleModel generate(MathContext mc, LoanApplicationTerms loanApplicationTerms, Set<LoanCharge> loanCharges,
            final HolidayDetailDTO holidayDetailDTO);

    LoanScheduleModel rescheduleNextInstallments(MathContext mc, LoanApplicationTerms loanApplicationTerms, Set<LoanCharge> loanCharges,
            final HolidayDetailDTO holidayDetailDTO, List<LoanTransaction> transactions,
            LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            LocalDate lastTransactionDate);

    LoanRepaymentScheduleInstallment calculatePrepaymentAmount(List<LoanRepaymentScheduleInstallment> installments,
            MonetaryCurrency currency, LocalDate onDate, LoanApplicationTerms loanApplicationTerms, MathContext mc,
            Set<LoanCharge> charges, HolidayDetailDTO holidayDetailDTO);

    LoanRescheduleModel reschedule(final MathContext mathContext, final LoanRescheduleRequest loanRescheduleRequest,
            final ApplicationCurrency applicationCurrency, final HolidayDetailDTO holidayDetailDTO, CalendarInstance restCalendarInstance);
}