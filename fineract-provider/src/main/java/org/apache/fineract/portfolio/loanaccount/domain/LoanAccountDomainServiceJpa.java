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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargePaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargePaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCreditBalanceRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCreditBalanceRefundPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanForeClosurePostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanForeClosurePreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanRefundPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionGoodwillCreditPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionGoodwillCreditPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestPaymentWaiverPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestPaymentWaiverPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestRefundPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMakeRepaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMakeRepaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMerchantIssuedRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMerchantIssuedRefundPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionPayoutRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionPayoutRefundPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionRecoveryPaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionRecoveryPaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformService;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanRefundRequestData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanForeclosureValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.InterestRefundService;
import org.apache.fineract.portfolio.loanaccount.service.InterestRefundServiceDelegate;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeService;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentHandlerService;
import org.apache.fineract.portfolio.loanaccount.service.LoanRefundService;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanSupportedInterestRefundTypes;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.data.PostDatedChecksStatus;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanAccountDomainServiceJpa implements LoanAccountDomainService {

    private final LoanAssembler loanAccountAssembler;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepository holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;

    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final NoteRepository noteRepository;
    private final AccountTransferRepository accountTransferRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanUtilService loanUtilService;
    private final StandingInstructionRepository standingInstructionRepository;
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final LoanCollateralManagementRepository loanCollateralManagementRepository;
    private final DelinquencyWritePlatformService delinquencyWritePlatformService;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;
    private final ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;
    private final LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;
    private final InterestRefundServiceDelegate interestRefundServiceDelegate;
    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanForeclosureValidator loanForeclosureValidator;
    private final LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    private final LoanChargeService loanChargeService;
    private final LoanScheduleService loanScheduleService;
    private final LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanRefundService loanRefundService;

    @Transactional
    @Override
    public LoanTransaction makeRepayment(final LoanTransactionType repaymentTransactionType, final Loan loan,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText,
            final ExternalId txnExternalId, final boolean isRecoveryRepayment, final String chargeRefundChargeType,
            boolean isAccountTransfer, HolidayDetailDTO holidayDetailDto, Boolean isHolidayValidationDone) {
        return makeRepayment(repaymentTransactionType, loan, transactionDate, transactionAmount, paymentDetail, noteText, txnExternalId,
                isRecoveryRepayment, chargeRefundChargeType, isAccountTransfer, holidayDetailDto, isHolidayValidationDone, false);
    }

    @Transactional
    @Override
    public void updateLoanCollateralStatus(Set<LoanCollateralManagement> loanCollateralManagementSet, boolean isReleased) {
        for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagementSet) {
            loanCollateralManagement.setIsReleased(isReleased);
        }
        this.loanCollateralManagementRepository.saveAll(loanCollateralManagementSet);
    }

    @Nullable
    private LoanTransaction createInterestRefundLoanTransaction(Loan loan, LoanTransaction refundTransaction) {

        InterestRefundService interestRefundService = interestRefundServiceDelegate.lookupInterestRefundService(loan);
        if (interestRefundService == null) {
            return null;
        }

        Money totalInterest = interestRefundService.totalInterestByTransactions(null, loan.getId(), refundTransaction.getTransactionDate(),
                List.of(), loan.getLoanTransactions().stream().map(AbstractPersistableCustom::getId).toList());
        Money previouslyRefundedInterests = interestRefundService.getTotalInterestRefunded(loan.getLoanTransactions(), loan.getCurrency(),
                totalInterest.getMc());

        Money newTotalInterest = interestRefundService.totalInterestByTransactions(null, loan.getId(),
                refundTransaction.getTransactionDate(), List.of(refundTransaction),
                loan.getLoanTransactions().stream().map(AbstractPersistableCustom::getId).toList());
        BigDecimal interestRefundAmount = totalInterest.minus(previouslyRefundedInterests).minus(newTotalInterest).getAmount();

        if (MathUtil.isZero(interestRefundAmount)) {
            return null;
        }
        final ExternalId txnExternalId = externalIdFactory.create();
        businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionInterestRefundPreBusinessEvent(loan));
        return LoanTransaction.interestRefund(loan, interestRefundAmount, refundTransaction.getDateOf(), txnExternalId);

    }

    @Transactional
    @Override
    public LoanTransaction makeRepayment(final LoanTransactionType repaymentTransactionType, Loan loan, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText, final ExternalId txnExternalId,
            final boolean isRecoveryRepayment, final String chargeRefundChargeType, boolean isAccountTransfer,
            HolidayDetailDTO holidayDetailDto, Boolean isHolidayValidationDone, final boolean isLoanToLoanTransfer) {
        checkClientOrGroupActive(loan);

        LoanBusinessEvent repaymentEvent = getLoanRepaymentTypeBusinessEvent(repaymentTransactionType, isRecoveryRepayment, loan);
        businessEventNotifierService.notifyPreBusinessEvent(repaymentEvent);

        // TODO: Is it required to validate transaction date with meeting dates
        // if repayments is synced with meeting?
        /*
         * if(loan.isSyncDisbursementWithMeeting()){ // validate actual disbursement date against meeting date
         * CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByLoanId
         * (loan.getId(), CalendarEntityType.LOANS.getValue()); this.loanEventApiJsonValidator
         * .validateRepaymentDateWithMeetingDate(transactionDate, calendarInstance); }
         */

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money repaymentAmount = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction newRepaymentTransaction;
        if (isRecoveryRepayment) {
            newRepaymentTransaction = LoanTransaction.recoveryRepayment(loan.getOffice(), repaymentAmount, paymentDetail, transactionDate,
                    txnExternalId);
        } else {
            newRepaymentTransaction = LoanTransaction.repaymentType(repaymentTransactionType, loan.getOffice(), repaymentAmount,
                    paymentDetail, transactionDate, txnExternalId, chargeRefundChargeType);
        }

        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }
        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom,
                holidayDetailDto);

        if (!isHolidayValidationDone) {
            final HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
            loanTransactionValidator.validateRepaymentDateIsOnHoliday(newRepaymentTransaction.getTransactionDate(),
                    holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
            loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(newRepaymentTransaction.getTransactionDate(),
                    holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
        }
        final LoanEvent event = isRecoveryRepayment ? LoanEvent.LOAN_RECOVERY_PAYMENT : LoanEvent.LOAN_REPAYMENT_OR_WAIVER;
        loanTransactionValidator.validateActivityNotBeforeLastTransactionDate(loan, newRepaymentTransaction.getTransactionDate(), event);
        loanDownPaymentTransactionValidator.validateRepaymentTypeAccountStatus(loan, newRepaymentTransaction, event);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, event,
                newRepaymentTransaction.getTransactionDate());
        final ChangedTransactionDetail changedTransactionDetail = makeRepayment(loan, newRepaymentTransaction,
                defaultLoanLifecycleStateMachine, existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO);

        if (loan.getLoanRepaymentScheduleDetail().isInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.reprocessExistingAccruals(loan);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan);
        }

        saveLoanTransactionWithDataIntegrityViolationChecks(newRepaymentTransaction);

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a payment is made
         * before the latest payment recorded against the loan)
         ***/
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                saveLoanTransactionWithDataIntegrityViolationChecks(mapEntry.getValue());
                updateLoanTransferTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
            // Trigger transaction replayed event
            replayedTransactionBusinessEventService.raiseTransactionReplayedEvents(changedTransactionDetail);
        }
        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRepaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan,
                loan.repaymentScheduleDetail().isInterestRecalculationEnabled(), false);

        setLoanDelinquencyTag(loan, transactionDate);

        if (!repaymentTransactionType.isChargeRefund()) {
            LoanTransactionBusinessEvent transactionRepaymentEvent = getTransactionRepaymentTypeBusinessEvent(repaymentTransactionType,
                    isRecoveryRepayment, newRepaymentTransaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
            businessEventNotifierService.notifyPostBusinessEvent(transactionRepaymentEvent);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, isLoanToLoanTransfer);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);

        // disable all active standing orders linked to this loan if status
        // changes to closed
        disableStandingInstructionsLinkedToClosedLoan(loan);

        if (loan.getLoanType().isIndividualAccount()) {
            // Mark Post Dated Check as paid.
            final Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = newRepaymentTransaction
                    .getLoanTransactionToRepaymentScheduleMappings();

            if (loanTransactionToRepaymentScheduleMappings != null) {
                for (LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping : loanTransactionToRepaymentScheduleMappings) {
                    LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loanTransactionToRepaymentScheduleMapping
                            .getLoanRepaymentScheduleInstallment();
                    if (loanRepaymentScheduleInstallment != null) {
                        final boolean isPaid = loanRepaymentScheduleInstallment.isNotFullyPaidOff();
                        PostDatedChecks postDatedChecks = this.postDatedChecksRepository
                                .getPendingPostDatedCheck(loanRepaymentScheduleInstallment);

                        if (postDatedChecks != null) {
                            if (!isPaid) {
                                postDatedChecks.setStatus(PostDatedChecksStatus.POST_DATED_CHECKS_PAID);
                            } else {
                                postDatedChecks.setStatus(PostDatedChecksStatus.POST_DATED_CHECKS_PENDING);
                            }
                            this.postDatedChecksRepository.saveAndFlush(postDatedChecks);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return newRepaymentTransaction;
    }

    private LoanBusinessEvent getLoanRepaymentTypeBusinessEvent(LoanTransactionType repaymentTransactionType, boolean isRecoveryRepayment,
            Loan loan) {
        LoanBusinessEvent repaymentEvent = null;
        if (repaymentTransactionType.isRepayment()) {
            repaymentEvent = new LoanTransactionMakeRepaymentPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isMerchantIssuedRefund()) {
            repaymentEvent = new LoanTransactionMerchantIssuedRefundPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isPayoutRefund()) {
            repaymentEvent = new LoanTransactionPayoutRefundPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isGoodwillCredit()) {
            repaymentEvent = new LoanTransactionGoodwillCreditPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isInterestPaymentWaiver()) {
            repaymentEvent = new LoanTransactionInterestPaymentWaiverPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isChargeRefund()) {
            repaymentEvent = new LoanChargePaymentPreBusinessEvent(loan);
        } else if (isRecoveryRepayment) {
            repaymentEvent = new LoanTransactionRecoveryPaymentPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isDownPayment()) {
            repaymentEvent = new LoanTransactionDownPaymentPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isInterestRefund()) {
            repaymentEvent = new LoanTransactionInterestRefundPreBusinessEvent(loan);
        }
        return repaymentEvent;
    }

    private LoanTransactionBusinessEvent getTransactionRepaymentTypeBusinessEvent(LoanTransactionType repaymentTransactionType,
            boolean isRecoveryRepayment, LoanTransaction transaction) {
        LoanTransactionBusinessEvent repaymentEvent = null;
        if (repaymentTransactionType.isRepayment()) {
            repaymentEvent = new LoanTransactionMakeRepaymentPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isMerchantIssuedRefund()) {
            repaymentEvent = new LoanTransactionMerchantIssuedRefundPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isPayoutRefund()) {
            repaymentEvent = new LoanTransactionPayoutRefundPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isGoodwillCredit()) {
            repaymentEvent = new LoanTransactionGoodwillCreditPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isInterestPaymentWaiver()) {
            repaymentEvent = new LoanTransactionInterestPaymentWaiverPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isChargeRefund()) {
            repaymentEvent = new LoanChargePaymentPostBusinessEvent(transaction);
        } else if (isRecoveryRepayment) {
            repaymentEvent = new LoanTransactionRecoveryPaymentPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isDownPayment()) {
            repaymentEvent = new LoanTransactionDownPaymentPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isInterestRefund()) {
            repaymentEvent = new LoanTransactionInterestRefundPostBusinessEvent(transaction);
        }
        return repaymentEvent;
    }

    @Override
    public LoanTransaction saveLoanTransactionWithDataIntegrityViolationChecks(LoanTransaction newRepaymentTransaction) {
        try {
            return this.loanTransactionRepository.saveAndFlush(newRepaymentTransaction);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            raiseValidationExceptionForUniqueConstraintViolation(e);
            throw e;
        }
    }

    @Override
    public Loan saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            return this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            raiseValidationExceptionForUniqueConstraintViolation(e);
            throw e;
        }
    }

    @Override
    public Loan saveLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            return this.loanRepositoryWrapper.save(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            raiseValidationExceptionForUniqueConstraintViolation(e);
            throw e;
        }
    }

    private void raiseValidationExceptionForUniqueConstraintViolation(Exception e) {
        final Throwable realCause = e.getCause();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
        if (realCause.getMessage().toLowerCase().contains("external_id_unique") || realCause.getMessage()
                .contains("duplicate key value violates unique constraint \"m_loan_transaction_external_id_key\"")) {
            baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors, e);
        }
    }

    @Override
    @Transactional
    public LoanTransaction makeChargePayment(final Loan loan, final Long chargeId, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText, final ExternalId txnExternalId,
            final Integer transactionType, Integer installmentNumber) {
        boolean isAccountTransfer = true;
        checkClientOrGroupActive(loan);
        if (loan.isChargedOff() && DateUtils.isBefore(transactionDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanChargePaymentPreBusinessEvent(loan));

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money paymentAmout = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransactionType loanTransactionType = LoanTransactionType.fromInt(transactionType);

        final LoanTransaction newPaymentTransaction = LoanTransaction.loanPayment(null, loan.getOffice(), paymentAmout, paymentDetail,
                transactionDate, txnExternalId, loanTransactionType);

        if (loanTransactionType.isRepaymentAtDisbursement()) {
            loan.handlePayDisbursementTransaction(chargeId, newPaymentTransaction, existingTransactionIds, existingReversedTransactionIds);
        } else {
            final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
            final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), transactionDate,
                    HolidayStatusType.ACTIVE.getValue());
            final WorkingDays workingDays = this.workingDaysRepository.findOne();
            final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
            final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
            HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                    allowTransactionsOnNonWorkingDay);

            loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_CHARGE_PAYMENT);
            loanTransactionValidator.validateRepaymentDateIsOnHoliday(newPaymentTransaction.getTransactionDate(),
                    holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
            loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(newPaymentTransaction.getTransactionDate(),
                    holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
            loanTransactionValidator.validateActivityNotBeforeLastTransactionDate(loan, newPaymentTransaction.getTransactionDate(),
                    LoanEvent.LOAN_CHARGE_PAYMENT);
            loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_CHARGE_PAYMENT,
                    newPaymentTransaction.getTransactionDate());
            loanChargeService.makeChargePayment(loan, chargeId, defaultLoanLifecycleStateMachine, existingTransactionIds,
                    existingReversedTransactionIds, newPaymentTransaction, installmentNumber);
        }
        saveLoanTransactionWithDataIntegrityViolationChecks(newPaymentTransaction);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newPaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan,
                loan.repaymentScheduleDetail().isInterestRecalculationEnabled(), false);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargePaymentPostBusinessEvent(newPaymentTransaction));

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        return newPaymentTransaction;
    }

    private void postJournalEntries(final Loan loanAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer) {
        postJournalEntries(loanAccount, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, false);
    }

    private void postJournalEntries(final Loan loanAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer, boolean isLoanToLoanTransfer) {

        final MonetaryCurrency currency = loanAccount.getCurrency();

        List<Map<String, Object>> accountingBridgeData = new ArrayList<>();
        if (loanAccount.isChargedOff()) {
            accountingBridgeData = loanAccount.deriveAccountingBridgeDataForChargeOff(currency.getCode(), existingTransactionIds,
                    existingReversedTransactionIds, isAccountTransfer);
        } else {
            accountingBridgeData.add(loanAccount.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                    existingReversedTransactionIds, isAccountTransfer));
        }
        for (Map<String, Object> accountingData : accountingBridgeData) {
            accountingData.put("isLoanToLoanTransfer", isLoanToLoanTransfer);
            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingData);
        }

    }

    private void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = loan.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }

    @Override
    public LoanTransaction makeRefund(final Long accountId, final CommandProcessingResultBuilder builderResult,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final String noteText,
            final ExternalId txnExternalId) {
        boolean isAccountTransfer = true;
        final Loan loan = this.loanAccountAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);
        if (loan.isChargedOff() && DateUtils.isBefore(transactionDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanRefundPreBusinessEvent(loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        final LoanTransaction newRefundTransaction = LoanTransaction.refund(loan.getOffice(), refundAmount, paymentDetail, transactionDate,
                txnExternalId);
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), transactionDate,
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loanTransactionValidator.validateRepaymentDateIsOnHoliday(newRefundTransaction.getTransactionDate(), allowTransactionsOnHoliday,
                holidays);
        loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(newRefundTransaction.getTransactionDate(), workingDays,
                allowTransactionsOnNonWorkingDay);

        loanRefundService.makeRefund(loan, newRefundTransaction, defaultLoanLifecycleStateMachine, existingTransactionIds,
                existingReversedTransactionIds);

        saveLoanTransactionWithDataIntegrityViolationChecks(newRefundTransaction);
        this.loanRepositoryWrapper.saveAndFlush(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRefundPostBusinessEvent(newRefundTransaction));
        builderResult.withEntityId(newRefundTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withGroupId(loan.getGroupId());

        return newRefundTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction makeDisburseTransaction(final Long loanId, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PaymentDetail paymentDetail, final String noteText, final ExternalId txnExternalId) {
        return makeDisburseTransaction(loanId, transactionDate, transactionAmount, paymentDetail, noteText, txnExternalId, false);
    }

    @Transactional
    @Override
    public LoanTransaction makeDisburseTransaction(final Long loanId, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PaymentDetail paymentDetail, final String noteText, final ExternalId txnExternalId, final boolean isLoanToLoanTransfer) {
        final Loan loan = this.loanAccountAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        if (loan.isChargedOff() && DateUtils.isBefore(transactionDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }
        boolean isAccountTransfer = true;
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        final Money amount = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan, amount, paymentDetail, transactionDate, txnExternalId,
                loan.getTotalOverpaidAsMoney());

        // Subtract Previous loan outstanding balance from netDisbursalAmount
        loan.deductFromNetDisbursalAmount(transactionAmount);

        disbursementTransaction.updateLoan(loan);
        loan.addLoanTransaction(disbursementTransaction);
        saveLoanTransactionWithDataIntegrityViolationChecks(disbursementTransaction);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, disbursementTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, isLoanToLoanTransfer);
        return disbursementTransaction;
    }

    @Override
    public void reverseTransfer(final LoanTransaction loanTransaction) {
        if (loanTransaction.getLoan().isChargedOff()
                && DateUtils.isBefore(loanTransaction.getTransactionDate(), loanTransaction.getLoan().getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date",
                    "Loan transaction: " + loanTransaction.getId()
                            + " reversal is not allowed before or on the date when the loan got charged-off",
                    loanTransaction.getId());
        }
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loanTransaction.getLoan(), loanTransaction, "reversed");
        loanTransaction.reverse();
        saveLoanTransactionWithDataIntegrityViolationChecks(loanTransaction);
    }

    @Override
    public void setLoanDelinquencyTag(final Loan loan, final LocalDate transactionDate) {
        LoanScheduleDelinquencyData loanDelinquencyData = new LoanScheduleDelinquencyData(loan.getId(), transactionDate, null, loan);
        final List<LoanDelinquencyAction> savedDelinquencyList = delinquencyReadPlatformService
                .retrieveLoanDelinquencyActions(loan.getId());
        List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                .calculateEffectiveDelinquencyList(savedDelinquencyList);
        loanDelinquencyData = this.delinquencyWritePlatformService.calculateDelinquencyData(loanDelinquencyData, effectiveDelinquencyList);
        log.debug("Processing Loan {} with {} overdue days since date {}", loanDelinquencyData.getLoanId(),
                loanDelinquencyData.getOverdueDays(), loanDelinquencyData.getOverdueSinceDate());
        // Set or Unset the Delinquency Classification Tag
        if (loanDelinquencyData.getOverdueDays() > 0) {
            this.delinquencyWritePlatformService.applyDelinquencyTagToLoan(loanDelinquencyData, effectiveDelinquencyList);
        } else {
            this.delinquencyWritePlatformService.removeDelinquencyTagToLoan(loanDelinquencyData.getLoan());
        }
    }

    @Override
    public void setLoanDelinquencyTag(Loan loan, LocalDate transactionDate, List<LoanDelinquencyActionData> effectiveDelinquencyList) {
        LoanScheduleDelinquencyData loanDelinquencyData = new LoanScheduleDelinquencyData(loan.getId(), transactionDate, null, loan);
        loanDelinquencyData = this.delinquencyWritePlatformService.calculateDelinquencyData(loanDelinquencyData, effectiveDelinquencyList);
        log.debug("Processing Loan {} with {} overdue days since date {}", loanDelinquencyData.getLoanId(),
                loanDelinquencyData.getOverdueDays(), loanDelinquencyData.getOverdueSinceDate());
        // Set or Unset the Delinquency Classification Tag
        if (loanDelinquencyData.getOverdueDays() > 0) {
            this.delinquencyWritePlatformService.applyDelinquencyTagToLoan(loanDelinquencyData, effectiveDelinquencyList);
        } else {
            this.delinquencyWritePlatformService.removeDelinquencyTagToLoan(loanDelinquencyData.getLoan());
        }
    }

    private void updateLoanTransferTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
    }

    @Override
    public LoanTransaction creditBalanceRefund(final Loan loan, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final String noteText, final ExternalId externalId, PaymentDetail paymentDetail) {
        if (transactionDate.isAfter(DateUtils.getBusinessLocalDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.in.the.future",
                    "Loan: " + loan.getId() + ", Credit Balance Refund transaction cannot be created for the future.", loan.getId());
        }
        if (loan.isChargedOff() && DateUtils.isBefore(transactionDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }

        businessEventNotifierService.notifyPreBusinessEvent(new LoanCreditBalanceRefundPreBusinessEvent(loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction newCreditBalanceRefundTransaction = LoanTransaction.creditBalanceRefund(loan, loan.getOffice(), refundAmount,
                transactionDate, externalId, paymentDetail);

        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_CREDIT_BALANCE_REFUND);
        loanTransactionValidator.validateRefundDateIsAfterLastRepayment(loan, newCreditBalanceRefundTransaction.getTransactionDate());

        loanRefundService.creditBalanceRefund(loan, newCreditBalanceRefundTransaction, defaultLoanLifecycleStateMachine,
                existingTransactionIds, existingReversedTransactionIds);

        newCreditBalanceRefundTransaction = this.loanTransactionRepository.saveAndFlush(newCreditBalanceRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newCreditBalanceRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan,
                loan.repaymentScheduleDetail().isInterestRecalculationEnabled(), false);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanCreditBalanceRefundPostBusinessEvent(newCreditBalanceRefundTransaction));

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);

        return newCreditBalanceRefundTransaction;
    }

    @Override
    public LoanTransaction makeRefundForActiveLoan(Long accountId, CommandProcessingResultBuilder builderResult, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, String noteText, ExternalId txnExternalId) {
        final Loan loan = this.loanAccountAssembler.assembleFrom(accountId);
        checkClientOrGroupActive(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanRefundPreBusinessEvent(loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money refundAmount = Money.of(loan.getCurrency(), transactionAmount);
        if (loan.isChargedOff() && DateUtils.isBefore(transactionDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }
        final LoanTransaction newRefundTransaction = LoanTransaction.refundForActiveLoan(loan.getOffice(), refundAmount, paymentDetail,
                transactionDate, txnExternalId);
        loanTransactionValidator.validateRefundDateIsAfterLastRepayment(loan, newRefundTransaction.getTransactionDate());
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), transactionDate,
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_REFUND);
        loanTransactionValidator.validateRepaymentDateIsOnHoliday(newRefundTransaction.getTransactionDate(), allowTransactionsOnHoliday,
                holidays);
        loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(newRefundTransaction.getTransactionDate(), workingDays,
                allowTransactionsOnNonWorkingDay);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_REFUND,
                newRefundTransaction.getTransactionDate());
        loanRefundService.makeRefundForActiveLoan(loan, newRefundTransaction, defaultLoanLifecycleStateMachine, existingTransactionIds,
                existingReversedTransactionIds);

        this.loanTransactionRepository.saveAndFlush(newRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan,
                loan.repaymentScheduleDetail().isInterestRecalculationEnabled(), false);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRefundPostBusinessEvent(newRefundTransaction));

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);

        builderResult.withEntityId(newRefundTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withGroupId(loan.getGroupId());

        return newRefundTransaction;
    }

    @Override
    public LoanTransaction foreCloseLoan(Loan loan, final LocalDate foreClosureDate, final String noteText, final ExternalId externalId,
            Map<String, Object> changes) {

        if (loan.isChargedOff() && DateUtils.isBefore(foreClosureDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanForeClosurePreBusinessEvent(loan));
        MonetaryCurrency currency = loan.getCurrency();
        List<LoanTransaction> newTransactions = new ArrayList<>();

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
        final ScheduleGeneratorDTO scheduleGeneratorDTO = null;
        final LoanRepaymentScheduleInstallment foreCloseDetail = loan.fetchLoanForeclosureDetail(foreClosureDate);

        loanAccrualsProcessingService.processAccrualsOnLoanForeClosure(loan, foreClosureDate, newTransactions);

        Money interestPayable = foreCloseDetail.getInterestCharged(currency);
        Money feePayable = foreCloseDetail.getFeeChargesCharged(currency);
        Money penaltyPayable = foreCloseDetail.getPenaltyChargesCharged(currency);
        Money payPrincipal = foreCloseDetail.getPrincipal(currency);
        updateInstallmentsPostDate(loan, foreClosureDate);

        LoanTransaction payment = null;

        if (payPrincipal.plus(interestPayable).plus(feePayable).plus(penaltyPayable).isGreaterThanZero()) {
            final PaymentDetail paymentDetail = null;
            payment = LoanTransaction.repayment(loan.getOffice(), payPrincipal.plus(interestPayable).plus(feePayable).plus(penaltyPayable),
                    paymentDetail, foreClosureDate, externalId);
            payment.updateLoan(loan);
            newTransactions.add(payment);
        }

        List<Long> transactionIds = new ArrayList<>();
        if (payment != null) {
            loanForeclosureValidator.validateForForeclosure(loan, payment.getTransactionDate());
        }
        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_FORECLOSURE);
        final ChangedTransactionDetail changedTransactionDetail = handleForeClosureTransactions(loan, payment,
                defaultLoanLifecycleStateMachine, scheduleGeneratorDTO);

        loanAccrualsProcessingService.reprocessExistingAccruals(loan);
        if (loan.getLoanRepaymentScheduleDetail().isInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan);
        }

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a payment is made
         * before the latest payment recorded against the loan)
         ***/

        for (LoanTransaction newTransaction : newTransactions) {
            saveLoanTransactionWithDataIntegrityViolationChecks(newTransaction);
            transactionIds.add(newTransaction.getId());
        }
        changes.put("transactions", transactionIds);
        changes.put("eventAmount", payPrincipal.getAmount().negate());

        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                saveLoanTransactionWithDataIntegrityViolationChecks(mapEntry.getValue());
                updateLoanTransferTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
            // Trigger transaction replayed event
            replayedTransactionBusinessEventService.raiseTransactionReplayedEvents(changedTransactionDetail);
        }
        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanForeClosurePostBusinessEvent(payment));
        return payment;
    }

    @Override
    @Transactional
    public void disableStandingInstructionsLinkedToClosedLoan(Loan loan) {
        if ((loan != null) && (loan.getStatus() != null) && loan.getStatus().isClosed()) {
            final Integer standingInstructionStatus = StandingInstructionStatus.ACTIVE.getValue();
            final Collection<AccountTransferStandingInstruction> accountTransferStandingInstructions = this.standingInstructionRepository
                    .findByLoanAccountAndStatus(loan, standingInstructionStatus);

            if (!accountTransferStandingInstructions.isEmpty()) {
                for (AccountTransferStandingInstruction accountTransferStandingInstruction : accountTransferStandingInstructions) {
                    accountTransferStandingInstruction.updateStatus(StandingInstructionStatus.DISABLED.getValue());
                    this.standingInstructionRepository.save(accountTransferStandingInstruction);
                }
            }
        }
    }

    @Override
    public void updateAndSaveLoanCollateralTransactionsForIndividualAccounts(Loan loan, LoanTransaction loanTransaction) {
        if (loan.getLoanType().isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                if (loanTransaction != null) {
                    loanCollateralManagement.setLoanTransactionData(loanTransaction);
                }
                ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();

                if (loan.getStatus().isClosed()) {
                    loanCollateralManagement.setIsReleased(true);
                    BigDecimal quantity = loanCollateralManagement.getQuantity();
                    clientCollateralManagement.updateQuantity(clientCollateralManagement.getQuantity().add(quantity));
                    loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
                }
            }
            this.loanCollateralManagementRepository.saveAll(loanCollateralManagements);
        }
    }

    @Override
    public Pair<LoanTransaction, LoanTransaction> makeRefund(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO,
            final LoanTransactionType loanTransactionType, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PaymentDetail paymentDetail, final ExternalId txnExternalId) {
        // Pre-processing business event
        switch (loanTransactionType) {
            case MERCHANT_ISSUED_REFUND ->
                businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionMerchantIssuedRefundPreBusinessEvent(loan));
            case PAYOUT_REFUND ->
                businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionPayoutRefundPreBusinessEvent(loan));
            default ->
                throw new UnsupportedOperationException(String.format("Not configured loan transaction type: %s!", loanTransactionType));
        }
        LoanTransaction refundTransaction = LoanTransaction.refund(loan, loanTransactionType, transactionAmount, paymentDetail,
                transactionDate, txnExternalId);

        final boolean isTransactionChronologicallyLatest = loan.isChronologicallyLatestRepaymentOrWaiver(refundTransaction);

        boolean shouldCreateInterestRefundTransaction = loan.getLoanProductRelatedDetail().getSupportedInterestRefundTypes().stream()
                .map(LoanSupportedInterestRefundTypes::getTransactionType)
                .anyMatch(transactionType -> transactionType.equals(loanTransactionType));
        LoanTransaction interestRefundTransaction = null;

        if (shouldCreateInterestRefundTransaction) {
            interestRefundTransaction = createInterestRefundLoanTransaction(loan, refundTransaction);
            if (interestRefundTransaction != null) {
                interestRefundTransaction.getLoanTransactionRelations().add(LoanTransactionRelation
                        .linkToTransaction(interestRefundTransaction, refundTransaction, LoanTransactionRelationTypeEnum.RELATED));
            }
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = transactionProcessorFactory
                .determineProcessor(loan.getTransactionProcessingStrategyCode());

        final LoanRepaymentScheduleInstallment currentInstallment = loan.fetchLoanRepaymentScheduleInstallmentByDueDate(transactionDate);

        boolean reprocess = loan.getLoanRepaymentScheduleDetail().isInterestRecalculationEnabled() //
                || loan.isForeclosure() //
                || !isTransactionChronologicallyLatest //
                || !DateUtils.isEqualBusinessDate(transactionDate) //
                || currentInstallment == null //
                || !currentInstallment.getTotalOutstanding(loan.getCurrency()).isEqualTo(refundTransaction.getAmount(loan.getCurrency())); //

        if (!reprocess) {
            loan.getLoanTransactions().add(refundTransaction);
            loanRepaymentScheduleTransactionProcessor.processLatestTransaction(refundTransaction,
                    new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                            new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));
            if (interestRefundTransaction != null) {
                loan.addLoanTransaction(interestRefundTransaction);
                loanRepaymentScheduleTransactionProcessor.processLatestTransaction(interestRefundTransaction,
                        new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                                new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));
            }
        } else {
            if (loan.getLoanRepaymentScheduleDetail().isInterestRecalculationEnabled()) {
                loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
            }
            loan.getLoanTransactions().add(refundTransaction);
            if (interestRefundTransaction != null) {
                loan.addLoanTransaction(interestRefundTransaction);
            }
            ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();

            // Store and flush newly created transaction to generate PK
            saveLoanTransactionWithDataIntegrityViolationChecks(refundTransaction);
            if (interestRefundTransaction != null) {
                saveLoanTransactionWithDataIntegrityViolationChecks(interestRefundTransaction);
            }
            /*
             * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for
             * the time being, not a major issue for now as this loop is entered only in edge cases (when a payment is
             * made before the latest payment recorded against the loan)
             */
            // Apply reprocessed transactions (if applicable)
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                saveLoanTransactionWithDataIntegrityViolationChecks(mapEntry.getValue());
                updateLoanTransferTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
            // Trigger transaction replayed event
            replayedTransactionBusinessEventService.raiseTransactionReplayedEvents(changedTransactionDetail);
        }

        loan.updateLoanSummaryDerivedFields();
        loan.doPostLoanTransactionChecks(transactionDate, defaultLoanLifecycleStateMachine);

        switch (loanTransactionType) {
            case MERCHANT_ISSUED_REFUND -> businessEventNotifierService
                    .notifyPostBusinessEvent(new LoanTransactionMerchantIssuedRefundPostBusinessEvent(refundTransaction));
            case PAYOUT_REFUND ->
                businessEventNotifierService.notifyPostBusinessEvent(new LoanTransactionPayoutRefundPostBusinessEvent(refundTransaction));
            default ->
                throw new UnsupportedOperationException(String.format("Not configured loan transaction type: %s!", loanTransactionType));
        }

        if (interestRefundTransaction != null) {
            businessEventNotifierService
                    .notifyPostBusinessEvent(new LoanTransactionInterestRefundPostBusinessEvent(interestRefundTransaction));
        }
        return Pair.of(refundTransaction, interestRefundTransaction);
    }

    @Override
    public void updateAndSavePostDatedChecksForIndividualAccount(final Loan loan, final LoanTransaction transaction) {
        if (loan.getLoanType().isIndividualAccount()) {
            // Mark Post Dated Check as paid.
            final Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = transaction
                    .getLoanTransactionToRepaymentScheduleMappings();

            if (loanTransactionToRepaymentScheduleMappings != null) {
                for (LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping : loanTransactionToRepaymentScheduleMappings) {
                    LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loanTransactionToRepaymentScheduleMapping
                            .getLoanRepaymentScheduleInstallment();
                    if (loanRepaymentScheduleInstallment != null) {
                        final boolean isPaid = loanRepaymentScheduleInstallment.isNotFullyPaidOff();
                        PostDatedChecks postDatedChecks = this.postDatedChecksRepository
                                .getPendingPostDatedCheck(loanRepaymentScheduleInstallment);

                        if (postDatedChecks != null) {
                            if (!isPaid) {
                                postDatedChecks.setStatus(PostDatedChecksStatus.POST_DATED_CHECKS_PAID);
                            } else {
                                postDatedChecks.setStatus(PostDatedChecksStatus.POST_DATED_CHECKS_PENDING);
                            }
                            this.postDatedChecksRepository.saveAndFlush(postDatedChecks);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public LoanTransaction applyInterestRefund(final Loan loan, final LoanRefundRequestData loanRefundRequest) {
        final PaymentDetail paymentDetail = null;
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final BigDecimal refundAmount = loanRefundRequest.getTotalAmount();

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

        final LoanTransaction interestRefundTransaction = LoanTransaction.interestRefund(loan, loan.getOffice(), refundAmount,
                loanRefundRequest.getPrincipal(), loanRefundRequest.getInterest(), loanRefundRequest.getFeeCharges(),
                loanRefundRequest.getPenaltyCharges(), paymentDetail, transactionDate, externalIdFactory.create());
        interestRefundTransaction.updateLoan(loan);
        saveLoanTransactionWithDataIntegrityViolationChecks(interestRefundTransaction);
        loan.addLoanTransaction(interestRefundTransaction);
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);

        return interestRefundTransaction;
    }

    private void updateInstallmentsPostDate(final Loan loan, final LocalDate transactionDate) {
        final List<LoanRepaymentScheduleInstallment> newInstallments = new ArrayList<>(loan.getRepaymentScheduleInstallments());
        final MonetaryCurrency currency = loan.getCurrency();
        Money totalPrincipal = Money.zero(currency);
        final Money[] balances = loan.retriveIncomeForOverlappingPeriod(transactionDate);
        boolean isInterestComponent = true;
        for (final LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (!DateUtils.isAfter(transactionDate, installment.getDueDate())) {
                totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
                newInstallments.remove(installment);
                if (DateUtils.isEqual(transactionDate, installment.getDueDate())) {
                    isInterestComponent = false;
                }
            }

        }

        for (LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
            if (loanDisbursementDetails.actualDisbursementDate() == null) {
                totalPrincipal = Money.of(currency, totalPrincipal.getAmount().subtract(loanDisbursementDetails.principal()));
            }
        }

        LocalDate installmentStartDate = loan.getDisbursementDate();

        if (!newInstallments.isEmpty()) {
            installmentStartDate = newInstallments.get(newInstallments.size() - 1).getDueDate();
        }

        int installmentNumber = newInstallments.size();

        if (!isInterestComponent) {
            installmentNumber++;
        }

        final LoanRepaymentScheduleInstallment newInstallment = new LoanRepaymentScheduleInstallment(null, newInstallments.size() + 1,
                installmentStartDate, transactionDate, totalPrincipal.getAmount(), balances[0].getAmount(), balances[1].getAmount(),
                balances[2].getAmount(), isInterestComponent, null);
        newInstallment.updateInstallmentNumber(newInstallments.size() + 1);
        newInstallments.add(newInstallment);
        loan.updateLoanScheduleOnForeclosure(newInstallments);

        final Set<LoanCharge> charges = loan.getActiveCharges();
        final int penaltyWaitPeriod = 0;
        for (LoanCharge loanCharge : charges) {
            if (DateUtils.isAfter(loanCharge.getDueLocalDate(), transactionDate)) {
                loanCharge.setActive(false);
            } else if (loanCharge.getDueLocalDate() == null) {
                loanChargeService.recalculateLoanCharge(loan, loanCharge, penaltyWaitPeriod);
                loanCharge.updateWaivedAmount(currency);
            }
        }

        for (LoanTransaction loanTransaction : loan.getLoanTransactions()) {
            if (loanTransaction.isChargesWaiver()) {
                for (LoanChargePaidBy chargePaidBy : loanTransaction.getLoanChargesPaid()) {
                    if ((chargePaidBy.getLoanCharge().isDueDateCharge()
                            && DateUtils.isBefore(transactionDate, chargePaidBy.getLoanCharge().getDueLocalDate()))
                            || (chargePaidBy.getLoanCharge().isInstalmentFee() && chargePaidBy.getInstallmentNumber() != null
                                    && chargePaidBy.getInstallmentNumber() > installmentNumber)) {
                        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loanTransaction.getLoan(),
                                loanTransaction, "reversed");
                        loanTransaction.reverse();
                    }
                }

            }
        }
    }

    @SuppressWarnings("null")
    private ChangedTransactionDetail makeRepayment(final Loan loan, final LoanTransaction repaymentTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loan, repaymentTransaction, "created");
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

        return loanDownPaymentHandlerService.handleRepaymentOrRecoveryOrWaiverTransaction(loan, repaymentTransaction,
                loanLifecycleStateMachine, null, scheduleGeneratorDTO);
    }

    private ChangedTransactionDetail handleForeClosureTransactions(final Loan loan, final LoanTransaction repaymentTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        loan.setLoanSubStatus(LoanSubStatus.FORECLOSED.getValue());
        return loanDownPaymentHandlerService.handleRepaymentOrRecoveryOrWaiverTransaction(loan, repaymentTransaction,
                loanLifecycleStateMachine, null, scheduleGeneratorDTO);
    }
}
