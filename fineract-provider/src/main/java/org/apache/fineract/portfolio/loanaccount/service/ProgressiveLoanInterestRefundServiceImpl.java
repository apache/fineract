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
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.starter.AdvancedPaymentScheduleTransactionProcessorCondition;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
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
    private final LoanScheduleService loanScheduleService;
    private final LoanUtilService loanUtilService;
    private final LoanTransactionProcessingService loanTransactionProcessingService;

    private static void copyTransactionForSimulation(LoanTransaction lt, List<LoanTransaction> collect) {
        // The refunded amount is no longer removed here: instead it is passed down to the transaction processor as
        // an "amortizable principal reduction" (see reprocessProgressiveLoanTransactions below), which shrinks only
        // the amortizable part of a disbursement. Shrinking the disbursement amount itself would also shrink the
        // simulated down-payment installment, while the real DOWN_PAYMENT transaction copy keeps its full amount;
        // the resulting mismatch lets the excess flow through the normal in-advance allocation order and pay off
        // fees that a real, already-recorded charge payment had covered (see FINERACT-3014).
        LoanTransaction copy = new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), lt.getTypeOf(), lt.getDateOf(), lt.getAmount(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null);
        if (LoanTransactionType.CHARGE_PAYMENT.equals(copy.getTypeOf())
                || LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.equals(copy.getTypeOf())) {
            copy.getLoanChargesPaid().addAll(lt.getLoanChargesPaid());
        }
        if (LoanTransactionType.REAGE.equals(copy.getTypeOf())) {
            copy.setLoanReAgeParameter(lt.getLoanReAgeParameter().getCopy(copy));
        }
        if (LoanTransactionType.REAMORTIZE.equals(copy.getTypeOf())) {
            copy.setLoanReAmortizationParameter(lt.getLoanReAmortizationParameter());
        }
        collect.add(copy);
    }

    private Money recalculateTotalInterest(AdvancedPaymentScheduleTransactionProcessor processor, Loan loan,
            LocalDate relatedRefundTransactionDate, List<LoanTransaction> transactionsToReprocess, Money amortizablePrincipalReduction) {

        final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
        loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);

        Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> reprocessResult = processor
                .reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), relatedRefundTransactionDate, transactionsToReprocess,
                        loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                        amortizablePrincipalReduction);

        final List<LoanTransaction> newTransactions = reprocessResult.getLeft().getTransactionChanges().stream()
                .map(TransactionChangeData::getNewTransaction).toList().stream().filter(LoanTransaction::isNotReversed).toList();
        loan.getLoanTransactions().addAll(newTransactions);
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
            LocalDate relatedRefundTransactionDate, List<LoanTransaction> newTransactions, List<Long> oldTransactionIds,
            List<LoanTermVariations> activeLoanTermVariations) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        loan.setLoanTermVariations(activeLoanTermVariations);
        if (processor == null) {
            processor = loanTransactionProcessingService.getTransactionProcessor(loan.getTransactionProcessingStrategyCode());
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

        BigDecimal refundTotal = transactions.stream().filter(lt -> interestRefundTypes.contains(lt.getTypeOf())) //
                .map(LoanTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        transactions.stream().filter(loanTransaction -> !interestRefundTypes.contains(loanTransaction.getTypeOf())) //
                .forEach(lt -> copyTransactionForSimulation(lt, transactionsToReprocess)); //

        return recalculateTotalInterest((AdvancedPaymentScheduleTransactionProcessor) processor, loan, relatedRefundTransactionDate,
                transactionsToReprocess, Money.of(loan.getCurrency(), refundTotal));
    }
}
