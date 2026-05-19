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
    public void processDiscountFeeAmortization(final WorkingCapitalLoan loan, final LocalDate transactionDate) {
        final BigDecimal scheduleAmortization = calculateScheduleAmortization(loan);
        if (MathUtil.isZero(scheduleAmortization)) {
            log.debug("Skipping discount fee amortization for WC loan [{}] - no amortization on schedule", loan.getId());
            syncIncomeBalances(loan, scheduleAmortization);
            return;
        }

        final BigDecimal alreadyPosted = calculateAlreadyPostedAmount(loan);
        final BigDecimal amortizationAmount = scheduleAmortization.subtract(alreadyPosted);

        if (!MathUtil.isGreaterThanZero(amortizationAmount)) {
            log.debug("Skipping discount fee amortization for WC loan [{}] - no new amount to amortize (schedule={}, posted={})",
                    loan.getId(), scheduleAmortization, alreadyPosted);
            return;
        }

        final WorkingCapitalLoanTransaction amortizationTxn = WorkingCapitalLoanTransaction.discountFeeAmortization(loan,
                amortizationAmount, transactionDate, externalIdFactory.create());
        transactionRepository.saveAndFlush(amortizationTxn);
        loan.getTransactions().add(amortizationTxn);

        syncIncomeBalances(loan, scheduleAmortization);

        accountingProcessor.postJournalEntriesForDiscountFeeAmortization(loan, amortizationTxn, false);

        log.debug("Posted discount fee amortization of {} for WC loan [{}]", amortizationAmount, loan.getId());
    }

    private BigDecimal calculateScheduleAmortization(final WorkingCapitalLoan loan) {
        final MathContext mc = MoneyHelper.getMathContext();
        return scheduleRepositoryWrapper.readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .map(ProjectedAmortizationScheduleModel::totalActualAmortization).orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateAlreadyPostedAmount(final WorkingCapitalLoan loan) {
        return loan.getTransactions().stream()
                .filter(txn -> txn.getTypeOf() == LoanTransactionType.DISCOUNT_FEE_AMORTIZATION && !txn.isReversed())
                .map(WorkingCapitalLoanTransaction::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
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
