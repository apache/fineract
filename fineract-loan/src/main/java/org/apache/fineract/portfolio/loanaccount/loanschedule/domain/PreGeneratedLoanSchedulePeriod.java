/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

@Data
@Accessors(fluent = true)
public class PreGeneratedLoanSchedulePeriod implements LoanScheduleModelPeriod {

    private Integer periodNumber;
    private boolean isRepaymentPeriod;
    private boolean isDownPaymentPeriod;
    private LocalDate periodFromDate;
    private LocalDate periodDueDate;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal feeChargesDue;
    private BigDecimal penaltyChargesDue;
    private BigDecimal rescheduleInterestPortion;
    private boolean isRecalculatedInterestComponent;
    private boolean isEMIFixedSpecificToInstallment;

    public PreGeneratedLoanSchedulePeriod(Integer periodNumber, LocalDate periodFromDate, LocalDate periodDueDate) {
        this.periodNumber = periodNumber;
        this.isRepaymentPeriod = true;
        this.isDownPaymentPeriod = false;
        this.periodFromDate = periodFromDate;
        this.periodDueDate = periodDueDate;
        this.principalDue = BigDecimal.ZERO;
        this.interestDue = BigDecimal.ZERO;
        this.feeChargesDue = BigDecimal.ZERO;
        this.penaltyChargesDue = BigDecimal.ZERO;
        this.rescheduleInterestPortion = BigDecimal.ZERO;
        this.isRecalculatedInterestComponent = false;
        this.isEMIFixedSpecificToInstallment = false;
    }

    @Override
    public LoanSchedulePeriodData toData() {
        return null;
    }

    @Override
    public void addLoanCharges(BigDecimal feeCharge, BigDecimal penaltyCharge) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPrincipalAmount(Money principalDue) {
        this.principalDue = this.principalDue.add(principalDue.getAmount());
    }

    @Override
    public void addInterestAmount(Money interestDue) {
        this.interestDue = this.principalDue.add(interestDue.getAmount());
    }

    @Override
    public Set<LoanInterestRecalcualtionAdditionalDetails> getLoanCompoundingDetails() {
        return Set.of();
    }

    @Override
    public void setEMIFixedSpecificToInstallmentTrue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRescheduleInterestPortion(BigDecimal rescheduleInterestPortion) {
        throw new UnsupportedOperationException();
    }
}
