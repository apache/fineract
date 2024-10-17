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

    private ProgressiveLoanInterestScheduleModel getModelSafely(final Loan loan, BigDecimal refundAmount) {
        final AtomicReference<BigDecimal> refundFinal = new AtomicReference<>(refundAmount);
        List<LoanTransaction> withoutSpecificDisbursements = loan.getLoanTransactions().stream().filter(lt -> !lt.isReversed())
                .filter(lt -> !lt.isAccrual() && !lt.isAccrualActivity() && !lt.isReversed()).filter(lt -> {
                    if (lt.getTypeOf().isDisbursement() && refundFinal.get().compareTo(BigDecimal.ZERO) > 0) {
                        if (lt.getAmount().compareTo(refundFinal.get()) <= 0) {
                            refundFinal.set(refundFinal.get().subtract(lt.getAmount()));
                            return false;
                        }
                    }
                    return true;
                }).toList();
        List<LoanTransaction> transactionsToReprocess = withoutSpecificDisbursements.stream().map(lt -> {
            if (lt.getTypeOf().isDisbursement() && refundFinal.get().compareTo(BigDecimal.ZERO) > 0) {
                refundFinal.set(BigDecimal.ZERO);
                return new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), lt.getTypeOf().getValue(), lt.getDateOf(),
                        lt.getAmount().subtract(refundFinal.get()), lt.getPrincipalPortion(), lt.getInterestPortion(),
                        lt.getFeeChargesPortion(), lt.getPenaltyChargesPortion(),
                        lt.getOverPaymentPortion(lt.getLoan().getCurrency()).getAmount(), lt.isReversed(), lt.getPaymentDetail(),
                        lt.getExternalId());
            }
            return lt;
        }).toList();

        List<LoanRepaymentScheduleInstallment> installmentsToReprocess = new ArrayList<>(
                loan.getRepaymentScheduleInstallments().stream().filter(i -> !i.isReAged() && !i.isAdditional()).toList());

        return processor.reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), transactionsToReprocess, loan.getCurrency(),
                installmentsToReprocess, loan.getActiveCharges()).getRight();
    }

    @Override
    public boolean canHandle(Loan loan) {
        return loan != null && loan.isInterestBearing() && processor.accept(loan.getTransactionProcessingStrategyCode());
    }

    private BigDecimal calculateTotalPaidInterest(final Loan loan) {
        return loan.getRepaymentScheduleInstallments().stream().map(i -> i.getInterestPaid(loan.getCurrency()).getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePayableInterest(final Loan loan, LocalDate relatedRefundTransactionDate) {
        ProgressiveLoanInterestScheduleModel modelAfter = getModelSafely(loan, BigDecimal.ZERO);
        return emiCalculator.getPayableDetails(modelAfter,
                loan.getRelatedRepaymentScheduleInstallment(relatedRefundTransactionDate).getDueDate(), relatedRefundTransactionDate)
                .getPayableInterest().getAmount();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public BigDecimal calculateInterestRefundAmount(Long loanId, BigDecimal relatedRefundTransactionAmount,
            LocalDate relatedRefundTransactionDate) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        BigDecimal totalPaidInterest = calculateTotalPaidInterest(loan);
        BigDecimal totalPayableInterestBeforeTransaction = calculatePayableInterest(loan, relatedRefundTransactionDate);
        ProgressiveLoanInterestScheduleModel modelAfter = getModelSafely(loan, relatedRefundTransactionAmount);
        BigDecimal payableInterestWithoutDisbursement = BigDecimal.ZERO;
        if (modelAfter != null) {
            payableInterestWithoutDisbursement = emiCalculator.getPayableDetails(modelAfter,
                    loan.getRelatedRepaymentScheduleInstallment(relatedRefundTransactionDate).getDueDate(), relatedRefundTransactionDate)
                    .getPayableInterest().getAmount();

        }
        return totalPaidInterest.add(totalPayableInterestBeforeTransaction).subtract(payableInterestWithoutDisbursement);
    }
}
