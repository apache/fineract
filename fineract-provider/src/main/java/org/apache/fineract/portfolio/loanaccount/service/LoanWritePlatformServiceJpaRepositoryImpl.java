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

import static org.apache.fineract.portfolio.loanaccount.domain.Loan.CLOSED_ON_DATE;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.EXTERNAL_ID;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.PARAM_STATUS;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.TRANSACTION_DATE;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.WRITTEN_OFF_ON_DATE;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.exceptions.AccountLockCannotBeOverruledException;
import org.apache.fineract.cob.service.AccountLockService;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAcceptTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanChargebackTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanCloseAsRescheduleBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanCloseBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanInitiateTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanInterestRecalculationBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanReassignOfficerBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRejectTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRemoveOfficerBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRescheduledDueCalendarChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanWithdrawTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeOffPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeOffPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestPaymentWaiverPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestPaymentWaiverPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoChargeOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoWrittenOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWaiveInterestBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWrittenOffPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWrittenOffPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.holiday.service.HolidayUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.exception.CalendarParameterUpdateNotSupportedException;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.command.LoanUpdateCommand;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSubStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidPaidInAdvanceAmountException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanForeclosureException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerUnassignmentException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanOfficerValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentFeeEventNotifier.FeeState;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentParameter;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanSupportedInterestRefundTypes;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.transfer.api.TransferApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccountDomainService loanAccountDomainService;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanTransactionRelationRepository loanTransactionRelationRepository;
    private final LoanAssembler loanAssembler;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final GuarantorDomainService guarantorDomainService;
    private final LoanUtilService loanUtilService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final GLIMAccountInfoRepository glimRepository;
    private final LoanRepository loanRepository;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final AccountLockService<LoanAccountLock> loanAccountLockService;
    private final ExternalIdFactory externalIdFactory;
    private final LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    private final LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    private final LoanTransactionAssembler loanTransactionAssembler;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanOfficerValidator loanOfficerValidator;
    private final LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    private final LoanDisbursementService loanDisbursementService;
    private final LoanScheduleService loanScheduleService;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanOfficerService loanOfficerService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanAccountService loanAccountService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanAdjustmentService loanAdjustmentService;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionService loanTransactionService;
    private final LoanDownPaymentFeeEventNotifier loanDownPaymentFeeEventNotifier;
    private final LoanWriteServiceHelper loanWriteServiceHelper;
    private final LoanDisbursementWritePlatformService loanDisbursementWritePlatformService;

    @Override
    public CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer) {
        return loanDisbursementWritePlatformService.disburseLoan(loanId, command, isAccountTransfer);
    }

    @Override
    public CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer, Boolean isWithoutAutoPayment) {
        return loanDisbursementWritePlatformService.disburseLoan(loanId, command, isAccountTransfer, isWithoutAutoPayment);
    }

    @Override
    public Map<String, Object> bulkLoanDisbursal(JsonCommand command, CollectionSheetBulkDisbursalCommand bulkDisbursalCommand,
            Boolean isAccountTransfer) {
        return loanDisbursementWritePlatformService.bulkLoanDisbursal(command, bulkDisbursalCommand, isAccountTransfer);
    }

    @Override
    public CommandProcessingResult undoLoanDisbursal(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.undoLoanDisbursal(loanId, command);
    }

    @Override
    public CommandProcessingResult updateDisbursementDateAndAmountForTranche(Long loanId, Long disbursementId, JsonCommand command) {
        return loanDisbursementWritePlatformService.updateDisbursementDateAndAmountForTranche(loanId, disbursementId, command);
    }

    @Override
    public CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.addAndDeleteLoanDisburseDetails(loanId, command);
    }

    @Override
    public CommandProcessingResult undoLastLoanDisbursal(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.undoLastLoanDisbursal(loanId, command);
    }

    @Override
    public CommandProcessingResult disburseGLIMLoan(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.disburseGLIMLoan(loanId, command);
    }

    @Override
    public CommandProcessingResult undoGLIMLoanDisbursal(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.undoGLIMLoanDisbursal(loanId, command);
    }

    private void saveLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            this.loanRepositoryWrapper.save(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase(Locale.ROOT).contains("external_id_unique")) {
                baseDataValidator.reset().parameter(LoanApiConstants.externalIdParameterName).failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }
    }

    @Transactional
    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public CommandProcessingResult makeGLIMLoanRepayment(final Long loanId, final JsonCommand command) {

        final Long parentLoanId = loanId;

        glimRepository.findById(parentLoanId).orElseThrow();

        JsonArray repayments = command.arrayOfParameterNamed("formDataArray");
        JsonCommand childCommand;
        CommandProcessingResult result = null;
        JsonObject jsonObject;

        Long[] childLoanId = new Long[repayments.size()];
        for (int i = 0; i < repayments.size(); i++) {
            jsonObject = repayments.get(i).getAsJsonObject();
            log.debug("{}", jsonObject.toString());
            childLoanId[i] = jsonObject.get("loanId").getAsLong();
        }
        int j = 0;
        for (JsonElement element : repayments) {
            childCommand = JsonCommand.fromExistingCommand(command, element);
            result = makeLoanRepayment(LoanTransactionType.REPAYMENT, childLoanId[j++], childCommand, false);
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult makeLoanRepayment(final LoanTransactionType repaymentTransactionType, final Long loanId,
            final JsonCommand command, final boolean isRecoveryRepayment) {
        final String chargeRefundChargeType = null;
        return makeLoanRepaymentWithChargeRefundChargeType(repaymentTransactionType, loanId, command, isRecoveryRepayment,
                chargeRefundChargeType);
    }

    private void recalculateLoanWithInterestPaymentWaiverTxn(Loan loan, LoanTransaction newInterestPaymentWaiverTransaction) {
        LocalDate recalculateFrom = null;
        LocalDate transactionDate = newInterestPaymentWaiverTransaction.getTransactionDate();
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }
        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom, null);

        newInterestPaymentWaiverTransaction.updateLoan(loan);

        final boolean isTransactionChronologicallyLatest = loanTransactionService.isChronologicallyLatestRepaymentOrWaiver(loan,
                newInterestPaymentWaiverTransaction);

        final LoanRepaymentScheduleInstallment currentInstallment = loan.fetchLoanRepaymentScheduleInstallmentByDueDate(transactionDate);

        boolean reprocess = true;
        if (!loan.isForeclosure() && isTransactionChronologicallyLatest && DateUtils.isEqualBusinessDate(transactionDate)
                && currentInstallment != null && currentInstallment.getTotalOutstanding(loan.getCurrency())
                        .isEqualTo(newInterestPaymentWaiverTransaction.getAmount(loan.getCurrency()))) {
            reprocess = false;
        }

        if (isTransactionChronologicallyLatest && (!reprocess || !loan.isInterestBearingAndInterestRecalculationEnabled())
                && !loan.isForeclosure()) {
            loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(),
                    newInterestPaymentWaiverTransaction,
                    new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                            new MoneyHolder(loan.getTotalOverpaidAsMoney()), null, loan.getActiveLoanTermVariations()));

            reprocess = false;
            if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                if (currentInstallment == null || currentInstallment.isNotFullyPaidOff()) {
                    reprocess = true;
                } else {
                    final LoanRepaymentScheduleInstallment nextInstallment = loan
                            .fetchRepaymentScheduleInstallment(currentInstallment.getInstallmentNumber() + 1);
                    if (nextInstallment != null && nextInstallment.getTotalPaidInAdvance(loan.getCurrency()).isGreaterThanZero()) {
                        reprocess = true;
                    }
                }
            }
            if (!reprocess) {
                loan.addLoanTransaction(newInterestPaymentWaiverTransaction);
            }
        }
        if (reprocess) {
            reprocessChangedLoanTransactions(loan, newInterestPaymentWaiverTransaction, scheduleGeneratorDTO);
        }

        loanLifecycleStateMachine.determineAndTransition(loan, newInterestPaymentWaiverTransaction.getTransactionDate());
    }

    private void reprocessChangedLoanTransactions(Loan loan, LoanTransaction newInterestPaymentWaiverTransaction,
            ScheduleGeneratorDTO scheduleGeneratorDTO) {
        if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
        } else if (loan.isProgressiveSchedule()) {
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }
        loan.addLoanTransaction(newInterestPaymentWaiverTransaction);
        reprocessLoanTransactionsService.reprocessTransactions(loan);

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult makeInterestPaymentWaiver(final JsonCommand command) {
        loanTransactionValidator.validateLoanTransactionInterestPaymentWaiver(command);

        final Long loanId = command.getLoanId();
        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);

        businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionInterestPaymentWaiverPreBusinessEvent(loan));

        final String noteText = command.stringValueOfParameterNamed("note");
        final Map<String, Object> changes = new LinkedHashMap<>();

        LoanTransaction newInterestPaymentWaiverTransaction = loanTransactionAssembler.assembleTransactionAndCalculateChanges(loan, command,
                changes);

        recalculateLoanWithInterestPaymentWaiverTxn(loan, newInterestPaymentWaiverTransaction);

        loanTransactionValidator.validateLoanTransactionInterestPaymentWaiverAfterRecalculation(loan);

        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newInterestPaymentWaiverTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(newInterestPaymentWaiverTransaction, false, false);

        loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        saveNote(noteText, loan, newInterestPaymentWaiverTransaction);

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);
        loanAccountDomainService.setLoanDelinquencyTag(loan, newInterestPaymentWaiverTransaction.getTransactionDate());

        // disable all active standing orders linked to this loan if status changes to closed
        loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);

        loanAccountDomainService.updateAndSavePostDatedChecksForIndividualAccount(loan, newInterestPaymentWaiverTransaction);
        loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, newInterestPaymentWaiverTransaction);

        // Finished Notification
        final LoanTransactionBusinessEvent transactionRepaymentEvent = new LoanTransactionInterestPaymentWaiverPostBusinessEvent(
                newInterestPaymentWaiverTransaction);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(transactionRepaymentEvent);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withLoanId(loan.getId()) //
                .withEntityId(newInterestPaymentWaiverTransaction.getId()) //
                .withEntityExternalId(newInterestPaymentWaiverTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .with(changes) //
                .build();
    }

    private void saveNote(String noteText, Loan loan, LoanTransaction loanTransaction) {
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanTransactionNote(loan, loanTransaction, noteText);
            this.noteRepository.save(note);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult makeLoanRepaymentWithChargeRefundChargeType(final LoanTransactionType repaymentTransactionType,
            final Long loanId, final JsonCommand command, final boolean isRecoveryRepayment, final String chargeRefundChargeType) {

        this.loanUtilService.validateRepaymentTransactionType(repaymentTransactionType);
        this.loanTransactionValidator.validateNewRepaymentTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.longValueOfParameterNamed("paymentTypeId"));

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        if (!txnExternalId.isEmpty()) {
            changes.put(LoanApiConstants.externalIdParameterName, txnExternalId);
        }
        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        final Boolean isHolidayValidationDone = false;
        final HolidayDetailDTO holidayDetailDto = null;
        boolean isAccountTransfer = false;

        LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(repaymentTransactionType, loan, transactionDate,
                transactionAmount, paymentDetail, noteText, txnExternalId, isRecoveryRepayment, chargeRefundChargeType, isAccountTransfer,
                holidayDetailDto, isHolidayValidationDone);
        loan = loanTransaction.getLoan();
        this.loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, loanTransaction);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withLoanId(loan.getId()) //
                .withEntityId(loanTransaction.getId()) //
                .withEntityExternalId(loanTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public Map<String, Object> makeLoanBulkRepayment(final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand) {

        final SingleRepaymentCommand[] repaymentCommand = bulkRepaymentCommand.getLoanTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        final boolean isRecoveryRepayment = false;

        if (repaymentCommand == null) {
            return changes;
        }
        List<Long> transactionIds = new ArrayList<>();
        boolean isAccountTransfer = false;
        HolidayDetailDTO holidayDetailDTO = null;
        boolean isHolidayValidationDone = false;
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            if (singleLoanRepaymentCommand != null) {
                Loan loan = this.loanAssembler.assembleFrom(singleLoanRepaymentCommand.getLoanId());
                final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                        singleLoanRepaymentCommand.getTransactionDate());
                final WorkingDays workingDays = this.workingDaysRepository.findOne();
                final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
                boolean isHolidayEnabled;
                isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
                holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                        allowTransactionsOnNonWorkingDay);
                loanTransactionValidator.validateRepaymentDateIsOnHoliday(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
                loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
                isHolidayValidationDone = true;
                break;
            }

        }
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            if (singleLoanRepaymentCommand != null) {
                final Loan loan = this.loanAssembler.assembleFromInUserScope(singleLoanRepaymentCommand.getLoanId());
                final PaymentDetail paymentDetail = singleLoanRepaymentCommand.getPaymentDetail();
                ExternalId externalId = singleLoanRepaymentCommand.getExternalId();
                if (externalId.isEmpty() && configurationDomainService.isExternalIdAutoGenerationEnabled()) {
                    externalId = ExternalId.generate();
                }
                if (paymentDetail != null && paymentDetail.getId() == null) {
                    this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
                }
                final String chargeRefundChargeType = null;
                LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT, loan,
                        bulkRepaymentCommand.getTransactionDate(), singleLoanRepaymentCommand.getTransactionAmount(), paymentDetail,
                        bulkRepaymentCommand.getNote(), externalId, isRecoveryRepayment, chargeRefundChargeType, isAccountTransfer,
                        holidayDetailDTO, isHolidayValidationDone);
                transactionIds.add(loanTransaction.getId());
            }
        }
        changes.put("loanTransactions", transactionIds);
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

        this.loanTransactionValidator.validateTransaction(command.json());
        LoanTransaction transactionToAdjust = this.loanTransactionRepository.findByIdAndLoanId(command.entityId(), command.getLoanId())
                .orElseThrow(() -> new LoanTransactionNotFoundException(command.entityId(), command.getLoanId()));

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        if (loan.getStatus().isClosed() && loan.getLoanSubStatus() != null && loan.getLoanSubStatus().equals(LoanSubStatus.FORECLOSED)) {
            final String defaultUserMessage = "The loan cannot reopened as it is foreclosed.";
            throw new LoanForeclosureException("loan.cannot.be.reopened.as.it.is.foreclosured", defaultUserMessage, loanId);
        }
        loanWriteServiceHelper.checkClientOrGroupActive(loan);

        businessEventNotifierService.notifyPreBusinessEvent(
                new LoanAdjustTransactionBusinessEvent(new LoanAdjustTransactionBusinessEvent.Data(transactionToAdjust)));
        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.LOAN)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transfer.transaction.update.not.allowed",
                    "Loan transaction: " + transactionId + " update not allowed as it involves in account transfer", transactionId);
        }
        if (loan.isClosedWrittenOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.written.off.update.not.allowed",
                    "Loan transaction: " + transactionId + " update not allowed as loan status is written off", transactionId);
        }

        if (transactionToAdjust.hasChargebackLoanTransactionRelations()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.update.not.allowed",
                    "Loan transaction: " + transactionId + " update not allowed as loan transaction is linked to other transactions",
                    transactionId);
        }

        if (transactionToAdjust.isInterestRefund()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.update.not.allowed",
                    "Interest refund transaction: " + transactionId + " cannot be reversed or adjusted directly", transactionId);
        }

        Long commandId = command.commandId();
        final String noteText = command.stringValueOfParameterNamed("note");
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        final boolean isAdjustCommand = (transactionAmount.compareTo(BigDecimal.ZERO) > 0);
        if (isAdjustCommand && !transactionToAdjust.isEditable()) {
            final String errorMessage = "Loan transaction: " + transactionId + " update not allowed as loan transaction is a "
                    + transactionToAdjust.getTypeOf().getCode();
            throw new InvalidLoanTransactionTypeException("transaction", "error.msg.loan.transaction.update.not.allowed", errorMessage);
        }

        if (isAdjustCommand && (transactionToAdjust.isMerchantIssuedRefund() || transactionToAdjust.isPayoutRefund())) {
            if (loan.getSupportedInterestRefundTransactionTypes().contains(transactionToAdjust.getTypeOf())) {
                throw new GeneralPlatformDomainRuleException(
                        "error.msg.loan.transaction.adjust.not.allowed.when.interest.refund.enabled", "Loan transaction: " + transactionId
                                + " cannot be adjusted as interest refund is enabled for " + transactionToAdjust.getTypeOf().getCode(),
                        transactionId);
            }
            // Decide on the actual data too: replacing the refund would reverse its linked interest refund without
            // creating a new one
            if (!loanTransactionRelationRepository.findNonReversedInterestRefundsRelatedTo(transactionToAdjust).isEmpty()) {
                throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.adjust.not.allowed.when.interest.refund.exists",
                        "Loan transaction: " + transactionId + " cannot be adjusted as it has a linked interest refund transaction",
                        transactionId);
            }
        }

        if (transactionToAdjust.isChargeback() && !loan.isProgressiveSchedule()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.update.not.allowed",
                    "Chargeback transaction: " + transactionId + " reversal is only supported for progressive loans", transactionId);
        }

        // We dont need auto generation for reversal external id... if it is not provided, it remains null (empty)
        final String reversalExternalId = command.stringValueOfParameterNamedAllowingNull(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME);
        final ExternalId reversalTxnExternalId = ExternalIdFactory.produce(reversalExternalId);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.longValueOfParameterNamed("paymentTypeId"));

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);

        LoanAdjustmentParameter parameter = LoanAdjustmentParameter.builder().transactionAmount(transactionAmount)
                .paymentDetail(paymentDetail).transactionDate(transactionDate).txnExternalId(txnExternalId)
                .reversalTxnExternalId(reversalTxnExternalId).noteText(noteText).build();

        return loanAdjustmentService.adjustLoanTransaction(loan, transactionToAdjust, parameter, commandId, changes);
    }

    @Transactional
    @Override
    public CommandProcessingResult chargebackLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {
        this.loanTransactionValidator.validateChargebackTransaction(command.json());

        LoanTransaction loanTransaction = this.loanTransactionRepository.findByIdAndLoanId(command.entityId(), command.getLoanId())
                .orElseThrow(() -> new LoanTransactionNotFoundException(command.entityId(), command.getLoanId()));

        if (loanTransaction.isReversed()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as loan transaction repayment is reversed",
                    transactionId);
        }

        if (!loanTransaction.isTypeAllowedForChargeback()) {
            throw new PlatformServiceUnavailableException(
                    "error.msg.loan.chargeback.operation.not.allowed", "Loan transaction:" + transactionId
                            + " chargeback not allowed for loan transaction type, its type is " + loanTransaction.getTypeOf().getCode(),
                    transactionId);
        }

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.LOAN)) {
            throw new PlatformServiceUnavailableException("error.msg.loan.transfer.transaction.update.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as it involves in account transfer", transactionId);
        }
        if (loan.isClosedWrittenOff()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as loan status is written off", transactionId);
        }
        if (loan.isInterestBearingAndInterestRecalculationEnabled() && !loan.isProgressiveSchedule()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as loan product is interest recalculation enabled",
                    transactionId);
        }
        loanWriteServiceHelper.checkClientOrGroupActive(loan);

        businessEventNotifierService.notifyPreBusinessEvent(new LoanChargebackTransactionBusinessEvent(loanTransaction));

        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.TRANSACTION_AMOUNT_PARAMNAME);
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionAmount", command.stringValueOfParameterNamed(LoanApiConstants.TRANSACTION_AMOUNT_PARAMNAME));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.longValueOfParameterNamed(LoanApiConstants.PAYMENT_TYPE_PARAMNAME));

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        if (paymentDetail != null) {
            paymentDetail = this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
        }
        LoanTransaction newTransaction = LoanTransaction.chargeback(loan, transactionAmountAsMoney, paymentDetail, transactionDate,
                txnExternalId);

        validateLoanTransactionAmountChargeBack(loanTransaction, newTransaction);

        if (loan.isInterestBearing() && loan.isInterestRecalculationEnabled() && loan.isProgressiveSchedule()) {
            final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }

        // Store the Loan Transaction Relation
        LoanTransactionRelation loanTransactionRelation = LoanTransactionRelation.linkToTransaction(loanTransaction, newTransaction,
                LoanTransactionRelationTypeEnum.CHARGEBACK);
        this.loanTransactionRelationRepository.save(loanTransactionRelation);

        handleChargebackTransaction(loan, newTransaction);

        newTransaction = this.loanTransactionRepository.saveAndFlush(newTransaction);
        // Create journal entries immediately for this transaction
        journalEntryPoster.postJournalEntriesForLoanTransaction(newTransaction, false, false);

        loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed(LoanApiConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note = Note.loanTransactionNote(loan, newTransaction, noteText);
            this.noteRepository.save(note);
        }

        this.loanAccountDomainService.setLoanDelinquencyTag(loan, transactionDate);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargebackTransactionBusinessEvent(newTransaction));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newTransaction.getId()) //
                .withEntityExternalId(newTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void validateLoanTransactionAmountChargeBack(LoanTransaction loanTransaction, LoanTransaction chargebackTransaction) {
        BigDecimal actualAmount = BigDecimal.ZERO;
        for (LoanTransactionRelation loanTransactionRelation : loanTransaction.getLoanTransactionRelations()) {
            if (loanTransactionRelation.getRelationType().equals(LoanTransactionRelationTypeEnum.CHARGEBACK)
                    && loanTransactionRelation.getToTransaction().isNotReversed()) {
                actualAmount = actualAmount.add(loanTransactionRelation.getToTransaction().getAmount());
            }
        }
        actualAmount = actualAmount.add(chargebackTransaction.getAmount());
        if (loanTransaction.getAmount() != null && actualAmount.compareTo(loanTransaction.getAmount()) > 0) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + loanTransaction.getId() + " chargeback not allowed as loan transaction amount is not enough",
                    loanTransaction.getId());
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveInterestOnLoan(final Long loanId, final JsonCommand command) {
        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        loanTransactionValidator.validateTransaction(loan, LoanTransactionType.WAIVE_INTEREST, command.json());
        loanWriteServiceHelper.checkClientOrGroupActive(loan);

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        Money unrecognizedIncome = transactionAmountAsMoney.zero();
        Money interestComponent = transactionAmountAsMoney;
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Money receivableInterest = loanBalanceService.getReceivableInterest(loan, transactionDate);
            if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                interestComponent = receivableInterest;
                unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
            }
        }
        final LoanTransaction waiveInterestTransaction = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney,
                transactionDate, interestComponent, unrecognizedIncome, externalId);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWaiveInterestBusinessEvent(waiveInterestTransaction));
        LocalDate recalculateFrom = null;
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_REPAYMENT_OR_WAIVER);
        loanTransactionValidator.validateActivityNotBeforeLastTransactionDate(loan, waiveInterestTransaction.getTransactionDate(),
                LoanEvent.LOAN_REPAYMENT_OR_WAIVER);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                waiveInterestTransaction.getTransactionDate());
        waiveInterest(loan, waiveInterestTransaction, scheduleGeneratorDTO);

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

        this.loanTransactionRepository.saveAndFlush(waiveInterestTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(waiveInterestTransaction, false, false);
        loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, waiveInterestTransaction, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);
        loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveInterestBusinessEvent(waiveInterestTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(waiveInterestTransaction.getId()) //
                .withEntityExternalId(waiveInterestTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult writeOff(final Long loanId, final JsonCommand command) {
        final AppUser currentUser = loanWriteServiceHelper.getAppUserIfPresent();

        this.loanTransactionValidator.validateTransactionWithNoAmount(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final Map<Long, FeeState> downPaymentFeesBefore = loanDownPaymentFeeEventNotifier.captureFees(loan);
        if (command.hasParameter("writeoffReasonId")) {
            Long writeoffReasonId = command.longValueOfParameterNamed("writeoffReasonId");
            CodeValue writeoffReason = this.codeValueRepository
                    .findOneByCodeNameAndIdWithNotFoundDetection(LoanApiConstants.WRITEOFFREASONS, writeoffReasonId);
            changes.put("writeoffReasonId", writeoffReasonId);
            loan.updateWriteOffReason(writeoffReason);
        }

        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (loan.isChargedOff() && DateUtils.isBefore(transactionDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loanId
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loanId);
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWrittenOffPreBusinessEvent(loan));
        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.WRITE_OFF.getValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        loanWriteServiceHelper.removeLoanCycle(loan);
        loanWriteServiceHelper.updateLoanCounters(loan, loan.getDisbursementDate());

        LocalDate recalculateFrom = null;
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            recalculateFrom = command.localDateValueOfParameterNamed("transactionDate");
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        final LocalDate writtenOffOnLocalDate = command.localDateValueOfParameterNamed(TRANSACTION_DATE);
        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.WRITE_OFF_OUTSTANDING);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.WRITE_OFF_OUTSTANDING,
                writtenOffOnLocalDate);

        CommandProcessingResultBuilder builder = new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes);

        final Optional<LoanTransaction> loanTransactionOptional = closeAsWrittenOff(loan, command, changes, currentUser,
                scheduleGeneratorDTO);

        if (loanTransactionOptional.isPresent()) {
            final LoanTransaction loanTransaction = loanTransactionOptional.get();
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
            }
            this.loanTransactionRepository.saveAndFlush(loanTransaction);
            journalEntryPoster.postJournalEntriesForLoanTransaction(loanTransaction, false, false);
            saveLoanWithDataIntegrityViolationChecks(loan);
            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                changes.put("note", noteText);
                final Note note = Note.loanTransactionNote(loan, loanTransaction, noteText);
                this.noteRepository.save(note);
            }
            loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan,
                    loan.isInterestBearingAndInterestRecalculationEnabled(), true);
            loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
            loanDownPaymentFeeEventNotifier.notifyFeeChanges(loan, downPaymentFeesBefore);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
            businessEventNotifierService.notifyPostBusinessEvent(new LoanWrittenOffPostBusinessEvent(loanTransaction));

            builder.withEntityId(loanTransaction.getId()).withEntityExternalId(loanTransaction.getExternalId());
        }

        return builder.build();
    }

    @Transactional
    @Override
    public CommandProcessingResult closeLoan(final Long loanId, final JsonCommand command) {
        this.loanTransactionValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final Map<Long, FeeState> downPaymentFeesBefore = loanDownPaymentFeeEventNotifier.captureFees(loan);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        if (loan.isChargedOff() && DateUtils.isBefore(transactionDate, loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date", "Loan: "
                    + loanId
                    + " backdated transaction is not allowed. Transaction date cannot be earlier than the charge-off date of the loan",
                    loanId);
        }

        businessEventNotifierService.notifyPreBusinessEvent(new LoanCloseBusinessEvent(loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        loanWriteServiceHelper.updateLoanCounters(loan, loan.getDisbursementDate());

        LocalDate recalculateFrom = null;
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            recalculateFrom = command.localDateValueOfParameterNamed("transactionDate");
        }

        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        final LocalDate closureDate = command.localDateValueOfParameterNamed(TRANSACTION_DATE);
        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_CLOSED);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.REPAID_IN_FULL, closureDate);
        final Optional<LoanTransaction> loanTransactionOptional = close(loan, command, changes, scheduleGeneratorDTO);

        loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

        loanTransactionOptional.ifPresent(loanTransaction -> {
            this.loanTransactionRepository.saveAndFlush(loanTransaction);
            journalEntryPoster.postJournalEntriesForLoanTransaction(loanTransaction, false, false);
        });
        saveLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);

        loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        loanDownPaymentFeeEventNotifier.notifyFeeChanges(loan, downPaymentFeesBefore);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanCloseBusinessEvent(loan));

        // Update loan transaction on repayment.
        this.loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, null);

        // disable all active standing instructions linked to the loan
        this.loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);

        CommandProcessingResult result;
        if (loanTransactionOptional.isPresent()) {
            result = new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanTransactionOptional.get().getId()) //
                    .withEntityExternalId(loanTransactionOptional.get().getExternalId()) //
                    .withOfficeId(loan.getOfficeId()) //
                    .withClientId(loan.getClientId()) //
                    .withGroupId(loan.getGroupId()) //
                    .withLoanId(loanId) //
                    .with(changes) //
                    .build();
        } else {
            result = new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanId) //
                    .withEntityExternalId(loan.getExternalId()) //
                    .withOfficeId(loan.getOfficeId()) //
                    .withClientId(loan.getClientId()) //
                    .withGroupId(loan.getGroupId()) //
                    .withLoanId(loanId) //
                    .with(changes) //
                    .build();
        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult closeAsRescheduled(final Long loanId, final JsonCommand command) {

        this.loanTransactionValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.charged.off",
                    "Loan: " + loanId + " Close as rescheduled is not allowed. Loan Account is Charged-off", loanId);
        }
        loanWriteServiceHelper.removeLoanCycle(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanCloseAsRescheduleBusinessEvent(loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        closeAsMarkedForReschedule(loan, command, changes);

        saveLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanCloseAsRescheduleBusinessEvent(loan));

        // disable all active standing instructions linked to the loan
        this.loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);

        // Update loan transaction on repayment.
        this.loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, null);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public LoanTransaction initiateLoanTransfer(final Loan loan, final LocalDate transferDate) {
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        validateTransactionsForTransfer(loan, transferDate);

        businessEventNotifierService.notifyPreBusinessEvent(new LoanInitiateTransferBusinessEvent(loan));

        ExternalId externalId = externalIdFactory.create();
        final LoanTransaction newTransferTransaction = LoanTransaction.initiateTransfer(loan.getOffice(), loan, transferDate, externalId);
        loan.addLoanTransaction(newTransferTransaction);
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_INITIATE_TRANSFER, loan);

        this.loanTransactionRepository.saveAndFlush(newTransferTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(newTransferTransaction, false, false);
        saveLoanWithDataIntegrityViolationChecks(loan);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanInitiateTransferBusinessEvent(loan));
        return newTransferTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction acceptLoanTransfer(final Loan loan, final LocalDate transferDate, final Office acceptedInOffice,
            final Staff loanOfficer) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanAcceptTransferBusinessEvent(loan));
        ExternalId externalId = externalIdFactory.create();
        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.approveTransfer(acceptedInOffice, loan, transferDate,
                externalId);
        loan.addLoanTransaction(newTransferAcceptanceTransaction);
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_COMPLETE_TRANSFER, loan);
        if (loanOfficer != null) {
            loanOfficerService.reassignLoanOfficer(loan, loanOfficer, transferDate);
        }

        this.loanTransactionRepository.saveAndFlush(newTransferAcceptanceTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(newTransferAcceptanceTransaction, false, false);
        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAcceptTransferBusinessEvent(loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction withdrawLoanTransfer(final Loan loan, final LocalDate transferDate) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWithdrawTransferBusinessEvent(loan));

        ExternalId externalId = externalIdFactory.create();

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.withdrawTransfer(loan.getOffice(), loan, transferDate,
                externalId);
        loan.addLoanTransaction(newTransferAcceptanceTransaction);
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_WITHDRAW_TRANSFER, loan);

        this.loanTransactionRepository.saveAndFlush(newTransferAcceptanceTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(newTransferAcceptanceTransaction, false, false);
        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanWithdrawTransferBusinessEvent(loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public void rejectLoanTransfer(final Loan loan) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanRejectTransferBusinessEvent(loan));
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_REJECT_TRANSFER, loan);
        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRejectTransferBusinessEvent(loan));
    }

    @Transactional
    @Override
    public CommandProcessingResult loanReassignment(final Long loanId, final JsonCommand command) {

        this.loanTransactionValidator.validateUpdateOfLoanOfficer(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);
        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));
        if (!loan.hasLoanOfficer(fromLoanOfficer)) {
            throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId);
        }

        loanOfficerService.reassignLoanOfficer(loan, toLoanOfficer, dateOfLoanOfficerAssignment);

        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult bulkLoanReassignment(final JsonCommand command) {

        this.loanTransactionValidator.validateForBulkLoanReassignment(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");
        final String[] loanIdsAsStr = command.arrayValueOfParameterNamed("loans");
        List<Long> listLoanIds = Arrays.stream(loanIdsAsStr).map(Long::parseLong).toList();

        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);

        if (loanAccountLockService.isAnyLoanHardLocked(listLoanIds)) {
            throw new AccountLockCannotBeOverruledException("There are hard-locked loan accounts in the bulk loan reassignment");
        }

        for (final Long loanId : listLoanIds) {
            final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
            businessEventNotifierService.notifyPreBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));
            loanWriteServiceHelper.checkClientOrGroupActive(loan);

            if (!loan.hasLoanOfficer(fromLoanOfficer)) {
                throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId);
            }

            loanOfficerService.reassignLoanOfficer(loan, toLoanOfficer, dateOfLoanOfficerAssignment);
            saveLoanWithDataIntegrityViolationChecks(loan);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult removeLoanOfficer(final Long loanId, final JsonCommand command) {

        final LoanUpdateCommand loanUpdateCommand = this.loanUpdateCommandFromApiJsonDeserializer.commandFromApiJson(command.json());

        loanUpdateCommand.validate();

        final LocalDate dateOfLoanOfficerUnassigned = command.localDateValueOfParameterNamed("unassignedDate");

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);

        if (loan.getLoanOfficer() == null) {
            throw new LoanOfficerUnassignmentException(loanId);
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanRemoveOfficerBusinessEvent(loan));

        loanOfficerValidator.validateUnassignDate(loan, dateOfLoanOfficerUnassigned);
        loan.removeLoanOfficer(dateOfLoanOfficerUnassigned);

        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRemoveOfficerBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    @Override
    public void applyMeetingDateChanges(final Calendar calendar, final Collection<CalendarInstance> loanCalendarInstances) {

        final Boolean rescheduleBasedOnMeetingDates = null;
        final LocalDate presentMeetingDate = null;
        final LocalDate newMeetingDate = null;

        applyMeetingDateChanges(calendar, loanCalendarInstances, rescheduleBasedOnMeetingDates, presentMeetingDate, newMeetingDate);

    }

    @Transactional
    @Override
    public void applyMeetingDateChanges(final Calendar calendar, final Collection<CalendarInstance> loanCalendarInstances,
            final Boolean rescheduleBasedOnMeetingDates, final LocalDate presentMeetingDate, final LocalDate newMeetingDate) {

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        final Collection<LoanStatus> loanStatuses = new ArrayList<>(
                Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED, LoanStatus.ACTIVE));
        final Collection<AccountType> loanTypes = new ArrayList<>(Arrays.asList(AccountType.GROUP, AccountType.JLG));
        final Collection<Long> loanIds = new ArrayList<>(loanCalendarInstances.size());
        // loop through loanCalendarInstances to get loan ids
        for (final CalendarInstance calendarInstance : loanCalendarInstances) {
            loanIds.add(calendarInstance.getEntityId());
        }

        final List<Loan> loans = this.loanRepositoryWrapper.findByIdsAndLoanStatusAndLoanType(loanIds, loanStatuses, loanTypes);
        List<Holiday> holidays;
        final LocalDate recalculateFrom = null;
        // loop through each loan to reschedule the repayment dates
        for (final Loan loan : loans) {
            if (loan != null) {
                if (loan.getExpectedFirstRepaymentOnDate() != null && loan.getExpectedFirstRepaymentOnDate().equals(presentMeetingDate)) {
                    final String defaultUserMessage = "Meeting calendar date update is not supported since its a first repayment date";
                    throw new CalendarParameterUpdateNotSupportedException("meeting.for.first.repayment.date", defaultUserMessage,
                            loan.getExpectedFirstRepaymentOnDate(), presentMeetingDate);
                }

                if (loan.isChargedOff()) {
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.is.charged.off",
                            "Loan: " + loan.getId() + " reschedule is not allowed. Loan Account is Charged-off", loan.getId());
                }

                Boolean isSkipRepaymentOnFirstMonth = false;
                int numberOfDays = 0;
                boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
                if (isSkipRepaymentOnFirstMonthEnabled) {
                    isSkipRepaymentOnFirstMonth = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), calendar);
                    if (isSkipRepaymentOnFirstMonth) {
                        numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
                    }
                }

                holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate());
                if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                    ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                    loanScheduleService.recalculateScheduleFromLastTransaction(loan, scheduleGeneratorDTO, existingTransactionIds,
                            existingReversedTransactionIds);
                    loanWriteServiceHelper.createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
                } else if (rescheduleBasedOnMeetingDates != null && rescheduleBasedOnMeetingDates) {
                    updateLoanRepaymentScheduleDates(loan, calendar.getRecurrence(), isHolidayEnabled, holidays, workingDays,
                            presentMeetingDate, newMeetingDate, isSkipRepaymentOnFirstMonth, numberOfDays);
                } else {
                    updateLoanRepaymentScheduleDates(loan, calendar.getStartDateLocalDate(), calendar.getRecurrence(), isHolidayEnabled,
                            holidays, workingDays, isSkipRepaymentOnFirstMonth, numberOfDays);
                }

                loanAccrualsProcessingService.reprocessExistingAccruals(loan, false);
                if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                    loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, false);
                }
                saveLoanWithDataIntegrityViolationChecks(loan);
                businessEventNotifierService.notifyPostBusinessEvent(new LoanRescheduledDueCalendarChangeBusinessEvent(loan));
                loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
            }
        }
    }

    @Override
    public CommandProcessingResult undoWriteOff(Long loanId) {

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (!loan.isClosedWrittenOff()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.status.not.written.off.update.not.allowed",
                    "Loan :" + loanId + " update not allowed as loan status is not written off", loanId);
        }
        LoanTransaction writeOffTransaction = loan.findWriteOffTransaction();
        if (writeOffTransaction == null) {
            throw new PlatformServiceUnavailableException("error.msg.loan.write.off.transaction.not.found",
                    "Loan :" + loanId + " write off transaction not found", loanId);
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoWrittenOffBusinessEvent(writeOffTransaction));

        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.WRITE_OFF_OUTSTANDING_UNDO);
        undoWrittenOff(loan);
        loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }
        loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);

        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        journalEntryPoster.postJournalEntriesForLoanTransaction(writeOffTransaction, false, false);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoWrittenOffBusinessEvent(writeOffTransaction));

        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withEntityId(writeOffTransaction.getId()) //
                .withEntityExternalId(writeOffTransaction.getExternalId()) //
                .build();
    }

    @Transactional
    @Override
    @Retry(name = "recalculateInterest", fallbackMethod = "fallbackRecalculateInterest")
    public void recalculateInterest(final long loanId) {
        Loan loan = this.loanAssembler.assembleFrom(loanId);
        recalculateInterest(loan);
    }

    @Transactional
    @Override
    public Loan recalculateInterest(Loan loan) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanInterestRecalculationBusinessEvent(loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        if (loan.isCumulativeSchedule()) {
            LocalDate recalculateFrom = loan.fetchInterestRecalculateFromDate();
            ScheduleGeneratorDTO generatorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            loanScheduleService.recalculateScheduleFromLastTransaction(loan, generatorDTO, existingTransactionIds,
                    existingReversedTransactionIds);
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
            }

            loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan,
                    loan.isInterestBearingAndInterestRecalculationEnabled(), true);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanInterestRecalculationBusinessEvent(loan));
        } else {
            loanScheduleService.recalculateScheduleFromLastTransaction(loan, null, existingTransactionIds, existingReversedTransactionIds,
                    true);
            loanBalanceService.updateLoanSummaryDerivedFields(loan);
            loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanInterestRecalculationBusinessEvent(loan));
        }
        return loan;
    }

    @Override
    public CommandProcessingResult recoverFromGuarantor(final Long loanId) {
        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        this.guarantorDomainService.transferFundsFromGuarantor(loan);
        return new CommandProcessingResultBuilder() //
                .withLoanId(loanId) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .build();
    }

    @SuppressWarnings("unused")
    public void fallbackRecalculateInterest(Throwable t) {
        // NOTE: allow caller to catch the exceptions
        // NOTE: wrap throwable only if really necessary
        throw ErrorHandler.getMappable(t, null, null, "loan.recalculateinterest");
    }

    @Override
    public void updateOriginalSchedule(Loan loan) {
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            final LocalDate recalculateFrom = null;
            ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            loanWriteServiceHelper.createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }
    }

    @Override
    public CommandProcessingResult creditBalanceRefund(Long loanId, JsonCommand command) {
        this.loanTransactionValidator.validateNewRefundTransaction(command.json());

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String noteText = command.stringValueOfParameterNamedAllowingNull("note");
        final ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        if (!externalId.isEmpty()) {
            changes.put(LoanApiConstants.externalIdParameterName, externalId);
        }
        changes.put("paymentTypeId", command.longValueOfParameterNamed(LoanApiConstants.PAYMENT_TYPE_PARAMNAME));

        PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        if (paymentDetail != null) {
            paymentDetail = this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
        }

        final LoanTransaction loanTransaction = this.loanAccountDomainService.creditBalanceRefund(loan, transactionDate, transactionAmount,
                noteText, externalId, paymentDetail);

        return new CommandProcessingResultBuilder() //
                .withEntityId(loanTransaction.getId()) //
                .withEntityExternalId(loanTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withCommandId(command.commandId()) //
                .with(changes) //
                .build();

    }

    @Override
    @Transactional
    public CommandProcessingResult markLoanAsFraud(Long loanId, JsonCommand command) {
        this.loanTransactionValidator.validateMarkAsFraudLoan(command.json());

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final Map<String, Object> changes = new LinkedHashMap<>();
        final boolean fraud = command.booleanPrimitiveValueOfParameterNamed(LoanApiConstants.FRAUD_ATTRIBUTE_NAME);
        if (loan.isFraud() != fraud) {
            loan.markAsFraud(fraud);
            this.loanRepository.save(loan);
            changes.put(LoanApiConstants.FRAUD_ATTRIBUTE_NAME, fraud);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command) {

        this.loanTransactionValidator.validateNewRefundTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        // checkRefundDateIsAfterAtLeastOneRepayment(loanId, transactionDate);

        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        checkIfLoanIsPaidInAdvance(loanId, transactionAmount);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put(LoanApiConstants.externalIdParameterName, externalId);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        final PaymentDetail paymentDetail = null;

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();

        LoanTransaction loanTransaction = this.loanAccountDomainService.makeRefundForActiveLoan(loanId, commandProcessingResultBuilder,
                transactionDate, transactionAmount, paymentDetail, noteText, externalId);

        return commandProcessingResultBuilder //
                .withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .withEntityId(loanTransaction.getId()) //
                .withEntityExternalId(loanTransaction.getExternalId()) //
                .with(changes) //
                .build();

    }

    private void checkIfLoanIsPaidInAdvance(final Long loanId, final BigDecimal transactionAmount) {
        BigDecimal overpaid = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId).getPaidInAdvance();

        if (overpaid == null || overpaid.compareTo(BigDecimal.ZERO) == 0 || transactionAmount.floatValue() > overpaid.floatValue()) {
            if (overpaid == null) {
                overpaid = BigDecimal.ZERO;
            }
            throw new InvalidPaidInAdvanceAmountException(overpaid.toPlainString());
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult forecloseLoan(final Long loanId, final JsonCommand command) {
        final String json = command.json();
        final JsonElement element = fromApiJsonHelper.parse(json);
        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.transactionDateParamName, element);
        final ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);
        this.loanTransactionValidator.validateLoanForeclosure(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>();
        // Got changed to match with the rest of the APIs
        changes.put("dateFormat", command.dateFormat());
        changes.put("transactionDate", command.stringValueOfParameterNamed(LoanApiConstants.transactionDateParamName));
        changes.put("externalId", externalId);

        String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        LoanRescheduleRequest loanRescheduleRequest = null;
        for (LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
            if (!DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDateAsLocalDate(), transactionDate)
                    && loanDisbursementDetails.actualDisbursementDate() == null) {
                final String defaultUserMessage = "The loan with undisbursed tranche before foreclosure cannot be foreclosed.";
                throw new LoanForeclosureException("loan.with.undisbursed.tranche.before.foreclosure.cannot.be.foreclosured",
                        defaultUserMessage, transactionDate);
            }
        }
        this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(loan.getRepaymentScheduleInstallments(), loan,
                loanRescheduleRequest);

        LoanTransaction foreclosureTransaction = this.loanAccountDomainService.foreCloseLoan(loan, transactionDate, noteText, externalId,
                changes);

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        return commandProcessingResultBuilder //
                .withLoanId(loanId) //
                .withEntityId(foreclosureTransaction.getId()) //
                .withEntityExternalId(foreclosureTransaction.getExternalId()) //
                .with(changes) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult chargeOff(JsonCommand command) {
        loanTransactionValidator.validateChargeOffTransaction(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanApiConstants.transactionDateParamName,
                command.stringValueOfParameterNamed(LoanApiConstants.transactionDateParamName));
        changes.put(LoanApiConstants.localeParameterName, command.locale());
        changes.put(LoanApiConstants.dateFormatParameterName, command.dateFormat());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(LoanApiConstants.transactionDateParamName);
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);
        final AppUser currentUser = loanWriteServiceHelper.getAppUserIfPresent();

        Loan loan = loanAssembler.assembleFromInUserScope(command.getLoanId());
        final Long loanId = loan.getId();
        if (!loan.isOpen()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.active",
                    "Loan: " + loanId + " Charge-off is not allowed. Loan Account is not Active", loanId);
        }
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.already.charged.off",
                    "Loan: " + loanId + " is already charged-off", loanId);
        }
        if (DateUtils.isBefore(transactionDate, loan.getLastUserTransactionDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.charge.off.is.before.than.the.last.user.transaction",
                    "Loan: " + loanId + " charge-off cannot be executed. User transaction was found after the charge-off transaction date!",
                    loanId);
        }
        if (DateUtils.isDateInTheFuture(transactionDate)) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.cannot.be.a.future.date", errorMessage,
                    transactionDate);
        }

        if (loan.hasMonetaryActivityAfter(transactionDate)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.monetary.transactions.after.charge.off",
                    "Loan: " + loanId + " charge-off cannot be executed. Loan has monetary activity after the charge-off transaction date!",
                    loanId);
        }

        businessEventNotifierService.notifyPreBusinessEvent(new LoanChargeOffPreBusinessEvent(loan));

        if (command.hasParameter(LoanApiConstants.chargeOffReasonIdParamName)) {
            Long chargeOffReasonId = command.longValueOfParameterNamed(LoanApiConstants.chargeOffReasonIdParamName);
            CodeValue chargeOffReason = this.codeValueRepository
                    .findOneByCodeNameAndIdWithNotFoundDetection(LoanApiConstants.CHARGE_OFF_REASONS, chargeOffReasonId);
            changes.put(LoanApiConstants.chargeOffReasonIdParamName, chargeOffReasonId);
            loan.markAsChargedOff(transactionDate, currentUser, chargeOffReason);
        } else {
            loan.markAsChargedOff(transactionDate, currentUser, null);
        }

        loan.getLoanTransactions().stream().filter(lt -> lt.isAccrual() || lt.isAccrualAdjustment())
                .filter(transaction -> loan.getLoanProductRelatedDetail().isInterestRecognitionOnDisbursementDate()
                        ? !DateUtils.isBefore(transaction.getTransactionDate(), transactionDate)
                        : DateUtils.isAfter(transaction.getTransactionDate(), transactionDate))
                .forEach(transaction -> {
                    transaction.reverse();
                    journalEntryPoster.postJournalEntriesForLoanTransaction(transaction, false, false);
                    final LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(transaction);
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
                });

        final LoanTransaction chargeOffTransaction;

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, null, null);
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
            chargeOffTransaction = LoanTransaction.chargeOff(loan, transactionDate, txnExternalId);
            reprocessLoanTransactionsService.reprocessTransactions(loan, List.of(chargeOffTransaction));
            loan.addLoanTransaction(chargeOffTransaction);
        } else {
            chargeOffTransaction = LoanTransaction.chargeOff(loan, transactionDate, txnExternalId);
            reprocessLoanTransactionsService.processLatestTransaction(chargeOffTransaction, loan);
            loan.addLoanTransaction(chargeOffTransaction);
        }
        loanTransactionRepository.saveAndFlush(chargeOffTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(chargeOffTransaction, false, false);

        String noteText = command.stringValueOfParameterNamed(LoanApiConstants.noteParameterName);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(LoanApiConstants.noteParameterName, noteText);
            final Note note = Note.loanTransactionNote(loan, chargeOffTransaction, noteText);
            this.noteRepository.save(note);
        }

        final LoanChargeOffBehaviour chargeOffBehaviour = loan.getLoanProductRelatedDetail().getChargeOffBehaviour();
        if (chargeOffBehaviour != null && (chargeOffBehaviour.equals(LoanChargeOffBehaviour.ZERO_INTEREST)
                || chargeOffBehaviour.equals(LoanChargeOffBehaviour.ACCELERATE_MATURITY))) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargeOffPostBusinessEvent(chargeOffTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(chargeOffTransaction.getId()) //
                .withEntityExternalId(chargeOffTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult undoChargeOff(JsonCommand command) {
        this.loanTransactionValidator.validateUndoChargeOff(command.json());
        final Long loanId = command.getLoanId();
        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (!loan.isOpen()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.active",
                    "Loan: " + loanId + " Undo Charge-off is not allowed. Loan Account is not Active", loanId);
        }
        if (!loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.charged.off", "Loan: " + loanId + " is not charged-off",
                    loanId);
        }
        LoanTransaction chargedOffTransaction = loan.findChargedOffTransaction();
        if (chargedOffTransaction == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.charge.off.transaction.not.found",
                    "Loan: " + loanId + " charge-off transaction was not found", loanId);
        }
        if (!chargedOffTransaction.equals(loan.getLastUserTransaction())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.charge.off.is.not.the.last.user.transaction",
                    "Loan: " + loanId + " charge-off cannot be undone. User transaction was found after charge-off!", loanId);
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoChargeOffBusinessEvent(chargedOffTransaction));

        // check if reversalExternalId is provided
        final String reversalExternalId = command.stringValueOfParameterNamedAllowingNull(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME);
        final ExternalId reversalTxnExternalId = ExternalIdFactory.produce(reversalExternalId);

        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(chargedOffTransaction.getLoan(), chargedOffTransaction,
                "reversed");
        chargedOffTransaction.reverse(reversalTxnExternalId);
        chargedOffTransaction.manuallyAdjustedOrReversed();

        loan.liftChargeOff();
        loanTransactionRepository.saveAndFlush(chargedOffTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(chargedOffTransaction, false, false);

        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, null, null);
        if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
        } else if (loan.isProgressiveSchedule()) {
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }

        reprocessLoanTransactionsService.reprocessTransactions(loan);

        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoChargeOffBusinessEvent(chargedOffTransaction));

        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withEntityId(chargedOffTransaction.getId()) //
                .withEntityExternalId(chargedOffTransaction.getExternalId()) //
                .build();
    }

    @Override
    public CommandProcessingResult makeRefund(final Long loanId, final LoanTransactionType loanTransactionType, final JsonCommand command) {
        // API rule validations
        this.loanTransactionValidator.validateRefund(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put(LoanApiConstants.externalIdParameterName, txnExternalId);

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        // Build loan schedule generator dto
        LocalDate recalculateFrom = loan.isInterestBearingAndInterestRecalculationEnabled() ? transactionDate : null;
        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom, null);
        // Domain rule validations
        this.loanTransactionValidator.validateRefund(loan, loanTransactionType, transactionDate, scheduleGeneratorDTO);
        // Create payment details
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        // Create note
        loanWriteServiceHelper.createNote(loan, command, changes);
        // Create refund transaction(s)
        final Boolean interestRefundCalculation = command.booleanObjectValueOfParameterNamed("interestRefundCalculation");
        Pair<LoanTransaction, LoanTransaction> refundTransactions = loanAccountDomainService.makeRefund(loan, scheduleGeneratorDTO,
                loanTransactionType, transactionDate, transactionAmount, paymentDetail, txnExternalId, interestRefundCalculation);
        LoanTransaction refundTransaction = refundTransactions.getLeft();
        LoanTransaction interestRefundTransaction = refundTransactions.getRight();
        // Accrual reprocessing
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

        loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        // Calculate and apply delinquency tag (if applicable)
        loanAccountDomainService.setLoanDelinquencyTag(loan, transactionDate);
        // disable all active standing orders linked to this loan if status
        // changes to closed
        loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);
        //// Mark Post Dated Check as paid., Do we need this for refund?
        loanAccountDomainService.updateAndSavePostDatedChecksForIndividualAccount(loan, refundTransaction);
        if (interestRefundTransaction != null) {
            loanAccountDomainService.updateAndSavePostDatedChecksForIndividualAccount(loan, interestRefundTransaction);
        }
        // Collateral management
        loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, refundTransaction);
        if (interestRefundTransaction != null) {
            loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, interestRefundTransaction);
        }
        // Raise business events
        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);

        journalEntryPoster.postJournalEntriesForLoanTransaction(refundTransaction, false, false);
        if (interestRefundTransaction != null) {
            journalEntryPoster.postJournalEntriesForLoanTransaction(interestRefundTransaction, false, false);
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));

        Long entityId = refundTransaction.getId();
        ExternalId entityExternalId = refundTransaction.getExternalId();
        Long subEntityId = interestRefundTransaction != null ? interestRefundTransaction.getId() : null;
        ExternalId subEntityExternalId = interestRefundTransaction != null ? interestRefundTransaction.getExternalId() : null;
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withLoanId(loan.getId()) //
                .withEntityId(entityId) //
                .withEntityExternalId(entityExternalId) //
                .withSubEntityId(subEntityId) //
                .withSubEntityExternalId(subEntityExternalId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult makeManualInterestRefund(final Long loanId, final Long transactionId, final JsonCommand command) {
        loanTransactionValidator.validateManualInterestRefundTransaction(command.json());
        Loan loan = loanAssembler.assembleFromInUserScope(loanId);
        if (loan == null) {
            throw new LoanNotFoundException(loanId);
        }
        final LoanTransaction targetTransaction = this.loanTransactionRepository.findByIdAndLoanId(transactionId, loanId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(transactionId, loanId));

        final LocalDate transactionDate = targetTransaction.getDateOf();
        if (!(targetTransaction.isMerchantIssuedRefund() || targetTransaction.isPayoutRefund())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.not.refund.type",
                    "Target transaction must be Merchant Issued Refund or Payout Refund");
        }
        if (targetTransaction.isReversed()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.reversed", "Target transaction is already reversed");
        }
        boolean alreadyHasInterestRefund = loan.getLoanTransactions().stream()
                .anyMatch(txn -> txn.isInterestRefund()
                        && !txn.getLoanTransactionRelations(rel -> rel.getToTransaction().equals(targetTransaction)).isEmpty()
                        && !txn.isReversed());
        if (alreadyHasInterestRefund) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.interest.refund.already.exists",
                    "Interest Refund already exists for this transaction");
        }
        final BigDecimal amount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.interest.refund.amount.invalid",
                    "Amount must be provided and positive");
        }

        final boolean shouldCreateInterestRefundTransaction = loan.getLoanProductRelatedDetail().getSupportedInterestRefundTypes().stream()
                .map(LoanSupportedInterestRefundTypes::getTransactionType)
                .anyMatch(transactionType -> transactionType.equals(targetTransaction.getTypeOf()));
        if (!shouldCreateInterestRefundTransaction) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.interest.refund.not.supported",
                    "Interest Refund calculation is not supported for this loan");
        }
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put(LoanApiConstants.externalIdParameterName, txnExternalId);

        final LocalDate recalculateFrom = loan.isInterestBearingAndInterestRecalculationEnabled() ? transactionDate : null;
        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom, null);

        this.loanTransactionValidator.validateRefund(loan, LoanTransactionType.INTEREST_REFUND, transactionDate, scheduleGeneratorDTO);

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        loanWriteServiceHelper.createNote(loan, command, changes);

        final LoanTransaction interestRefundTxn = loanAccountDomainService.createManualInterestRefundWithAmount(loan, targetTransaction,
                amount, paymentDetail, txnExternalId);

        interestRefundTxn.getLoanTransactionRelations().add(
                LoanTransactionRelation.linkToTransaction(interestRefundTxn, targetTransaction, LoanTransactionRelationTypeEnum.RELATED));

        final boolean isTransactionChronologicallyLatest = loanTransactionService.isChronologicallyLatestRepaymentOrWaiver(loan,
                interestRefundTxn);
        final LoanRepaymentScheduleInstallment currentInstallment = loan.fetchLoanRepaymentScheduleInstallmentByDueDate(transactionDate);

        final boolean processLatest = isTransactionChronologicallyLatest //
                && !loan.isForeclosure() //
                && !loan.hasChargesAffectedByBackdatedRepaymentLikeTransaction(interestRefundTxn) //
                && loanTransactionProcessingService.canProcessLatestTransactionOnly(loan, interestRefundTxn, currentInstallment); //
        if (processLatest) {
            loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), interestRefundTxn,
                    new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                            new MoneyHolder(loan.getTotalOverpaidAsMoney()), null, loan.getActiveLoanTermVariations()));
            loan.addLoanTransaction(interestRefundTxn);
        } else {
            if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
            } else if (loan.isProgressiveSchedule()) {
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            }
            loan.addLoanTransaction(interestRefundTxn);
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        }

        // Update outstanding loan balances
        loanBalanceService.updateLoanOutstandingBalances(loan);

        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(interestRefundTxn);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanTransactionInterestRefundPostBusinessEvent(interestRefundTxn));

        // Accrual reprocessing
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }
        loanLifecycleStateMachine.determineAndTransition(loan, interestRefundTxn.getTransactionDate());

        loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        loanAccountDomainService.setLoanDelinquencyTag(loan, transactionDate);
        loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);

        loanAccountDomainService.updateAndSavePostDatedChecksForIndividualAccount(loan, interestRefundTxn);
        loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, interestRefundTxn);

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);

        journalEntryPoster.postJournalEntriesForLoanTransaction(interestRefundTxn, false, false);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withLoanId(loan.getId()) //
                .withEntityId(interestRefundTxn.getId()) //
                .withEntityExternalId(interestRefundTxn.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .with(changes) //
                .build();
    }

    public void handleChargebackTransaction(final Loan loan, LoanTransaction chargebackTransaction) {
        loanTransactionValidator.validateIfTransactionIsChargeback(chargebackTransaction);

        if (loan.isInterestBearing() && loan.isInterestRecalculationEnabled()) {
            loan.addLoanTransaction(chargebackTransaction);
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        } else {
            loan.addLoanTransaction(chargebackTransaction);
            loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), chargebackTransaction,
                    new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                            new MoneyHolder(loan.getTotalOverpaidAsMoney()), null, loan.getActiveLoanTermVariations()));
        }
        loanLifecycleStateMachine.determineAndTransition(loan, chargebackTransaction.getTransactionDate());
    }

    private void validateTransactionsForTransfer(final Loan loan, final LocalDate transferDate) {
        for (LoanTransaction transaction : loan.getLoanTransactions()) {
            if ((DateUtils.isEqual(transferDate, transaction.getTransactionDate())
                    && DateUtils.isEqual(transferDate, transaction.getSubmittedOnDate()))
                    || DateUtils.isBefore(transferDate, transaction.getTransactionDate())) {
                throw new GeneralPlatformDomainRuleException(TransferApiConstants.transferClientLoanException,
                        TransferApiConstants.transferClientLoanExceptionMessage, transaction.getTransactionDate(), transferDate);
            }
        }
    }

    private Optional<LoanTransaction> closeAsWrittenOff(final Loan loan, final JsonCommand command, final Map<String, Object> changes,
            final AppUser currentUser, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        closeDisbursements(loan, scheduleGeneratorDTO);

        final LocalDate writtenOffOnLocalDate = command.localDateValueOfParameterNamed(TRANSACTION_DATE);
        loan.setClosedOnDate(writtenOffOnLocalDate);
        loan.setWrittenOffOnDate(writtenOffOnLocalDate);
        loan.setClosedBy(currentUser);
        final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.WRITE_OFF_OUTSTANDING, loan);

        if (statusEnum.hasStateOf(loan.getStatus())) {
            return Optional.empty();
        }

        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull(EXTERNAL_ID);

        ExternalId externalId = ExternalIdFactory.produce(txnExternalId);

        if (externalId.isEmpty() && TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        changes.put(CLOSED_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));
        changes.put(WRITTEN_OFF_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));
        changes.put("externalId", externalId);

        if (DateUtils.isBefore(writtenOffOnLocalDate, loan.getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is written off cannot be before the loan disbursement date: "
                    + loan.getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.before.submittal.date", errorMessage,
                    writtenOffOnLocalDate, loan.getDisbursementDate());
        }

        if (DateUtils.isDateInTheFuture(writtenOffOnLocalDate)) {
            final String errorMessage = "The date on which a loan is written off cannot be in the future.";
            throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.a.future.date", errorMessage, writtenOffOnLocalDate);
        }

        final LoanTransaction loanTransaction = LoanTransaction.writeoff(loan, loan.getOffice(), writtenOffOnLocalDate, externalId);
        LocalDate lastTransactionDate = loan.getLastUserTransactionDate();
        if (DateUtils.isAfter(lastTransactionDate, writtenOffOnLocalDate)) {
            final String errorMessage = "The date of the writeoff transaction must occur on or after previous transactions.";
            throw new InvalidLoanStateTransitionException("writeoff", "must.occur.on.or.after.other.transaction.dates", errorMessage,
                    writtenOffOnLocalDate);
        }

        if (loan.isInterestBearingAndInterestRecalculationEnabled()
                && DateUtils.isBeforeBusinessDate(loanTransaction.getTransactionDate())) {
            if (loan.isProgressiveSchedule()) {
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            }
            loan.addLoanTransaction(loanTransaction);
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        } else {
            loan.addLoanTransaction(loanTransaction);
            loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), loanTransaction,
                    new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                            new MoneyHolder(loan.getTotalOverpaidAsMoney()), null, loan.getActiveLoanTermVariations()));
        }
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        loanLifecycleStateMachine.transition(LoanEvent.WRITE_OFF_OUTSTANDING, loan);
        changes.put(PARAM_STATUS, LoanEnumerations.status(loan.getLoanStatus()));
        return Optional.of(loanTransaction);
    }

    private void closeDisbursements(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        if (loan.isDisbursementAllowed() && loan.atLeastOnceDisbursed()) {
            loan.getLoanRepaymentScheduleDetail().setPrincipal(loan.getDisbursedAmount());
            loanDisbursementService.deactivateDownPaymentFeesOfUndisbursedTranches(loan);
            loan.removeDisbursementDetail();
            if (loan.isCumulativeSchedule()) {
                if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                    loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
                } else {
                    loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
                }
            }
        }
    }

    private Optional<LoanTransaction> close(final Loan loan, final JsonCommand command, final Map<String, Object> changes,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final LocalDate closureDate = command.localDateValueOfParameterNamed(TRANSACTION_DATE);
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull(EXTERNAL_ID);

        ExternalId externalId = ExternalIdFactory.produce(txnExternalId);
        if (externalId.isEmpty() && TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        loan.setClosedOnDate(closureDate);
        changes.put(CLOSED_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));

        if (DateUtils.isBefore(closureDate, loan.getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is closed cannot be before the loan disbursement date: "
                    + loan.getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close", "cannot.be.before.submittal.date", errorMessage, closureDate,
                    loan.getDisbursementDate());
        }

        if (DateUtils.isDateInTheFuture(closureDate)) {
            final String errorMessage = "The date on which a loan is closed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("close", "cannot.be.a.future.date", errorMessage, closureDate);
        }
        closeDisbursements(loan, scheduleGeneratorDTO);

        LoanTransaction loanTransaction = null;
        if (loan.isOpen()) {
            final Money totalOutstanding = loan.getSummary().getTotalOutstanding(loan.getCurrency());
            if (totalOutstanding.isGreaterThanZero() && loan.getInArrearsTolerance().isGreaterThanOrEqualTo(totalOutstanding)) {

                loan.setClosedOnDate(closureDate);
                final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.REPAID_IN_FULL, loan);
                if (!statusEnum.hasStateOf(loan.getStatus())) {
                    loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, loan);
                    changes.put(PARAM_STATUS, LoanEnumerations.status(loan.getLoanStatus()));
                }
                changes.put("externalId", externalId);
                loanTransaction = LoanTransaction.writeoff(loan, loan.getOffice(), closureDate, externalId);
                final boolean isLastTransaction = loanTransactionRepository.isChronologicallyLatest(loanTransaction.getTransactionDate(),
                        loan);
                if (!isLastTransaction) {
                    final String errorMessage = "The closing date of the loan must be on or after latest transaction date.";
                    throw new InvalidLoanStateTransitionException("close.loan", "must.occur.on.or.after.latest.transaction.date",
                            errorMessage, closureDate);
                }

                loan.addLoanTransaction(loanTransaction);
                loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), loanTransaction,
                        new TransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(),
                                new MoneyHolder(loan.getTotalOverpaidAsMoney()), null, loan.getActiveLoanTermVariations()));

                loanBalanceService.updateLoanSummaryDerivedFields(loan);
            } else if (totalOutstanding.isGreaterThanZero()) {
                final String errorMessage = "A loan with money outstanding cannot be closed";
                throw new InvalidLoanStateTransitionException("close", "loan.has.money.outstanding", errorMessage,
                        totalOutstanding.toString());
            }
        }

        if (loanBalanceService.isOverPaid(loan)) {
            final Money totalLoanOverpayment = loanBalanceService.calculateTotalOverpayment(loan);
            if (totalLoanOverpayment.isGreaterThanZero() && loan.getInArrearsTolerance().isGreaterThanOrEqualTo(totalLoanOverpayment)) {
                // TODO - KW - technically should set somewhere that this loan
                // has 'overpaid' amount
                loan.setClosedOnDate(closureDate);
                final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.REPAID_IN_FULL, loan);
                if (!statusEnum.hasStateOf(loan.getStatus())) {
                    loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, loan);
                    changes.put(PARAM_STATUS, LoanEnumerations.status(loan.getLoanStatus()));
                }
            } else if (totalLoanOverpayment.isGreaterThanZero()) {
                final String errorMessage = "The loan is marked as 'Overpaid' and cannot be moved to 'Closed (obligations met).";
                throw new InvalidLoanStateTransitionException("close", "loan.is.overpaid", errorMessage, totalLoanOverpayment.toString());
            }
        }

        return Optional.ofNullable(loanTransaction);
    }

    private void waiveInterest(final Loan loan, final LoanTransaction waiveInterestTransaction,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        loanDownPaymentHandlerService.handleRepaymentOrRecoveryOrWaiverTransaction(loan, waiveInterestTransaction, null,
                scheduleGeneratorDTO);
    }

    private void undoWrittenOff(final Loan loan) {
        final LoanTransaction writeOffTransaction = loan.findWriteOffTransaction();
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(writeOffTransaction.getLoan(), writeOffTransaction,
                "reversed");
        writeOffTransaction.reverse();
        loanLifecycleStateMachine.transition(LoanEvent.WRITE_OFF_OUTSTANDING_UNDO, loan);
        if (loan.isProgressiveSchedule()) {
            final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }
        reprocessLoanTransactionsService.reprocessTransactions(loan);
    }

    public void closeAsMarkedForReschedule(final Loan loan, final JsonCommand command, final Map<String, Object> changes) {
        final LocalDate rescheduledOn = command.localDateValueOfParameterNamed(TRANSACTION_DATE);

        loan.setClosedOnDate(rescheduledOn);
        final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_RESCHEDULE, loan);
        if (!statusEnum.hasStateOf(loan.getStatus())) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_RESCHEDULE, loan);
            changes.put(PARAM_STATUS, LoanEnumerations.status(loan.getLoanStatus()));
        }

        loan.setRescheduledOnDate(rescheduledOn);
        changes.put(CLOSED_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));
        changes.put("rescheduledOnDate", command.stringValueOfParameterNamed(TRANSACTION_DATE));

        loanTransactionValidator.validateLoanRescheduleDate(loan);
    }

    private void updateLoanRepaymentScheduleDates(final Loan loan, final LocalDate meetingStartDate, final String recuringRule,
            final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays,
            final boolean isSkipRepaymentonfirstdayofmonth, final Integer numberofDays) {
        // first repayment's from date is same as disbursement date.
        LocalDate tmpFromDate = loan.getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = loan.getLoanRepaymentScheduleDetail().getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate;
        LocalDate latestRepaymentDate = null;
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            // FIXME: AA this won't update repayment dates before current date.
            if (DateUtils.isAfter(oldDueDate, meetingStartDate) && DateUtils.isDateInTheFuture(oldDueDate)) {
                newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recuringRule, meetingStartDate, oldDueDate,
                        loanRepaymentInterval, frequency, workingDays, isSkipRepaymentonfirstdayofmonth, numberofDays);

                final LocalDate maxDateLimitForNewRepayment = getMaxDateLimitForNewRepayment(repaymentPeriodFrequencyType,
                        loanRepaymentInterval, tmpFromDate);

                if (DateUtils.isAfter(newRepaymentDate, maxDateLimitForNewRepayment)) {
                    newRepaymentDate = CalendarUtils.getNextRepaymentMeetingDate(recuringRule, meetingStartDate, tmpFromDate,
                            loanRepaymentInterval, frequency, workingDays, isSkipRepaymentonfirstdayofmonth, numberofDays);
                }

                if (isHolidayEnabled) {
                    newRepaymentDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(newRepaymentDate, holidays);
                }
                if (DateUtils.isBefore(latestRepaymentDate, newRepaymentDate)) {
                    latestRepaymentDate = newRepaymentDate;
                }

                loanRepaymentScheduleInstallment.updateDueDate(newRepaymentDate);
                // reset from date to get actual daysInPeriod
                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);
                tmpFromDate = newRepaymentDate;// update with new repayment date
            } else {
                tmpFromDate = oldDueDate;
            }
        }
        if (latestRepaymentDate != null) {
            loan.setExpectedMaturityDate(latestRepaymentDate);
        }
    }

    private LocalDate getMaxDateLimitForNewRepayment(final PeriodFrequencyType periodFrequencyType, final Integer loanRepaymentInterval,
            final LocalDate startDate) {
        LocalDate dueRepaymentPeriodDate = startDate;
        final int repaidEvery = 2 * loanRepaymentInterval;
        switch (periodFrequencyType) {
            case DAYS -> dueRepaymentPeriodDate = startDate.plusDays(repaidEvery);
            case WEEKS -> dueRepaymentPeriodDate = startDate.plusWeeks(repaidEvery);
            case MONTHS -> dueRepaymentPeriodDate = startDate.plusMonths(repaidEvery);
            case YEARS -> dueRepaymentPeriodDate = startDate.plusYears(repaidEvery);
            // This bounds the rescheduling of a loan driven by a meeting calendar, which a semi-monthly loan cannot
            // be attached to, since CalendarFrequencyType has no semi-monthly equivalent.
            case SEMI_MONTHLY, INVALID, WHOLE_TERM -> {
            }
        }
        return dueRepaymentPeriodDate.minusDays(1);// get 2n-1 range date from startDate
    }

    private void updateLoanRepaymentScheduleDates(final Loan loan, final String recurringRule, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays, final LocalDate presentMeetingDate, final LocalDate newMeetingDate,
            final boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {
        // first repayment's from date is same as disbursement date.
        // meetingStartDate is used as seedDate Capture the seedDate from user and use the seedDate as meetingStart date

        LocalDate tmpFromDate = loan.getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = loan.getLoanRepaymentScheduleDetail().getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate;
        boolean isFirstTime = true;
        LocalDate latestRepaymentDate = null;
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();
            if (!DateUtils.isBefore(oldDueDate, presentMeetingDate)) {
                if (isFirstTime) {
                    isFirstTime = false;
                    newRepaymentDate = newMeetingDate;
                } else {
                    // tmpFromDate.plusDays(1) is done to make sure
                    // getNewRepaymentMeetingDate method returns next meeting
                    // date and not the same as tmpFromDate
                    newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recurringRule, tmpFromDate, tmpFromDate.plusDays(1),
                            loanRepaymentInterval, frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays);
                }

                if (isHolidayEnabled) {
                    newRepaymentDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(newRepaymentDate, holidays);
                }
                if (DateUtils.isBefore(latestRepaymentDate, newRepaymentDate)) {
                    latestRepaymentDate = newRepaymentDate;
                }
                loanRepaymentScheduleInstallment.updateDueDate(newRepaymentDate);
                // reset from date to get actual daysInPeriod

                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);

                tmpFromDate = newRepaymentDate;// update with new repayment date
            } else {
                tmpFromDate = oldDueDate;
            }
        }
        if (latestRepaymentDate != null) {
            loan.setExpectedMaturityDate(latestRepaymentDate);
        }
    }
}
