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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkingCapitalLoanDiscountFeeAmortizationServiceImpl implements WorkingCapitalLoanDiscountFeeAmortizationService {

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final ExternalIdFactory externalIdFactory;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;

    @Override
    @Transactional
    public Optional<WorkingCapitalLoanTransaction> processDiscountFeeAmortization(final WorkingCapitalLoan loan,
            final LocalDate transactionDate) {
        final BigDecimal scheduleAmortization = calculateScheduleAmortization(loan);
        final BigDecimal alreadyRecognized = calculateAlreadyRecognizedAmount(loan);
        final BigDecimal delta = scheduleAmortization.subtract(alreadyRecognized);

        if (MathUtil.isZero(delta)) {
            log.debug("Skipping discount fee amortization for WC loan [{}] - no new amount to amortize (schedule={}, posted={})",
                    loan.getId(), scheduleAmortization, alreadyRecognized);
            syncIncomeBalances(loan, scheduleAmortization);
            return Optional.empty();
        }

        final boolean isChargedOff = false;
        final WorkingCapitalLoanTransaction postedTxn;
        if (MathUtil.isGreaterThanZero(delta)) {
            postedTxn = WorkingCapitalLoanTransaction.discountFeeAmortization(loan, delta, transactionDate, externalIdFactory.create());
            transactionRepository.saveAndFlush(postedTxn);
            loan.getTransactions().add(postedTxn);
            accountingProcessor.postJournalEntriesForDiscountFeeAmortization(loan, postedTxn, isChargedOff);
        } else {
            final BigDecimal adjustmentAmount = delta.negate();
            postedTxn = WorkingCapitalLoanTransaction.discountFeeAmortizationAdjustment(loan, adjustmentAmount, transactionDate,
                    externalIdFactory.create());
            transactionRepository.saveAndFlush(postedTxn);
            loan.getTransactions().add(postedTxn);
            accountingProcessor.postJournalEntriesForDiscountFeeAmortizationAdjustment(loan, postedTxn, isChargedOff);
        }

        log.debug("Posted discount fee amortization of {} for WC loan [{}]", delta, loan.getId());
        syncIncomeBalances(loan, scheduleAmortization);
        return Optional.of(postedTxn);
    }

    private BigDecimal calculateScheduleAmortization(final WorkingCapitalLoan loan) {
        final MathContext mc = MoneyHelper.getMathContext();
        return scheduleRepositoryWrapper.readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .map(ProjectedAmortizationScheduleModel::totalActualAmortization).orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateAlreadyRecognizedAmount(final WorkingCapitalLoan loan) {
        BigDecimal amortization = BigDecimal.ZERO;
        BigDecimal adjustment = BigDecimal.ZERO;
        for (final WorkingCapitalLoanTransaction txn : loan.getTransactions()) {
            if (txn.isReversed()) {
                continue;
            }
            if (txn.getTypeOf() == LoanTransactionType.DISCOUNT_FEE_AMORTIZATION) {
                amortization = amortization.add(txn.getTransactionAmount());
            } else if (txn.getTypeOf() == LoanTransactionType.DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT) {
                adjustment = adjustment.add(txn.getTransactionAmount());
            }
        }
        return amortization.subtract(adjustment);
    }

    private void syncIncomeBalances(final WorkingCapitalLoan loan, final BigDecimal scheduleAmortization) {
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDiscount() != null
                ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;
        final BigDecimal realizedIncome = scheduleAmortization.max(BigDecimal.ZERO).min(discount);
        final BigDecimal unrealizedIncome = discount.subtract(realizedIncome).max(BigDecimal.ZERO);

        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        balance.setRealizedIncome(realizedIncome);
        balance.setUnrealizedIncome(unrealizedIncome);
        balanceRepository.saveAndFlush(balance);
    }
}
