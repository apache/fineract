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
package org.apache.fineract.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;

@RequiredArgsConstructor
public class LoanScheduleService {

    private final LoanChargeService loanChargeService;

    /**
     * Ability to regenerate the repayment schedule based on the loans current details/state.
     */
    public void regenerateRepaymentSchedule(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final LoanScheduleModel loanSchedule = loan.regenerateScheduleModel(scheduleGeneratorDTO);
        if (loanSchedule == null) {
            return;
        }
        loan.updateLoanSchedule(loanSchedule);
        final Set<LoanCharge> charges = loan.getActiveCharges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isWaived()) {
                loanChargeService.recalculateLoanCharge(loan, loanCharge, scheduleGeneratorDTO.getPenaltyWaitPeriod());
            }
        }
    }

    public void recalculateScheduleFromLastTransaction(final Loan loan, final ScheduleGeneratorDTO generatorDTO) {
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(loan, generatorDTO);
        } else {
            regenerateRepaymentSchedule(loan, generatorDTO);
        }
        loan.reprocessTransactions();
    }

    public ChangedTransactionDetail recalculateScheduleFromLastTransaction(final Loan loan, final ScheduleGeneratorDTO generatorDTO,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
        /*
         * LocalDate recalculateFrom = null; List<LoanTransaction> loanTransactions =
         * this.retrieveListOfTransactionsPostDisbursementExcludeAccruals(); for (LoanTransaction loanTransaction :
         * loanTransactions) { if (recalculateFrom == null ||
         * loanTransaction.getTransactionDate().isAfter(recalculateFrom)) { recalculateFrom =
         * loanTransaction.getTransactionDate(); } } generatorDTO.setRecalculateFrom(recalculateFrom);
         */
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(loan, generatorDTO);
        } else {
            regenerateRepaymentSchedule(loan, generatorDTO);
        }
        return loan.reprocessTransactions();
    }

    public void regenerateRepaymentScheduleWithInterestRecalculation(final Loan loan, final ScheduleGeneratorDTO generatorDTO) {
        final LocalDate lastTransactionDate = loan.getLastUserTransactionDate();
        final LoanScheduleDTO loanSchedule = loan.getRecalculatedSchedule(generatorDTO);
        if (loanSchedule == null) {
            return;
        }
        // Either the installments got recalculated or the model
        if (loanSchedule.getInstallments() != null) {
            loan.updateLoanSchedule(loanSchedule.getInstallments());
        } else {
            loan.updateLoanSchedule(loanSchedule.getLoanScheduleModel());
        }
        loan.setInterestRecalculatedOn(DateUtils.getBusinessLocalDate());
        final LocalDate lastRepaymentDate = loan.getLastRepaymentPeriodDueDate(true);
        final Set<LoanCharge> charges = loan.getActiveCharges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {
                loan.updateOverdueScheduleInstallment(loanCharge);
                if (loanCharge.getDueLocalDate() == null || (!DateUtils.isBefore(lastRepaymentDate, loanCharge.getDueLocalDate())
                        || loan.getLoanProductRelatedDetail().getLoanScheduleType().equals(LoanScheduleType.PROGRESSIVE))) {
                    if ((loanCharge.isInstalmentFee() || !loanCharge.isWaived()) && (loanCharge.getDueLocalDate() == null
                            || !DateUtils.isAfter(lastTransactionDate, loanCharge.getDueLocalDate()))) {
                        loanChargeService.recalculateLoanCharge(loan, loanCharge, generatorDTO.getPenaltyWaitPeriod());
                        loanCharge.updateWaivedAmount(loan.getCurrency());
                    }
                } else {
                    loanCharge.setActive(false);
                }
            }
        }

        loan.processPostDisbursementTransactions();
    }

    public ChangedTransactionDetail handleRegenerateRepaymentScheduleWithInterestRecalculation(final Loan loan,
            final ScheduleGeneratorDTO generatorDTO) {
        regenerateRepaymentScheduleWithInterestRecalculation(loan, generatorDTO);
        return loan.reprocessTransactions();
    }
}
