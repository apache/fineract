/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;

public interface AmortizationLoanScheduleGenerator {

    LoanScheduleData generate(ApplicationCurrency applicationCurrency, LoanProductRelatedDetail loanScheduleInfo,
            LocalDate disbursementDate, LocalDate interestCalculatedFrom, BigDecimal periodInterestRateForRepaymentPeriod,
            LocalDate idealDisbursementDateBasedOnFirstRepaymentDate, List<LocalDate> scheduledDates, Set<LoanCharge> loanCharges);
}