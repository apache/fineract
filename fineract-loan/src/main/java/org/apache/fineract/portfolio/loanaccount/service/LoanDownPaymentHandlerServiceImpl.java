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

import static org.apache.fineract.portfolio.loanaccount.domain.Loan.ACTUAL_DISBURSEMENT_DATE;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanRefundValidator;

@Slf4j
@RequiredArgsConstructor
public class LoanDownPaymentHandlerServiceImpl implements LoanDownPaymentHandlerService {

    private final LoanTransactionRepository loanTransactionRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    private final LoanScheduleService loanScheduleService;
    private final LoanRefundService loanRefundService;
    private final LoanRefundValidator loanRefundValidator;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionService loanTransactionService;

    @Override
    public LoanTransaction handleDownPayment(ScheduleGeneratorDTO scheduleGeneratorDTO, JsonCommand command,
            LoanTransaction disbursementTransaction, Loan loan) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionDownPaymentPreBusinessEvent(loan));
        LoanTransaction downPaymentTransaction = handleDownPayment(loan, disbursementTransaction, command, scheduleGeneratorDTO);
        if (downPaymentTransaction != null) {
            downPaymentTransaction = loanTransactionRepository.saveAndFlush(downPaymentTransaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanTransactionDownPaymentPostBusinessEvent(downPaymentTransaction));
        }
        return downPaymentTransaction;
    }

    @Override
    public void handleRepaymentOrRecoveryOrWaiverTransaction(final Loan loan, final LoanTransaction loanTransaction,
            final LoanTransaction adjustedTransaction, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        if (loanTransaction.isRecoveryRepayment()) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_RECOVERY_PAYMENT, loan);
        }

        if (loanTransaction.isRecoveryRepayment()
                && loanTransaction.getAmount(loan.getCurrency()).getAmount().compareTo(loan.getSummary().getTotalWrittenOff()) > 0) {
            final String errorMessage = "The transaction amount cannot greater than the remaining written off amount.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.written.off", errorMessage);
        }

        loanTransaction.updateLoan(loan);

        final boolean isTransactionChronologicallyLatest = loanTransactionService.isChronologicallyLatestRepaymentOrWaiver(loan,
                loanTransaction);

        if (loanTransaction.isNotZero()) {
            loan.addLoanTransaction(loanTransaction);
        }

        if (loanTransaction.isNotRepaymentLikeType() && loanTransaction.isNotWaiver() && loanTransaction.isNotRecoveryRepayment()) {
            final String errorMessage = "A transaction of type repayment or recovery repayment or waiver was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.repayment.or.waiver.or.recovery.transaction",
                    errorMessage);
        }

        final LocalDate loanTransactionDate = loanRefundService.extractTransactionDate(loan, loanTransaction);

        if (DateUtils.isDateInTheFuture(loanTransactionDate)) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date", errorMessage, loanTransactionDate);
        }

        if (loanTransaction.isInterestWaiver()) {
            Money totalInterestOutstandingOnLoan = loan.getTotalInterestOutstandingOnLoan();
            if (adjustedTransaction != null) {
                totalInterestOutstandingOnLoan = totalInterestOutstandingOnLoan.plus(adjustedTransaction.getAmount(loan.loanCurrency()));
            }
            if (loanTransaction.getAmount(loan.getCurrency()).isGreaterThan(totalInterestOutstandingOnLoan)) {
                final String errorMessage = "The amount of interest to waive cannot be greater than total interest outstanding on loan.";
                throw new InvalidLoanStateTransitionException("waive.interest", "amount.exceeds.total.outstanding.interest", errorMessage,
                        loanTransaction.getAmount(loan.getCurrency()), totalInterestOutstandingOnLoan.getAmount());
            }
        }

        loanRefundValidator.validateTransactionAmountThreshold(loan, adjustedTransaction);

        final LoanRepaymentScheduleInstallment currentInstallment = loan
                .fetchLoanRepaymentScheduleInstallmentByDueDate(loanTransaction.getTransactionDate());

        boolean reprocessOnPostConditions = false;

        boolean processLatest = isTransactionChronologicallyLatest //
                && adjustedTransaction == null // covers reversals
                && !loan.isForeclosure() //
                && !loan.hasChargesAffectedByBackdatedRepaymentLikeTransaction(loanTransaction)
                && loanTransactionProcessingService.canProcessLatestTransactionOnly(loan, loanTransaction, currentInstallment); //
        if (processLatest) {
            loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), loanTransaction,
                    new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                            new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));
            if (!loan.isProgressiveSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
                if (currentInstallment == null || currentInstallment.isNotFullyPaidOff()) {
                    reprocessOnPostConditions = true;
                } else {
                    final LoanRepaymentScheduleInstallment nextInstallment = loan
                            .fetchRepaymentScheduleInstallment(currentInstallment.getInstallmentNumber() + 1);
                    if (nextInstallment != null && nextInstallment.getTotalPaidInAdvance(loan.getCurrency()).isGreaterThanZero()) {
                        reprocessOnPostConditions = true;
                    }
                }
            }
        }
        if (!processLatest || reprocessOnPostConditions) {
            if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
            } else if (loan.isProgressiveSchedule() && ((loan.hasChargeOffTransaction() && loan.hasAccelerateChargeOffStrategy())
                    || loan.hasContractTerminationTransaction())) {
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            }
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        }

        /**
         * FIXME: Vishwas, skipping post loan transaction checks for Loan recoveries
         **/
        if (loanTransaction.isNotRecoveryRepayment()) {
            loanLifecycleStateMachine.determineAndTransition(loan, loanTransaction.getTransactionDate());
        } else {
            loanBalanceService.updateLoanSummaryDerivedFields(loan);
        }

        if (loan.getLoanProduct().isMultiDisburseLoan()) {
            final BigDecimal totalDisbursed = loan.getDisbursedAmount();
            final BigDecimal totalPrincipalAdjusted = loan.getSummary().getTotalPrincipalAdjustments();
            final BigDecimal totalCapitalizedIncome = loan.getSummary().getTotalCapitalizedIncome();
            final BigDecimal totalPrincipalCredited = totalDisbursed.add(totalPrincipalAdjusted).add(totalCapitalizedIncome);
            if (totalPrincipalCredited.compareTo(loan.getSummary().getTotalPrincipalRepaid()) < 0
                    && loan.getLoanProductRelatedDetail().getPrincipal().minus(totalDisbursed).isGreaterThanZero()) {
                final String errorMessage = "The transaction amount cannot exceed threshold.";
                throw new InvalidLoanStateTransitionException("transaction", "amount.exceeds.threshold", errorMessage);
            }
        }
    }

    private LoanTransaction handleDownPayment(final Loan loan, final LoanTransaction disbursementTransaction, final JsonCommand command,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final LocalDate disbursedOn = command.localDateValueOfParameterNamed(ACTUAL_DISBURSEMENT_DATE);
        final BigDecimal disbursedAmountPercentageForDownPayment = loan.getLoanRepaymentScheduleDetail()
                .getDisbursedAmountPercentageForDownPayment();
        ExternalId externalId = ExternalId.empty();
        if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }
        Money downPaymentMoney = Money.of(loan.getCurrency(),
                MathUtil.percentageOf(disbursementTransaction.getAmount(), disbursedAmountPercentageForDownPayment, 19));
        if (loan.getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf() != null) {
            downPaymentMoney = Money.roundToMultiplesOf(downPaymentMoney,
                    loan.getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf());
        }
        final Money adjustedDownPaymentMoney = switch (loan.getLoanProductRelatedDetail().getLoanScheduleType()) {
            // For Cumulative loan: To check whether the loan was overpaid when the disbursement happened and to get the
            // proper amount after the disbursement we are using two balances:
            // 1. Whether the loan is still overpaid after the disbursement,
            // 2. if the loan is not overpaid anymore after the disbursement, but was it more overpaid than the
            // calculated down-payment amount?
            case CUMULATIVE -> {
                if (loan.getTotalOverpaidAsMoney().isGreaterThanZero()) {
                    yield Money.zero(loan.getCurrency());
                }
                yield MathUtil.negativeToZero(downPaymentMoney.minus(MathUtil.negativeToZero(disbursementTransaction
                        .getAmount(loan.getCurrency()).minus(disbursementTransaction.getOutstandingLoanBalanceMoney(loan.getCurrency())))));
            }
            // For Progressive loan: Disbursement transaction portion balances are enough to see whether the overpayment
            // amount was more than the calculated down-payment amount
            case PROGRESSIVE ->
                MathUtil.negativeToZero(downPaymentMoney.minus(disbursementTransaction.getOverPaymentPortion(loan.getCurrency())));
        };

        if (adjustedDownPaymentMoney.isGreaterThanZero()) {
            final LoanTransaction downPaymentTransaction = LoanTransaction.downPayment(loan.getOffice(), adjustedDownPaymentMoney, null,
                    disbursedOn, externalId);
            final LoanEvent event = LoanEvent.LOAN_REPAYMENT_OR_WAIVER;
            loanDownPaymentTransactionValidator.validateRepaymentTypeAccountStatus(loan, downPaymentTransaction, event);
            final HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
            loanDownPaymentTransactionValidator.validateRepaymentDateIsOnHoliday(downPaymentTransaction.getTransactionDate(),
                    holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
            loanDownPaymentTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(downPaymentTransaction.getTransactionDate(),
                    holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());

            handleRepaymentOrRecoveryOrWaiverTransaction(loan, downPaymentTransaction, null, scheduleGeneratorDTO);
            return downPaymentTransaction;
        } else {
            return null;
        }
    }
}
