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
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
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
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
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
import org.apache.fineract.portfolio.loanaccount.data.AccountingBridgeDataDTO;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanRefundRequestData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanAccountingBridgeMapper;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanForeclosureValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.InterestRefundService;
import org.apache.fineract.portfolio.loanaccount.service.InterestRefundServiceDelegate;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeService;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentHandlerService;
import org.apache.fineract.portfolio.loanaccount.service.LoanRefundService;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanSupportedInterestRefundTypes;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.data.PostDatedChecksStatus;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
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
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanUtilService loanUtilService;
    private final StandingInstructionRepository standingInstructionRepository;
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final LoanCollateralManagementRepository loanCollateralManagementRepository;
    private final DelinquencyWritePlatformService delinquencyWritePlatformService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;
    private final LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final InterestRefundServiceDelegate interestRefundServiceDelegate;
    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanForeclosureValidator loanForeclosureValidator;
    private final LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    private final LoanChargeService loanChargeService;
    private final LoanScheduleService loanScheduleService;
    private final LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanRefundService loanRefundService;
    private final LoanAccountService loanAccountService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanAccountingBridgeMapper loanAccountingBridgeMapper;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionService loanTransactionService;
    private final LoanAccountDomainServiceJpaHelper loanAccountDomainServiceJpaHelper;

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
        Money newTotalInterest = interestRefundService.totalInterestByTransactions(null, loan.getId(),
                refundTransaction.getTransactionDate(), List.of(refundTransaction),
                loan.getLoanTransactions().stream().map(AbstractPersistableCustom::getId).toList());
        BigDecimal interestRefundAmount = totalInterest.minus(newTotalInterest).getAmount();

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
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }
        final ScheduleGeneratorDTO scheduleGeneratorDTOForPrepay = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom,
                transactionDate, holidayDetailDto);

        LocalDate recalculateTill = loanAccountDomainServiceJpaHelper.calculateRecalculateTillDate(loan, transactionDate,
                scheduleGeneratorDTOForPrepay, repaymentAmount);

        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom,
                recalculateTill, holidayDetailDto);

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
        makeRepayment(loan, newRepaymentTransaction, existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO);

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.reprocessExistingAccruals(loan);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan);
        }

        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newRepaymentTransaction);
        loan = loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRepaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                false);

        setLoanDelinquencyTag(loan, transactionDate);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, isLoanToLoanTransfer);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        if (!repaymentTransactionType.isChargeRefund()) {
            final LoanTransactionBusinessEvent transactionRepaymentEvent = getTransactionRepaymentTypeBusinessEvent(
                    repaymentTransactionType, isRecoveryRepayment, newRepaymentTransaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
            businessEventNotifierService.notifyPostBusinessEvent(transactionRepaymentEvent);
        }

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
            handlePayDisbursementTransaction(loan, chargeId, newPaymentTransaction, existingTransactionIds, existingReversedTransactionIds);
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
            loanChargeService.makeChargePayment(loan, chargeId, existingTransactionIds, existingReversedTransactionIds,
                    newPaymentTransaction, installmentNumber);
        }
        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newPaymentTransaction);
        loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newPaymentTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                false);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargePaymentPostBusinessEvent(newPaymentTransaction));
        return newPaymentTransaction;
    }

    private void handlePayDisbursementTransaction(final Loan loan, final Long chargeId, final LoanTransaction chargesPayment,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(loanTransactionRepository.findTransactionIdsByLoan(loan));
        existingReversedTransactionIds.addAll(loanTransactionRepository.findReversedTransactionIdsByLoan(loan));
        LoanCharge charge = null;
        for (final LoanCharge loanCharge : loan.getCharges()) {
            if (loanCharge.isActive() && chargeId.equals(loanCharge.getId())) {
                charge = loanCharge;
            }
        }
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, charge.amount(), null);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        final Money zero = Money.zero(loan.getCurrency());
        chargesPayment.updateComponents(zero, zero, charge.getAmount(loan.getCurrency()), zero);
        chargesPayment.updateLoan(loan);
        loan.addLoanTransaction(chargesPayment);
        loanBalanceService.updateLoanOutstandingBalances(loan);
        charge.markAsFullyPaid();
    }

    private void postJournalEntries(final Loan loanAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer) {
        postJournalEntries(loanAccount, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, false);
    }

    private void postJournalEntries(final Loan loanAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer, boolean isLoanToLoanTransfer) {

        final MonetaryCurrency currency = loanAccount.getCurrency();

        if (loanAccount.isChargedOff()) {
            final List<AccountingBridgeDataDTO> accountingBridgeDataList = loanAccountingBridgeMapper
                    .deriveAccountingBridgeDataForChargeOff(currency.getCode(), existingTransactionIds, existingReversedTransactionIds,
                            isAccountTransfer, loanAccount);
            if (isLoanToLoanTransfer) {
                accountingBridgeDataList.forEach(dto -> dto.getNewLoanTransactions().forEach(tx -> tx.setLoanToLoanTransfer(true)));
            }

            for (AccountingBridgeDataDTO accountingBridgeData : accountingBridgeDataList) {
                this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
            }
        } else {
            final AccountingBridgeDataDTO accountingBridgeData = loanAccountingBridgeMapper.deriveAccountingBridgeData(currency.getCode(),
                    existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, loanAccount);
            if (isLoanToLoanTransfer) {
                accountingBridgeData.getNewLoanTransactions().forEach(tx -> tx.setLoanToLoanTransfer(true));
            }

            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
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

        loanRefundService.makeRefund(loan, newRefundTransaction, existingTransactionIds, existingReversedTransactionIds);

        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newRefundTransaction);
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
        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(disbursementTransaction);
        loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

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
        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(loanTransaction);
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

        loanRefundService.creditBalanceRefund(loan, newCreditBalanceRefundTransaction, existingTransactionIds,
                existingReversedTransactionIds);

        newCreditBalanceRefundTransaction = this.loanTransactionRepository.saveAndFlush(newCreditBalanceRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newCreditBalanceRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                false);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
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
        loanRefundService.makeRefundForActiveLoan(loan, newRefundTransaction, existingTransactionIds, existingReversedTransactionIds);

        this.loanTransactionRepository.saveAndFlush(newRefundTransaction);

        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, newRefundTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                false);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRefundPostBusinessEvent(newRefundTransaction));

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
        existingTransactionIds.addAll(loanTransactionRepository.findTransactionIdsByLoan(loan));
        existingReversedTransactionIds.addAll(loanTransactionRepository.findReversedTransactionIdsByLoan(loan));
        final ScheduleGeneratorDTO scheduleGeneratorDTO = null;
        final LoanRepaymentScheduleInstallment foreCloseDetail = loanBalanceService.fetchLoanForeclosureDetail(loan, foreClosureDate);

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
        handleForeClosureTransactions(loan, payment, scheduleGeneratorDTO);

        loanAccrualsProcessingService.reprocessExistingAccruals(loan);
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan);
        }

        for (LoanTransaction newTransaction : newTransactions) {
            LoanTransaction savedNewTransaction = loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newTransaction);
            loan.addLoanTransaction(savedNewTransaction);
            transactionIds.add(savedNewTransaction.getId());
        }
        changes.put("transactions", transactionIds);
        changes.put("eventAmount", payPrincipal.getAmount().negate());

        loan = loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

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

        final boolean isTransactionChronologicallyLatest = loanTransactionService.isChronologicallyLatestRepaymentOrWaiver(loan,
                refundTransaction);

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

        final LoanRepaymentScheduleInstallment currentInstallment = loan.fetchLoanRepaymentScheduleInstallmentByDueDate(transactionDate);

        boolean processLatest = isTransactionChronologicallyLatest //
                && !loan.isForeclosure() //
                && !loan.hasChargesAffectedByBackdatedRepaymentLikeTransaction(refundTransaction) //
                && loanTransactionProcessingService.canProcessLatestTransactionOnly(loan, refundTransaction, currentInstallment); //
        if (processLatest) {
            loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), refundTransaction,
                    new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                            new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));
            loan.getLoanTransactions().add(refundTransaction);
            if (interestRefundTransaction != null) {
                loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(),
                        interestRefundTransaction, new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(),
                                loan.getActiveCharges(), new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));
                loan.addLoanTransaction(interestRefundTransaction);
            }
        } else {
            if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
            } else if (loan.isProgressiveSchedule() && ((loan.hasChargeOffTransaction() && loan.hasAccelerateChargeOffStrategy())
                    || loan.hasContractTerminationTransaction())) {
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            }
            loan.getLoanTransactions().add(refundTransaction);
            if (interestRefundTransaction != null) {
                loan.addLoanTransaction(interestRefundTransaction);
            }

            reprocessLoanTransactionsService.reprocessTransactions(loan);
        }

        // Store and flush newly created transaction to generate PK
        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(refundTransaction);
        if (interestRefundTransaction != null) {
            loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(interestRefundTransaction);
        }

        loanLifecycleStateMachine.determineAndTransition(loan, transactionDate);

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
        existingTransactionIds.addAll(loanTransactionRepository.findTransactionIdsByLoan(loan));
        existingReversedTransactionIds.addAll(loanTransactionRepository.findReversedTransactionIdsByLoan(loan));

        final LoanTransaction interestRefundTransaction = LoanTransaction.interestRefund(loan, loan.getOffice(), refundAmount,
                loanRefundRequest.getPrincipal(), loanRefundRequest.getInterest(), loanRefundRequest.getFeeCharges(),
                loanRefundRequest.getPenaltyCharges(), paymentDetail, transactionDate, externalIdFactory.create());
        interestRefundTransaction.updateLoan(loan);
        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(interestRefundTransaction);
        loan.addLoanTransaction(interestRefundTransaction);
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds, false);

        return interestRefundTransaction;
    }

    private void updateInstallmentsPostDate(final Loan loan, final LocalDate transactionDate) {
        final List<LoanRepaymentScheduleInstallment> newInstallments = new ArrayList<>(loan.getRepaymentScheduleInstallments());
        final MonetaryCurrency currency = loan.getCurrency();
        Money totalPrincipal = Money.zero(currency);
        final Money[] balances = loanBalanceService.retrieveIncomeForOverlappingPeriod(loan, transactionDate);
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
    private void makeRepayment(final Loan loan, final LoanTransaction repaymentTransaction, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loan, repaymentTransaction, "created");
        existingTransactionIds.addAll(loanTransactionRepository.findTransactionIdsByLoan(loan));
        existingReversedTransactionIds.addAll(loanTransactionRepository.findReversedTransactionIdsByLoan(loan));

        loanDownPaymentHandlerService.handleRepaymentOrRecoveryOrWaiverTransaction(loan, repaymentTransaction, null, scheduleGeneratorDTO);
    }

    private void handleForeClosureTransactions(final Loan loan, final LoanTransaction repaymentTransaction,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        loan.setLoanSubStatus(LoanSubStatus.FORECLOSED);
        loanDownPaymentHandlerService.handleRepaymentOrRecoveryOrWaiverTransaction(loan, repaymentTransaction, null, scheduleGeneratorDTO);
    }

}
