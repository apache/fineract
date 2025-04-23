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
package org.apache.fineract.portfolio.loanaccount.service.schedule;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanScheduleComponent {

    private final LoanBalanceService loanBalanceService;

    public void updateLoanSchedule(Loan loan, final LoanScheduleModel modifiedLoanSchedule) {
        final List<LoanScheduleModelPeriod> periods = modifiedLoanSchedule.getPeriods();
        for (final LoanScheduleModelPeriod scheduledLoanInstallment : modifiedLoanSchedule.getPeriods()) {
            if (scheduledLoanInstallment.isRepaymentPeriod() || scheduledLoanInstallment.isDownPaymentPeriod()) {
                LoanRepaymentScheduleInstallment existingInstallment = findByInstallmentNumber(loan.getRepaymentScheduleInstallments(),
                        scheduledLoanInstallment.periodNumber());
                if (existingInstallment == null) {
                    final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan,
                            scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                            scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                            scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                            scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent(),
                            scheduledLoanInstallment.getLoanCompoundingDetails(), scheduledLoanInstallment.rescheduleInterestPortion(),
                            scheduledLoanInstallment.isDownPaymentPeriod());
                    loan.addLoanRepaymentScheduleInstallment(installment);
                } else {
                    existingInstallment.copyFrom(scheduledLoanInstallment);
                }
            }
        }
        // Review Installments removed
        loan.getRepaymentScheduleInstallments().removeIf(i -> !existInstallment(periods, i.getInstallmentNumber()));

        loan.updateLoanScheduleDependentDerivedFields();
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
    }

    public void updateLoanSchedule(Loan loan, final List<LoanRepaymentScheduleInstallment> installments) {
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            LoanRepaymentScheduleInstallment existingInstallment = findByInstallmentNumber(loan.getRepaymentScheduleInstallments(),
                    installment.getInstallmentNumber());
            if (existingInstallment != null) {
                existingInstallment.copyFrom(installment);
            } else {
                loan.addLoanRepaymentScheduleInstallment(installment);
            }
        }
        // Review Installments removed
        loan.getRepaymentScheduleInstallments().removeIf(i -> !existInstallment(installments, i.getInstallmentNumber()));

        loan.updateLoanScheduleDependentDerivedFields();
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
    }

    private LoanRepaymentScheduleInstallment findByInstallmentNumber(final Collection<LoanRepaymentScheduleInstallment> installments,
            final Integer installmentNumber) {
        return installments.stream().filter(i -> installmentNumber.compareTo(i.getInstallmentNumber()) == 0).findFirst().orElse(null);
    }

    private boolean existInstallment(final Collection<LoanRepaymentScheduleInstallment> installments, final Integer installmentNumber) {
        return installments.stream().anyMatch(i -> installmentNumber.compareTo(i.getInstallmentNumber()) == 0);
    }

    private boolean existInstallment(final List<LoanScheduleModelPeriod> periods, final Integer installmentNumber) {
        return periods.stream().anyMatch(p -> p.periodNumber() != null && installmentNumber.compareTo(p.periodNumber()) == 0);
    }

}
