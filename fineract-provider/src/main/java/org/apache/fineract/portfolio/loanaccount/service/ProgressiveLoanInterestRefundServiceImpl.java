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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
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

    private final AdvancedPaymentScheduleTransactionProcessor processor;
    private final EMICalculator emiCalculator;
    private final LoanAssembler loanAssembler;

    @Override
    public boolean canHandle(Loan loan) {
        return loan != null && loan.isInterestBearing() && processor.accept(loan.getTransactionProcessingStrategyCode());
    }

    private static void simulateRepaymentForDisbursements(LoanTransaction lt, final AtomicReference<BigDecimal> refundFinal,
            List<LoanTransaction> collect) {
        collect.add(lt);
        if (lt.getTypeOf().isDisbursement() && refundFinal.get().compareTo(BigDecimal.ZERO) > 0) {
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

    private BigDecimal totalInterest(final Loan loan, BigDecimal refundAmount, LocalDate relatedRefundTransactionDate) {
        final AtomicReference<BigDecimal> refundFinal = new AtomicReference<>(refundAmount);

        BigDecimal payableInterest = BigDecimal.ZERO;
        if (loan.getLoanTransactions().stream().anyMatch(LoanTransaction::isDisbursement)) {
            List<LoanTransaction> transactionsToReprocess = new ArrayList<>();
            loan.getLoanTransactions().stream().filter(lt -> !lt.isReversed()) //
                    .filter(lt -> !lt.isAccrual() && !lt.isAccrualActivity()) //
                    .forEach(lt -> simulateRepaymentForDisbursements(lt, refundFinal, transactionsToReprocess)); //

            List<LoanRepaymentScheduleInstallment> installmentsToReprocess = new ArrayList<>(
                    loan.getRepaymentScheduleInstallments().stream().filter(i -> !i.isReAged() && !i.isAdditional()).toList());

            ProgressiveLoanInterestScheduleModel modelAfter = processor.reprocessProgressiveLoanTransactions(loan.getDisbursementDate(),
                    transactionsToReprocess, loan.getCurrency(), installmentsToReprocess, loan.getActiveCharges()).getRight();

            payableInterest = installmentsToReprocess.stream() //
                    .map(installment -> emiCalculator //
                            .getPayableDetails(modelAfter, installment.getDueDate(), relatedRefundTransactionDate) //
                            .getPayableInterest() //
                            .getAmount()) //
                    .reduce(BigDecimal.ZERO, BigDecimal::add); //
        }
        return payableInterest;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public BigDecimal calculateInterestRefundAmount(Long loanId, BigDecimal relatedRefundTransactionAmount,
            LocalDate relatedRefundTransactionDate) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        BigDecimal totalInterestBeforeRefund = totalInterest(loan, BigDecimal.ZERO, relatedRefundTransactionDate);
        BigDecimal totalInterestAfterRefund = totalInterest(loan, relatedRefundTransactionAmount, relatedRefundTransactionDate);
        return totalInterestBeforeRefund.subtract(totalInterestAfterRefund);
    }
}
