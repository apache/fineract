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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeesBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.util.BuyDownFeeAmortizationUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoanBuyDownFeeAmortizationProcessingServiceImpl implements LoanBuyDownFeeAmortizationProcessingService {

    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanBuyDownFeesBalanceRepository loanBuyDownFeesBalanceRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final ExternalIdFactory externalIdFactory;

    @Override
    @Transactional
    public void processBuyDownFeeAmortizationTillDate(@NonNull Loan loan, @NonNull LocalDate tillDate, boolean addJournal) {
        final List<Long> existingTransactionIds = loanTransactionRepository.findTransactionIdsByLoan(loan);
        final List<Long> existingReversedTransactionIds = loanTransactionRepository.findReversedTransactionIdsByLoan(loan);

        List<LoanBuyDownFeeBalance> balances = loanBuyDownFeesBalanceRepository.findAllByLoanId(loan.getId());

        LocalDate maturityDate = loan.getMaturityDate() != null ? loan.getMaturityDate()
                : getFinalBuyDownFeeAmortizationTransactionDate(loan);
        LocalDate tillDatePlusOne = tillDate.plusDays(1);
        if (tillDatePlusOne.isAfter(maturityDate)) {
            tillDatePlusOne = maturityDate;
        }

        Money totalAmortization = Money.zero(loan.getCurrency());
        for (LoanBuyDownFeeBalance balance : balances) {
            List<LoanTransaction> adjustments = loanTransactionRepository.findAdjustments(balance.getLoanTransaction());
            Money amortizationTillDate = BuyDownFeeAmortizationUtil.calculateTotalAmortizationTillDate(balance, adjustments, maturityDate,
                    loan.getLoanProductRelatedDetail().getBuyDownFeeStrategy(), tillDatePlusOne, loan.getCurrency());
            totalAmortization = totalAmortization.add(amortizationTillDate);

            balance.setUnrecognizedAmount(balance.getAmount().subtract(MathUtil.nullToZero(balance.getAmountAdjustment()))
                    .subtract(amortizationTillDate.getAmount()));
        }

        loanBuyDownFeesBalanceRepository.saveAll(balances);

        BigDecimal totalAmortized = loanTransactionRepository.getAmortizedAmountBuyDownFee(loan);
        BigDecimal totalAmortizationAmount = totalAmortization.getAmount().subtract(totalAmortized);

        if (!MathUtil.isZero(totalAmortizationAmount)) {
            LoanTransaction transaction = MathUtil.isGreaterThanZero(totalAmortizationAmount)
                    ? LoanTransaction.buyDownFeeAmortization(loan, loan.getOffice(), tillDate, totalAmortizationAmount,
                            externalIdFactory.create())
                    : LoanTransaction.buyDownFeeAmortizationAdjustment(loan,
                            Money.of(loan.getCurrency(), MathUtil.negate(totalAmortizationAmount)), tillDate, externalIdFactory.create());
            loan.addLoanTransaction(transaction);

            transaction = loanTransactionRepository.save(transaction);
            loanTransactionRepository.flush();

            if (addJournal) {
                journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            }

            BusinessEvent<?> event = MathUtil.isGreaterThanZero(totalAmortizationAmount)
                    ? new LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent(transaction)
                    : new LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent(transaction);
            businessEventNotifierService.notifyPostBusinessEvent(event);
        }
    }

    private LocalDate getFinalBuyDownFeeAmortizationTransactionDate(final Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            case CLOSED_WRITTEN_OFF -> loan.getWrittenOffOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }
}
