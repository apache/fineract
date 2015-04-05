/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

public interface LoanScheduleModelPeriod {

    LoanSchedulePeriodData toData();

    boolean isRepaymentPeriod();

    Integer periodNumber();

    LocalDate periodFromDate();

    LocalDate periodDueDate();

    BigDecimal principalDue();

    BigDecimal interestDue();

    BigDecimal feeChargesDue();

    BigDecimal penaltyChargesDue();

    void addLoanCharges(BigDecimal feeCharge, BigDecimal penaltyCharge);
    
    boolean isRecalculatedInterestComponent();

    void addPrincipalAmount(Money principalDue);
    
    void addInterestAmount(Money interestDue);
}
