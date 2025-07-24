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

import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.ProgressiveTransactionCtx;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTermVariationsMapper;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class LoanTransactionProcessingServiceImpl implements LoanTransactionProcessingService {

    private final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;
    private final LoanTermVariationsMapper loanMapper;
    private final InterestScheduleModelRepositoryWrapper modelRepository;
    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionService loanTransactionService;

    @Override
    public boolean canProcessLatestTransactionOnly(Loan loan, LoanTransaction loanTransaction,
            LoanRepaymentScheduleInstallment currentInstallment) {
        if (!loan.isInterestBearingAndInterestRecalculationEnabled()) {
            return true;
        }
        if (!DateUtils.isEqualBusinessDate(loanTransaction.getTransactionDate())) {
            return false;
        }
        if (loan.hasChargesAffectedByBackdatedRepaymentLikeTransaction(loanTransaction)) {
            return false;
        }
        LoanInterestRecalculationDetails interestRecalculationDetails = loan.getLoanInterestRecalculationDetails();
        if (interestRecalculationDetails != null && ((interestRecalculationDetails.getRestFrequencyType().isSameAsRepayment()
                && interestRecalculationDetails.getPreCloseInterestCalculationStrategy().calculateTillPreClosureDateEnabled())
                || (interestRecalculationDetails.getRestFrequencyType().isDaily()
                        && interestRecalculationDetails.getPreCloseInterestCalculationStrategy().calculateTillRestFrequencyEnabled()))) {
            return false;
        }
        if (loan.isProgressiveSchedule()) {
            return modelRepository.hasValidModelForDate(loan.getId(), loanTransaction.getTransactionDate());
        }
        return currentInstallment != null
                && currentInstallment.getTotalOutstanding(loan.getCurrency()).isEqualTo(loanTransaction.getAmount(loan.getCurrency()));
    }

    private ChangedTransactionDetail processLatestTransactionProgressiveInterestRecalculation(
            AdvancedPaymentScheduleTransactionProcessor advancedProcessor, Loan loan, LoanTransaction loanTransaction) {
        Optional<ProgressiveLoanInterestScheduleModel> savedModel = modelRepository.getSavedModel(loan,
                loanTransaction.getTransactionDate());
        ProgressiveLoanInterestScheduleModel model = savedModel
                .orElseGet(() -> advancedProcessor.calculateInterestScheduleModel(loan.getId(), loanTransaction.getTransactionDate()));

        ProgressiveTransactionCtx progressiveContext = new ProgressiveTransactionCtx(loan.getCurrency(),
                loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(), new MoneyHolder(loan.getTotalOverpaidAsMoney()),
                new ChangedTransactionDetail(), model, getTotalRefundInterestAmount(loan));
        progressiveContext.getAlreadyProcessedTransactions().addAll(loanTransactionService.retrieveListOfTransactionsForReprocessing(loan));
        progressiveContext.setChargedOff(loan.isChargedOff());
        progressiveContext.setContractTerminated(loan.isContractTermination());
        ChangedTransactionDetail result = advancedProcessor.processLatestTransaction(loanTransaction, progressiveContext);
        if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            modelRepository.writeInterestScheduleModel(loan, model);
        }
        return result;
    }

    private Money getTotalRefundInterestAmount(Loan loan) {
        List<LoanTransactionType> supportedInterestRefundTransactionTypes = loan.getSupportedInterestRefundTransactionTypes();
        if (supportedInterestRefundTransactionTypes != null && supportedInterestRefundTransactionTypes.isEmpty()) {
            return Money.zero(loan.getCurrency());
        }
        return loan.getLoanTransactions().stream().filter(LoanTransaction::isNotReversed).filter(LoanTransaction::isInterestRefund)
                .map(t -> t.getAmount(loan.getCurrency())).reduce(Money.zero(loan.getCurrency()), Money::add);
    }

    @Override
    public ChangedTransactionDetail processLatestTransaction(String transactionProcessingStrategyCode, LoanTransaction loanTransaction,
            TransactionCtx ctx) {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor(
                transactionProcessingStrategyCode);
        if (loanRepaymentScheduleTransactionProcessor instanceof AdvancedPaymentScheduleTransactionProcessor advancedProcessor
                && loanTransaction.getLoan().isInterestRecalculationEnabled()) {
            return processLatestTransactionProgressiveInterestRecalculation(advancedProcessor, loanTransaction.getLoan(), loanTransaction);
        }
        return loanRepaymentScheduleTransactionProcessor.processLatestTransaction(loanTransaction, ctx);
    }

    private Loan getLoan(List<LoanTransaction> loanTransactions, List<LoanRepaymentScheduleInstallment> installments,
            Set<LoanCharge> charges) {
        if (!ObjectUtils.isEmpty(loanTransactions)) {
            return loanTransactions.getFirst().getLoan();
        } else if (!ObjectUtils.isEmpty(installments)) {
            return installments.getFirst().getLoan();
        } else if (!ObjectUtils.isEmpty(charges)) {
            return charges.iterator().next().getLoan();
        } else {
            throw new IllegalArgumentException("No loan found for the given transactions, installments or charges");
        }
    }

    @Override
    public ChangedTransactionDetail reprocessLoanTransactions(String transactionProcessingStrategyCode, LocalDate disbursementDate,
            List<LoanTransaction> loanTransactions, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            Set<LoanCharge> charges) {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor(
                transactionProcessingStrategyCode);
        if (loanRepaymentScheduleTransactionProcessor instanceof AdvancedPaymentScheduleTransactionProcessor advancedProcessor) {
            LocalDate currentDate = DateUtils.getBusinessLocalDate();
            Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> result = advancedProcessor
                    .reprocessProgressiveLoanTransactions(disbursementDate, currentDate, loanTransactions, currency, installments, charges);
            if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                modelRepository.writeInterestScheduleModel(getLoan(loanTransactions, installments, charges), result.getRight());
            }
            return result.getLeft();
        } else {
            return loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(disbursementDate, loanTransactions, currency,
                    installments, charges);
        }
    }

    @Override
    public LoanRepaymentScheduleTransactionProcessor getTransactionProcessor(String transactionProcessingStrategyCode) {
        return transactionProcessorFactory.determineProcessor(transactionProcessingStrategyCode);
    }

    @Override
    public Optional<ChangedTransactionDetail> processPostDisbursementTransactions(Loan loan) {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor(
                loan.getTransactionProcessingStrategyCode());
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = loanTransactionService
                .retrieveListOfTransactionsForReprocessing(loan);

        final List<LoanTransaction> copyTransactions = new ArrayList<>();

        if (allNonContraTransactionsPostDisbursement.isEmpty()) {
            return Optional.empty();
        }

        // TODO: Probably this is not needed and can be eliminated, make sure to double check it
        for (LoanTransaction loanTransaction : allNonContraTransactionsPostDisbursement) {
            copyTransactions.add(LoanTransaction.copyTransactionProperties(loanTransaction));
        }
        final ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(
                loan.getDisbursementDate(), copyTransactions, loan.getCurrency(), loan.getRepaymentScheduleInstallments(),
                loan.getActiveCharges());

        loanBalanceService.updateLoanSummaryDerivedFields(loan);

        return Optional.of(changedTransactionDetail);
    }

    @Override
    public LoanScheduleDTO getRecalculatedSchedule(final ScheduleGeneratorDTO generatorDTO, Loan loan) {
        if (!loan.isInterestBearingAndInterestRecalculationEnabled() || loan.isNpa() || loan.isChargedOff()) {
            return null;
        }
        final InterestMethod interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod();
        final LoanScheduleGenerator loanScheduleGenerator = generatorDTO.getLoanScheduleFactory()
                .create(loan.getLoanRepaymentScheduleDetail().getLoanScheduleType(), interestMethod);

        final MathContext mc = MoneyHelper.getMathContext();

        final LoanApplicationTerms loanApplicationTerms = loanMapper.constructLoanApplicationTerms(generatorDTO, loan);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor(
                loan.getTransactionProcessingStrategyCode());

        return loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, loan, generatorDTO.getHolidayDetailDTO(),
                loanRepaymentScheduleTransactionProcessor, generatorDTO.getRecalculateFrom(), generatorDTO.getRecalculateTill());
    }

    @Override
    public OutstandingAmountsDTO fetchPrepaymentDetail(final ScheduleGeneratorDTO scheduleGeneratorDTO, final LocalDate onDate, Loan loan) {
        OutstandingAmountsDTO outstandingAmounts;

        if (loan.isInterestBearingAndInterestRecalculationEnabled() && !loan.isChargeOffOnDate(onDate)) {
            final MathContext mc = MoneyHelper.getMathContext();

            final InterestMethod interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod();
            final LoanApplicationTerms loanApplicationTerms = loanMapper.constructLoanApplicationTerms(scheduleGeneratorDTO, loan);

            final LoanScheduleGenerator loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory()
                    .create(loanApplicationTerms.getLoanScheduleType(), interestMethod);
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor(
                    loan.getTransactionProcessingStrategyCode());
            outstandingAmounts = loanScheduleGenerator.calculatePrepaymentAmount(loan.getCurrency(), onDate, loanApplicationTerms, mc, loan,
                    scheduleGeneratorDTO.getHolidayDetailDTO(), loanRepaymentScheduleTransactionProcessor);
        } else {
            outstandingAmounts = getTotalOutstandingOnLoan(loan);
        }
        return outstandingAmounts;
    }

    private OutstandingAmountsDTO getTotalOutstandingOnLoan(Loan loan) {
        Money totalPrincipal = Money.zero(loan.getCurrency());
        Money totalInterest = Money.zero(loan.getCurrency());
        Money feeCharges = Money.zero(loan.getCurrency());
        Money penaltyCharges = Money.zero(loan.getCurrency());
        List<LoanRepaymentScheduleInstallment> repaymentSchedule = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : repaymentSchedule) {
            totalPrincipal = totalPrincipal.plus(scheduledRepayment.getPrincipalOutstanding(loan.getCurrency()));
            totalInterest = totalInterest.plus(scheduledRepayment.getInterestOutstanding(loan.getCurrency()));
            feeCharges = feeCharges.plus(scheduledRepayment.getFeeChargesOutstanding(loan.getCurrency()));
            penaltyCharges = penaltyCharges.plus(scheduledRepayment.getPenaltyChargesOutstanding(loan.getCurrency()));
        }
        return new OutstandingAmountsDTO(totalPrincipal.getCurrency()).principal(totalPrincipal).interest(totalInterest)
                .feeCharges(feeCharges).penaltyCharges(penaltyCharges);
    }
}
