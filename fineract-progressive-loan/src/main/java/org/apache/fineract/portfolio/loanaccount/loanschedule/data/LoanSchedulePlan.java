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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelDisbursementPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;

@Data
public class LoanSchedulePlan {

    private final List<LoanSchedulePlanPeriod> periods;
    private final CurrencyData currency;
    private final int loanTermInDays;
    private final BigDecimal totalDisbursedAmount;
    private final BigDecimal totalPrincipalAmount;
    private final BigDecimal totalInterestAmount;
    private final BigDecimal totalFeeAmount;
    private final BigDecimal totalPenaltyAmount;
    private final BigDecimal totalRepaymentAmount;
    private final BigDecimal totalOutstandingAmount;

    public static LoanSchedulePlan from(LoanScheduleModel model) {
        List<LoanSchedulePlanPeriod> periods = new ArrayList<>();

        model.getPeriods().forEach(periodModel -> {
            LoanSchedulePlanPeriod periodPlan = null;
            if (periodModel instanceof LoanScheduleModelDisbursementPeriod disbursementPeriod) {
                periodPlan = new LoanSchedulePlanDisbursementPeriod(disbursementPeriod.getDisbursementDate(), //
                        disbursementPeriod.getDisbursementDate(), //
                        disbursementPeriod.getPrincipalDisbursed().getAmount(), //
                        disbursementPeriod.getPrincipalDisbursed().getAmount());//
            } else if (periodModel instanceof LoanScheduleModelDownPaymentPeriod downPaymentPeriod) {
                periodPlan = new LoanSchedulePlanDownPaymentPeriod(downPaymentPeriod.getPeriodNumber(), //
                        downPaymentPeriod.getPeriodDate(), //
                        downPaymentPeriod.getPeriodDate(), //
                        downPaymentPeriod.getPrincipalDue().getAmount(), //
                        downPaymentPeriod.getPrincipalDue().getAmount(), //
                        downPaymentPeriod.getOutstandingLoanBalance().getAmount());//
            } else if (periodModel instanceof LoanScheduleModelRepaymentPeriod repaymentPeriod) {
                periodPlan = new LoanSchedulePlanRepaymentPeriod(repaymentPeriod.getPeriodNumber(), //
                        repaymentPeriod.getFromDate(), //
                        repaymentPeriod.getDueDate(), //
                        repaymentPeriod.getPrincipalDue().getAmount(), //
                        repaymentPeriod.getInterestDue().getAmount(), //
                        repaymentPeriod.getFeeChargesDue().getAmount(), //
                        repaymentPeriod.getPenaltyChargesDue().getAmount(), //
                        repaymentPeriod.getTotalDue().getAmount(), //
                        repaymentPeriod.getOutstandingLoanBalance().getAmount());//
            }
            if (periodPlan != null) {
                periods.add(periodPlan);
            }
        });

        return new LoanSchedulePlan(periods, //
                model.getCurrency(), //
                model.getLoanTermInDays(), //
                model.getTotalPrincipalDisbursed().getAmount(), //
                model.getTotalPrincipalExpected(), //
                model.getTotalInterestCharged(), //
                model.getTotalFeeChargesCharged(), //
                model.getTotalPenaltyChargesCharged(), //
                model.getTotalRepaymentExpected(), //
                model.getTotalOutstanding()//

        );
    }
}
