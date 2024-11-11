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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REPAYMENT;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.starter.AdvancedPaymentScheduleTransactionProcessorCondition;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Conditional(AdvancedPaymentScheduleTransactionProcessorCondition.class)
@Service
public class ProgressiveLoanInterestRefundServiceImpl implements InterestRefundService {

    private final EMICalculator emiCalculator;
    private final LoanAssembler loanAssembler;

    private static void simulateRepaymentForDisbursements(LoanTransaction lt, final AtomicReference<BigDecimal> refundFinal,
            List<LoanTransaction> collect) {
        collect.add(new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), lt.getTypeOf().getValue(), lt.getDateOf(), lt.getAmount(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null));
        if (lt.getTypeOf().isDisbursement() && MathUtil.isGreaterThanZero(refundFinal.get())) {
            if (lt.getAmount().compareTo(refundFinal.get()) <= 0) {
                collect.add(
                        new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), REPAYMENT.getValue(), lt.getDateOf(), lt.getAmount(),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null));
                refundFinal.set(refundFinal.get().subtract(lt.getAmount()));
            } else {
                collect.add(
                        new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), REPAYMENT.getValue(), lt.getDateOf(), refundFinal.get(),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null));
                refundFinal.set(BigDecimal.ZERO);
            }
        }
    }

    private Money recalculateTotalInterest(AdvancedPaymentScheduleTransactionProcessor processor, Loan loan,
            LocalDate relatedRefundTransactionDate, List<LoanTransaction> transactionsToReprocess) {
        List<LoanRepaymentScheduleInstallment> installmentsToReprocess = new ArrayList<>(
                loan.getRepaymentScheduleInstallments().stream().filter(i -> !i.isReAged() && !i.isAdditional()).toList());

        Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> reprocessResult = processor
                .reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), relatedRefundTransactionDate, transactionsToReprocess,
                        loan.getCurrency(), installmentsToReprocess, loan.getActiveCharges());
        loan.getLoanTransactions().addAll(reprocessResult.getLeft().getCurrentTransactionToOldId().keySet());
        ProgressiveLoanInterestScheduleModel modelAfter = reprocessResult.getRight();

        return emiCalculator.getSumOfDueInterestsOnDate(modelAfter, relatedRefundTransactionDate);
    }

    @Override
    public boolean canHandle(Loan loan) {
        String s = loan.getTransactionProcessingStrategyCode();
        return AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY_NAME.equalsIgnoreCase(s)
                || AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equalsIgnoreCase(s);
    }

    private boolean isTransactionNeededForInterestRefundCalculations(LoanTransaction lt) {
        return lt.isNotReversed() && !lt.isAccrualRelated() && !lt.isInterestRefund();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Money totalInterestByTransactions(LoanRepaymentScheduleTransactionProcessor processor, final Long loanId,
            LocalDate relatedRefundTransactionDate, List<LoanTransaction> newTransactions, List<Long> oldTransactionIds) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        if (processor == null) {
            processor = loan.getTransactionProcessor();
        }
        if (!(processor instanceof AdvancedPaymentScheduleTransactionProcessor)) {
            throw new IllegalArgumentException(
                    "Wrong processor implementation. ProgressiveLoanInterestRefundServiceImpl requires AdvancedPaymentScheduleTransactionProcessor");
        }

        List<LoanTransaction> transactionsToReprocess = new ArrayList<>();
        List<LoanTransactionType> interestRefundTypes = loan.getSupportedInterestRefundTransactionTypes();

        List<LoanTransaction> transactions = Stream.concat(loan.getLoanTransactions().stream() //
                .filter(lt -> isTransactionNeededForInterestRefundCalculations(lt) //
                        && oldTransactionIds.contains(lt.getId())), //
                newTransactions.stream() //
                        .filter(this::isTransactionNeededForInterestRefundCalculations) //
                        .map(LoanTransaction::copyTransactionProperties)) //
                .toList();

        final AtomicReference<BigDecimal> refundFinal = new AtomicReference<>(
                transactions.stream().filter(lt -> interestRefundTypes.contains(lt.getTypeOf())) //
                        .map(LoanTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

        transactions.stream().filter(loanTransaction -> !interestRefundTypes.contains(loanTransaction.getTypeOf())) //
                .forEach(lt -> simulateRepaymentForDisbursements(lt, refundFinal, transactionsToReprocess)); //

        return recalculateTotalInterest((AdvancedPaymentScheduleTransactionProcessor) processor, loan, relatedRefundTransactionDate,
                transactionsToReprocess);
    }

    @Override
    public Money getTotalInterestRefunded(List<LoanTransaction> loanTransactions, MonetaryCurrency currency, MathContext mc) {
        final BigDecimal totalInterestRefunded = loanTransactions.stream() //
                .filter(LoanTransaction::isNotReversed) //
                .filter(LoanTransaction::isInterestRefund) //
                .map(LoanTransaction::getAmount) //
                .reduce(BigDecimal.ZERO, BigDecimal::add); //
        return Money.of(currency, totalInterestRefunded, mc);
    }

}
