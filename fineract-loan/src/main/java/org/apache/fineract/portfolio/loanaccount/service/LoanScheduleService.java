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
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanMapper;
import org.apache.fineract.portfolio.loanaccount.service.schedule.LoanScheduleComponent;

@RequiredArgsConstructor
public class LoanScheduleService {

    private final LoanChargeService loanChargeService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanMapper loanMapper;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanScheduleComponent loanSchedule;
    private final LoanTransactionRepository loanTransactionRepository;

    /**
     * Ability to regenerate the repayment schedule based on the loans current details/state.
     */
    public void regenerateRepaymentSchedule(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final LoanScheduleModel loanScheduleModel = loanMapper.regenerateScheduleModel(scheduleGeneratorDTO, loan);
        if (loanScheduleModel == null) {
            return;
        }
        loanSchedule.updateLoanSchedule(loan, loanScheduleModel);
        final Set<LoanCharge> charges = loan.getActiveCharges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isWaived()) {
                loanChargeService.recalculateLoanCharge(loan, loanCharge, scheduleGeneratorDTO.getPenaltyWaitPeriod());
            }
        }
    }

    public void recalculateSchedule(final Loan loan, final ScheduleGeneratorDTO generatorDTO) {
        if (loan.isInterestBearingAndInterestRecalculationEnabled() && !loan.isChargedOff()) {
            regenerateRepaymentScheduleWithInterestRecalculation(loan, generatorDTO);
        } else {
            regenerateRepaymentSchedule(loan, generatorDTO);
        }
        reprocessLoanTransactionsService.reprocessTransactions(loan);
    }

    public void recalculateScheduleFromLastTransaction(final Loan loan, final ScheduleGeneratorDTO generatorDTO,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {
        recalculateScheduleFromLastTransaction(loan, generatorDTO, existingTransactionIds, existingReversedTransactionIds, false);
    }

    public void recalculateScheduleFromLastTransaction(final Loan loan, final ScheduleGeneratorDTO generatorDTO,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds, boolean skipTransactionIdCollecting) {
        if (!skipTransactionIdCollecting) {
            existingTransactionIds.addAll(loanTransactionRepository.findTransactionIdsByLoan(loan));
            existingReversedTransactionIds.addAll(loanTransactionRepository.findReversedTransactionIdsByLoan(loan));
        }
        if (!loan.isProgressiveSchedule()) {
            if (loan.isInterestBearingAndInterestRecalculationEnabled() && !loan.isChargedOff()) {
                regenerateRepaymentScheduleWithInterestRecalculation(loan, generatorDTO);
            } else {
                regenerateRepaymentSchedule(loan, generatorDTO);
            }
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        } else {
            reprocessLoanTransactionsService.updateModel(loan);
        }

    }

    public void regenerateRepaymentScheduleWithInterestRecalculation(final Loan loan, final ScheduleGeneratorDTO generatorDTO) {
        final LocalDate lastTransactionDate = loan.getLastUserTransactionDate();
        final LoanScheduleDTO loanScheduleDTO = loanTransactionProcessingService.getRecalculatedSchedule(generatorDTO, loan);
        if (loanScheduleDTO == null) {
            return;
        }
        // Either the installments got recalculated or the model
        if (loanScheduleDTO.getInstallments() != null) {
            loanSchedule.updateLoanSchedule(loan, loanScheduleDTO.getInstallments());
        } else {
            loanSchedule.updateLoanSchedule(loan, loanScheduleDTO.getLoanScheduleModel());
        }
        loan.setInterestRecalculatedOn(DateUtils.getBusinessLocalDate());
        final LocalDate lastRepaymentDate = loan.getLastRepaymentPeriodDueDate(true);
        final Set<LoanCharge> charges = loan.getActiveCharges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {
                loanChargeService.updateOverdueScheduleInstallment(loan, loanCharge);
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
        loanTransactionProcessingService.processPostDisbursementTransactions(loan);
    }

    public void handleRegenerateRepaymentScheduleWithInterestRecalculation(final Loan loan, final ScheduleGeneratorDTO generatorDTO) {
        regenerateRepaymentScheduleWithInterestRecalculation(loan, generatorDTO);
        reprocessLoanTransactionsService.reprocessTransactions(loan);
    }
}
