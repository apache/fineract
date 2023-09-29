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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.MultiException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
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
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformService;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventService;
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
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanAccrualPlatformService loanAccrualPlatformService;
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
    public void updateLoanCollateralTransaction(Set<LoanCollateralManagement> loanCollateralManagementSet) {
        this.loanCollateralManagementRepository.saveAll(loanCollateralManagementSet);
    }

    @Transactional
    @Override
    public void updateLoanCollateralStatus(Set<LoanCollateralManagement> loanCollateralManagementSet, boolean isReleased) {
        for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagementSet) {
            loanCollateralManagement.setIsReleased(isReleased);
        }
        this.loanCollateralManagementRepository.saveAll(loanCollateralManagementSet);
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

        final ChangedTransactionDetail changedTransactionDetail = loan.makeRepayment(newRepaymentTransaction,
                defaultLoanLifecycleStateMachine, existingTransactionIds, existingReversedTransactionIds, isRecoveryRepayment,
                scheduleGeneratorDTO, isHolidayValidationDone);

        saveLoanTransactionWithDataIntegrityViolationChecks(newRepaymentTransaction);

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a payment is made
         * before the latest payment recorded against the loan)
         ***/
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                saveLoanTransactionWithDataIntegrityViolationChecks(mapEntry.getValue());
                updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
            // Trigger transaction replayed event
            replayedTransactionBusinessEventService.raiseTransactionReplayedEvents(changedTransactionDetail);
        }
        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRepaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, isLoanToLoanTransfer);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        recalculateAccruals(loan);

        setLoanDelinquencyTag(loan, transactionDate);

        if (!repaymentTransactionType.isChargeRefund()) {
            LoanTransactionBusinessEvent transactionRepaymentEvent = getTransactionRepaymentTypeBusinessEvent(repaymentTransactionType,
                    isRecoveryRepayment, newRepaymentTransaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
            businessEventNotifierService.notifyPostBusinessEvent(transactionRepaymentEvent);
        }

        // disable all active standing orders linked to this loan if status
        // changes to closed
        disableStandingInstructionsLinkedToClosedLoan(loan);

        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
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
        } else if (repaymentTransactionType.isChargeRefund()) {
            repaymentEvent = new LoanChargePaymentPreBusinessEvent(loan);
        } else if (isRecoveryRepayment) {
            repaymentEvent = new LoanTransactionRecoveryPaymentPreBusinessEvent(loan);
        } else if (repaymentTransactionType.isDownPayment()) {
            repaymentEvent = new LoanTransactionDownPaymentPreBusinessEvent(loan);
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
        } else if (repaymentTransactionType.isChargeRefund()) {
            repaymentEvent = new LoanChargePaymentPostBusinessEvent(transaction);
        } else if (isRecoveryRepayment) {
            repaymentEvent = new LoanTransactionRecoveryPaymentPostBusinessEvent(transaction);
        } else if (repaymentTransactionType.isDownPayment()) {
            repaymentEvent = new LoanTransactionDownPaymentPostBusinessEvent(transaction);
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
        if (loan.isChargedOff() && transactionDate.isBefore(loan.getChargedOffOnDate())) {
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

            loan.makeChargePayment(chargeId, defaultLoanLifecycleStateMachine, existingTransactionIds, existingReversedTransactionIds,
                    holidayDetailDTO, newPaymentTransaction, installmentNumber);
        }
        saveLoanTransactionWithDataIntegrityViolationChecks(newPaymentTransaction);
        saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newPaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);

        recalculateAccruals(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargePaymentPostBusinessEvent(newPaymentTransaction));
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
        if (loan.isChargedOff() && transactionDate.isBefore(loan.getChargedOffOnDate())) {
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

        loan.makeRefund(newRefundTransaction, defaultLoanLifecycleStateMachine, existingTransactionIds, existingReversedTransactionIds,
                allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

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
        if (loan.isChargedOff() && transactionDate.isBefore(loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }
        boolean isAccountTransfer = true;
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        final Money amount = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), amount, paymentDetail, transactionDate,
                txnExternalId);

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
                && loanTransaction.getTransactionDate().isBefore(loanTransaction.getLoan().getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date",
                    "Loan transaction: " + loanTransaction.getId()
                            + " reversal is not allowed before or on the date when the loan got charged-off",
                    loanTransaction.getId());
        }
        loanTransaction.reverse();
        saveLoanTransactionWithDataIntegrityViolationChecks(loanTransaction);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService
     * #recalculateAccruals(org.apache.fineract.portfolio.loanaccount.domain. Loan)
     */
    @Override
    public void recalculateAccruals(Loan loan) {
        recalculateAccruals(loan, loan.repaymentScheduleDetail().isInterestRecalculationEnabled());
    }

    @Override
    public void setLoanDelinquencyTag(final Loan loan, final LocalDate transactionDate) {
        LoanScheduleDelinquencyData loanDelinquencyData = new LoanScheduleDelinquencyData(loan.getId(), transactionDate, null, loan);
        loanDelinquencyData = this.delinquencyWritePlatformService.calculateDelinquencyData(loanDelinquencyData);
        log.debug("Processing Loan {} with {} overdue days since date {}", loanDelinquencyData.getLoanId(),
                loanDelinquencyData.getOverdueDays(), loanDelinquencyData.getOverdueSinceDate());
        // Set or Unset the Delinquency Classification Tag
        if (loanDelinquencyData.getOverdueDays() > 0) {
            this.delinquencyWritePlatformService.applyDelinquencyTagToLoan(loanDelinquencyData);
        } else {
            this.delinquencyWritePlatformService.removeDelinquencyTagToLoan(loanDelinquencyData.getLoan());
        }
    }

    @Override
    public void recalculateAccruals(Loan loan, boolean isInterestCalculationHappened) {
        LocalDate accruedTill = loan.getAccruedTill();
        if (!loan.isPeriodicAccrualAccountingEnabledOnLoanProduct() || !isInterestCalculationHappened || accruedTill == null || loan.isNpa()
                || !loan.getStatus().isActive() || loan.isChargedOff()) {
            return;
        }

        boolean isOrganisationDateEnabled = this.configurationDomainService.isOrganisationstartDateEnabled();
        LocalDate organisationStartDate = DateUtils.getBusinessLocalDate();
        if (isOrganisationDateEnabled) {
            organisationStartDate = this.configurationDomainService.retrieveOrganisationStartDate();
        }
        Collection<LoanScheduleAccrualData> loanScheduleAccrualList = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        Long loanId = loan.getId();
        Long officeId = loan.getOfficeId();
        LocalDate accrualStartDate = null;
        PeriodFrequencyType repaymentFrequency = loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        Integer repayEvery = loan.repaymentScheduleDetail().getRepayEvery();
        LocalDate interestCalculatedFrom = loan.getInterestChargedFromDate();
        Long loanProductId = loan.productId();
        MonetaryCurrency currency = loan.getCurrency();
        ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        CurrencyData currencyData = applicationCurrency.toData();
        Set<LoanCharge> loanCharges = loan.getActiveCharges();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.getDueDate().isAfter(loan.getMaturityDate())) {
                accruedTill = DateUtils.getBusinessLocalDate();
            }
            if (!isOrganisationDateEnabled || organisationStartDate.isBefore(installment.getDueDate())) {
                generateLoanScheduleAccrualData(accruedTill, loanScheduleAccrualList, loanId, officeId, accrualStartDate,
                        repaymentFrequency, repayEvery, interestCalculatedFrom, loanProductId, currency, currencyData, loanCharges,
                        installment);
            }
        }

        if (!loanScheduleAccrualList.isEmpty()) {
            try {
                this.loanAccrualPlatformService.addPeriodicAccruals(accruedTill, loanScheduleAccrualList);
            } catch (MultiException e) {
                String globalisationMessageCode = "error.msg.accrual.exception";
                throw new GeneralPlatformDomainRuleException(globalisationMessageCode, e.getMessage(), e);
            }
        }

    }

    private void generateLoanScheduleAccrualData(final LocalDate accruedTill,
            final Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas, final Long loanId, Long officeId,
            final LocalDate accrualStartDate, final PeriodFrequencyType repaymentFrequency, final Integer repayEvery,
            final LocalDate interestCalculatedFrom, final Long loanProductId, final MonetaryCurrency currency,
            final CurrencyData currencyData, final Set<LoanCharge> loanCharges, final LoanRepaymentScheduleInstallment installment) {

        if (!accruedTill.isBefore(installment.getDueDate())
                || (accruedTill.isAfter(installment.getFromDate()) && !accruedTill.isAfter(installment.getDueDate()))) {
            BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
            BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
            LocalDate chargesTillDate = installment.getDueDate();
            if (!accruedTill.isAfter(installment.getDueDate())) {
                chargesTillDate = accruedTill;
            }

            for (final LoanCharge loanCharge : loanCharges) {
                boolean isDue = installment.isFirstPeriod()
                        ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(installment.getFromDate(), chargesTillDate)
                        : loanCharge.isDueForCollectionFromAndUpToAndIncluding(installment.getFromDate(), chargesTillDate);
                if (isDue) {
                    if (loanCharge.isFeeCharge()) {
                        dueDateFeeIncome = dueDateFeeIncome.add(loanCharge.amount());
                    } else if (loanCharge.isPenaltyCharge()) {
                        dueDatePenaltyIncome = dueDatePenaltyIncome.add(loanCharge.amount());
                    }
                }
            }
            LoanScheduleAccrualData accrualData = new LoanScheduleAccrualData(loanId, officeId, installment.getInstallmentNumber(),
                    accrualStartDate, repaymentFrequency, repayEvery, installment.getDueDate(), installment.getFromDate(),
                    installment.getId(), loanProductId, installment.getInterestCharged(currency).getAmount(),
                    installment.getFeeChargesCharged(currency).getAmount(), installment.getPenaltyChargesCharged(currency).getAmount(),
                    installment.getInterestAccrued(currency).getAmount(), installment.getFeeAccrued(currency).getAmount(),
                    installment.getPenaltyAccrued(currency).getAmount(), currencyData, interestCalculatedFrom,
                    installment.getInterestWaived(currency).getAmount());
            loanScheduleAccrualDatas.add(accrualData);

        }
    }

    private void updateLoanTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
    }

    @Override
    public LoanTransaction creditBalanceRefund(final Loan loan, final LocalDate transactionDate, final BigDecimal transactionAmount,
            final String noteText, final ExternalId externalId, PaymentDetail paymentDetail) {
        if (loan.isChargedOff() && transactionDate.isBefore(loan.getChargedOffOnDate())) {
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

        loan.creditBalanceRefund(newCreditBalanceRefundTransaction, defaultLoanLifecycleStateMachine, existingTransactionIds,
                existingReversedTransactionIds);

        newCreditBalanceRefundTransaction = this.loanTransactionRepository.saveAndFlush(newCreditBalanceRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newCreditBalanceRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        recalculateAccruals(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanCreditBalanceRefundPostBusinessEvent(newCreditBalanceRefundTransaction));

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
        if (loan.isChargedOff() && transactionDate.isBefore(loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loan.getId()
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loan.getId());
        }
        final LoanTransaction newRefundTransaction = LoanTransaction.refundForActiveLoan(loan.getOffice(), refundAmount, paymentDetail,
                transactionDate, txnExternalId);
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), transactionDate,
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        loan.makeRefundForActiveLoan(newRefundTransaction, defaultLoanLifecycleStateMachine, existingTransactionIds,
                existingReversedTransactionIds, allowTransactionsOnHoliday, holidays, workingDays, allowTransactionsOnNonWorkingDay);

        this.loanTransactionRepository.saveAndFlush(newRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        recalculateAccruals(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRefundPostBusinessEvent(newRefundTransaction));

        builderResult.withEntityId(newRefundTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withGroupId(loan.getGroupId());

        return newRefundTransaction;
    }

    @Override
    public LoanTransaction foreCloseLoan(Loan loan, final LocalDate foreClosureDate, final String noteText, final ExternalId externalId,
            Map<String, Object> changes) {
        if (loan.isChargedOff() && foreClosureDate.isBefore(loan.getChargedOffOnDate())) {
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
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                && (loan.getAccruedTill() == null || !foreClosureDate.isEqual(loan.getAccruedTill()))) {
            loan.reverseAccrualsAfter(foreClosureDate);
            Money[] accruedReceivables = loan.getReceivableIncome(foreClosureDate);
            Money interestPortion = foreCloseDetail.getInterestCharged(currency).minus(accruedReceivables[0]);
            Money feePortion = foreCloseDetail.getFeeChargesCharged(currency).minus(accruedReceivables[1]);
            Money penaltyPortion = foreCloseDetail.getPenaltyChargesCharged(currency).minus(accruedReceivables[2]);
            Money total = interestPortion.plus(feePortion).plus(penaltyPortion);
            if (total.isGreaterThanZero()) {
                ExternalId accrualExternalId = externalIdFactory.create();
                LoanTransaction accrualTransaction = LoanTransaction.accrueTransaction(loan, loan.getOffice(), foreClosureDate,
                        total.getAmount(), interestPortion.getAmount(), feePortion.getAmount(), penaltyPortion.getAmount(),
                        accrualExternalId);
                LocalDate fromDate = loan.getDisbursementDate();
                if (loan.getAccruedTill() != null) {
                    fromDate = loan.getAccruedTill();
                }
                newTransactions.add(accrualTransaction);
                loan.addLoanTransaction(accrualTransaction);
                Set<LoanChargePaidBy> accrualCharges = accrualTransaction.getLoanChargesPaid();
                for (LoanCharge loanCharge : loan.getActiveCharges()) {
                    boolean isDue = fromDate.isEqual(loan.getDisbursementDate())
                            ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(fromDate, foreClosureDate)
                            : loanCharge.isDueForCollectionFromAndUpToAndIncluding(fromDate, foreClosureDate);
                    if (loanCharge.isActive() && !loanCharge.isPaid() && (isDue || loanCharge.isInstalmentFee())) {
                        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction, loanCharge,
                                loanCharge.getAmountOutstanding(currency).getAmount(), null);
                        accrualCharges.add(loanChargePaidBy);
                    }
                }
            }
        }

        Money interestPayable = foreCloseDetail.getInterestCharged(currency);
        Money feePayable = foreCloseDetail.getFeeChargesCharged(currency);
        Money penaltyPayable = foreCloseDetail.getPenaltyChargesCharged(currency);
        Money payPrincipal = foreCloseDetail.getPrincipal(currency);
        loan.updateInstallmentsPostDate(foreClosureDate);

        LoanTransaction payment = null;

        if (payPrincipal.plus(interestPayable).plus(feePayable).plus(penaltyPayable).isGreaterThanZero()) {
            final PaymentDetail paymentDetail = null;
            payment = LoanTransaction.repayment(loan.getOffice(), payPrincipal.plus(interestPayable).plus(feePayable).plus(penaltyPayable),
                    paymentDetail, foreClosureDate, externalId);
            payment.updateLoan(loan);
            newTransactions.add(payment);
        }

        List<Long> transactionIds = new ArrayList<>();
        final ChangedTransactionDetail changedTransactionDetail = loan.handleForeClosureTransactions(payment,
                defaultLoanLifecycleStateMachine, scheduleGeneratorDTO);

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
                updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
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
    public void applyFinalIncomeAccrualTransaction(Loan loan) {
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                // to avoid collision with processIncomeAccrualTransactionOnLoanClosure()
                && !(loan.getLoanInterestRecalculationDetails() != null
                        && loan.getLoanInterestRecalculationDetails().isCompoundingToBePostedAsTransaction())
                && !loan.isNpa() && !loan.isChargedOff()) {

            MonetaryCurrency currency = loan.getCurrency();
            Money interestPortion = Money.zero(currency);
            Money feePortion = Money.zero(currency);
            Money penaltyPortion = Money.zero(currency);

            for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loan.getRepaymentScheduleInstallments()) {
                interestPortion = interestPortion.add(loanRepaymentScheduleInstallment.getInterestCharged(currency))
                        .minus(loanRepaymentScheduleInstallment.getInterestAccrued(currency))
                        .minus(loanRepaymentScheduleInstallment.getInterestWaived(currency));
                feePortion = feePortion.add(loanRepaymentScheduleInstallment.getFeeChargesCharged(currency))
                        .minus(loanRepaymentScheduleInstallment.getFeeAccrued(currency))
                        .minus(loanRepaymentScheduleInstallment.getFeeChargesWaived(currency));
                penaltyPortion = penaltyPortion.add(loanRepaymentScheduleInstallment.getPenaltyChargesCharged(currency))
                        .minus(loanRepaymentScheduleInstallment.getPenaltyAccrued(currency))
                        .minus(loanRepaymentScheduleInstallment.getPenaltyChargesWaived(currency));
            }
            Money total = interestPortion.plus(feePortion).plus(penaltyPortion);

            if (total.isGreaterThanZero()) {
                ExternalId externalId = externalIdFactory.create();

                LocalDate accrualTransactionDate = getFinalAccrualTransactionDate(loan);

                LoanTransaction accrualTransaction = LoanTransaction.accrueTransaction(loan, loan.getOffice(), accrualTransactionDate,
                        total.getAmount(), interestPortion.getAmount(), feePortion.getAmount(), penaltyPortion.getAmount(), externalId);

                Set<LoanChargePaidBy> accrualCharges = accrualTransaction.getLoanChargesPaid();

                Map<Long, Money> accrualDetails = loan.getActiveCharges().stream()
                        .collect(Collectors.toMap(LoanCharge::getId, v -> Money.zero(currency)));

                loan.getLoanTransactions(LoanTransaction::isAccrual).forEach(transaction -> {
                    transaction.getLoanChargesPaid().forEach(loanChargePaid -> {
                        accrualDetails.computeIfPresent(loanChargePaid.getLoanCharge().getId(),
                                (mappedKey, mappedValue) -> mappedValue.add(Money.of(currency, loanChargePaid.getAmount())));
                    });
                });

                loan.getActiveCharges().forEach(loanCharge -> {
                    Money amount = loanCharge.getAmount(currency).minus(loanCharge.getAmountWaived(currency));
                    if (!loanCharge.isInstalmentFee() && loanCharge.isActive()
                            && accrualDetails.get(loanCharge.getId()).isLessThan(amount)) {
                        Money amountToBeAccrued = amount.minus(accrualDetails.get(loanCharge.getId()));
                        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction, loanCharge,
                                amountToBeAccrued.getAmount(), null);
                        accrualCharges.add(loanChargePaidBy);
                    }
                });

                for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loan.getRepaymentScheduleInstallments()) {
                    for (LoanInstallmentCharge installmentCharge : loanRepaymentScheduleInstallment.getInstallmentCharges()) {
                        if (installmentCharge.getLoanCharge().isActive()) {
                            Money notWaivedAmount = installmentCharge.getAmount(currency)
                                    .minus(installmentCharge.getAmountWaived(currency));
                            if (notWaivedAmount.isGreaterThanZero()) {
                                Money amountToBeAccrued = notWaivedAmount
                                        .minus(accrualDetails.get(installmentCharge.getLoanCharge().getId()));
                                if (amountToBeAccrued.isGreaterThanZero()) {
                                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction,
                                            installmentCharge.getLoanCharge(), amountToBeAccrued.getAmount(),
                                            installmentCharge.getInstallment().getInstallmentNumber());
                                    accrualCharges.add(loanChargePaidBy);
                                    accrualDetails.computeIfPresent(installmentCharge.getLoanCharge().getId(),
                                            (mappedKey, mappedValue) -> mappedValue.add(amountToBeAccrued));
                                }
                                accrualDetails.computeIfPresent(installmentCharge.getLoanCharge().getId(),
                                        (mappedKey, mappedValue) -> MathUtil
                                                .negativeToZero(mappedValue.minus(Money.of(currency, installmentCharge.getAmount()))));
                            }
                        }
                    }
                }
                saveLoanTransactionWithDataIntegrityViolationChecks(accrualTransaction);
                loan.addLoanTransaction(accrualTransaction);
                businessEventNotifierService.notifyPostBusinessEvent(new LoanAccrualTransactionCreatedBusinessEvent(accrualTransaction));

                loan.getRepaymentScheduleInstallments().forEach(installment -> {
                    installment.updateAccrualPortion(
                            installment.getInterestCharged(currency).minus(installment.getInterestWaived(currency)),
                            installment.getFeeChargesCharged(currency).minus(installment.getFeeChargesWaived(currency)),
                            installment.getPenaltyChargesCharged(currency).minus(installment.getPenaltyChargesWaived(currency)));
                });
            }
        }
    }

    private LocalDate getFinalAccrualTransactionDate(Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }

}
