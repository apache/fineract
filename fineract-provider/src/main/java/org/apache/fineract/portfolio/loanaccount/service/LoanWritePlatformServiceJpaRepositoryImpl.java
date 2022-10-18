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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAcceptTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApplyOverdueChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanChargebackTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanCloseAsRescheduleBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanCloseBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanInitiateTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanInterestRecalculationBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanReassignOfficerBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRejectTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRemoveOfficerBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRescheduledDueCalendarChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanUndoDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanUndoLastDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanUpdateDisbursementDataBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanWithdrawTransferBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanAddChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanDeleteChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanUpdateChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeUndoBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeRefundBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanDisbursalTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoWrittenOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWaiveInterestBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWrittenOffPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWrittenOffPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionPriority;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.exception.CalendarParameterUpdateNotSupportedException;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeDeletedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeDeletedException.LoanChargeCannotBeDeletedReason;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBePayedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeUpdatedException.LoanChargeCannotBeUpdatedReason;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeWaivedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeWaivedException.LoanChargeCannotBeWaivedReason;
import org.apache.fineract.portfolio.charge.exception.LoanChargeNotFoundException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeWaiveCannotBeReversedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.exception.LoanCollateralAmountNotSufficientException;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.command.LoanUpdateCommand;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetailsRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOverdueInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSubStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheDisbursementCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.DateMismatchException;
import org.apache.fineract.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.apache.fineract.portfolio.loanaccount.exception.InstallmentNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidPaidInAdvanceAmountException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeRefundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanForeclosureException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanMultiDisbursementException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerUnassignmentException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanEventApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanproduct.data.LoanOverdueDTO;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.transfer.api.TransferApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private final PlatformSecurityContext context;
    private final LoanEventApiJsonValidator loanEventApiJsonValidator;
    private final LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccountDomainService loanAccountDomainService;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanTransactionRelationRepository loanTransactionRelationRepository;
    private final LoanAssembler loanAssembler;
    private final ChargeRepositoryWrapper chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final AccountTransferRepository accountTransferRepository;
    private final CalendarRepository calendarRepository;
    private final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    private final LoanApplicationCommandFromApiJsonHelper loanApplicationCommandFromApiJsonHelper;
    private final AccountAssociationsRepository accountAssociationRepository;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final GuarantorDomainService guarantorDomainService;
    private final LoanUtilService loanUtilService;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessingStrategy;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CashierTransactionDataValidator cashierTransactionDataValidator;
    private final GLIMAccountInfoRepository glimRepository;
    private final LoanRepository loanRepository;
    private final RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler;
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final LoanDisbursementDetailsRepository loanDisbursementDetailsRepository;
    private final LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        return new DefaultLoanLifecycleStateMachine(LoanStatus.values(), businessEventNotifierService);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseGLIMLoan(final Long loanId, final JsonCommand command) {
        final Long parentLoanId = loanId;
        GroupLoanIndividualMonitoringAccount parentLoan = glimRepository.findById(parentLoanId).orElseThrow();
        List<Loan> childLoans = this.loanRepository.findByGlimId(loanId);
        CommandProcessingResult result = null;
        int count = 0;
        for (Loan loan : childLoans) {
            result = disburseLoan(loan.getId(), command, false);
            if (result.getLoanId() != null) {
                count++;
                // if all the child loans are approved, mark the parent loan as
                // approved
                if (count == parentLoan.getChildAccountsCount()) {
                    parentLoan.setLoanStatus(LoanStatus.ACTIVE.getValue());
                    glimRepository.save(parentLoan);
                }
            }
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command, Boolean isAccountTransfer) {

        final AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateDisbursement(command.json(), isAccountTransfer);

        if (command.parameterExists("postDatedChecks")) {
            // validate with post dated checks for the disbursement
            this.loanEventApiJsonValidator.validateDisbursementWithPostDatedChecks(command.json(), loanId);
        }

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        // Fail fast if client/group is not active or actual loan status disallows disbursal
        checkClientOrGroupActive(loan);
        if (loan.loanProduct().isDisallowExpectedDisbursements()) {
            List<LoanDisbursementDetails> filteredList = loan.getDisbursementDetails().stream()
                    .filter(disbursementDetails -> disbursementDetails.actualDisbursementDate() == null).toList();
            // Check whether a new LoanDisbursementDetails is required
            if (filteredList.isEmpty()) {
                // create artificial 'tranche/expected disbursal' as current disburse code expects it for
                // multi-disbursal
                // products
                final LocalDate artificialExpectedDate = loan.getExpectedDisbursedOnLocalDate();
                LoanDisbursementDetails disbursementDetail = new LoanDisbursementDetails(artificialExpectedDate, null,
                        loan.getDisbursedAmount(), null, false);
                disbursementDetail.updateLoan(loan);
                loan.getAllDisbursementDetails().add(disbursementDetail);
            }
        }
        loan.validateAccountStatus(LoanEvent.LOAN_DISBURSED);

        // Get disbursedAmount
        final BigDecimal disbursedAmount = loan.getDisbursedAmount();
        final Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();

        // Get relevant loan collateral modules
        if ((loanCollateralManagements != null && loanCollateralManagements.size() != 0)
                && AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {

            BigDecimal totalCollateral = BigDecimal.valueOf(0);

            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                BigDecimal quantity = loanCollateralManagement.getQuantity();
                BigDecimal pctToBase = loanCollateralManagement.getClientCollateralManagement().getCollaterals().getPctToBase();
                BigDecimal basePrice = loanCollateralManagement.getClientCollateralManagement().getCollaterals().getBasePrice();
                totalCollateral = totalCollateral.add(quantity.multiply(basePrice).multiply(pctToBase).divide(BigDecimal.valueOf(100)));
            }

            // Validate the loan collateral value against the disbursedAmount
            if (disbursedAmount.compareTo(totalCollateral) > 0) {
                throw new LoanCollateralAmountNotSufficientException(disbursedAmount);
            }
        }

        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");

        // validate ActualDisbursement Date Against Expected Disbursement Date
        LoanProduct loanProduct = loan.loanProduct();
        if (loanProduct.syncExpectedWithDisbursementDate()) {
            syncExpectedDateWithActualDisbursementDate(loan, actualDisbursementDate);
        }

        final LocalDate nextPossibleRepaymentDate = loan.getNextPossibleRepaymentDateForRescheduling();
        final LocalDate rescheduledRepaymentDate = command.localDateValueOfParameterNamed("adjustRepaymentDate");

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.DISBURSE.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        LocalDate recalculateFrom = null;
        if (!loan.isMultiDisburmentLoan()) {
            loan.setActualDisbursementDate(actualDisbursementDate);
        }
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        // validate actual disbursement date against meeting date
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        if (loan.isSyncDisbursementWithMeeting()) {
            this.loanEventApiJsonValidator.validateDisbursementDateWithMeetingDate(actualDisbursementDate, calendarInstance,
                    scheduleGeneratorDTO.isSkipRepaymentOnFirstDayofMonth(), scheduleGeneratorDTO.getNumberOfdays());
        }

        businessEventNotifierService.notifyPreBusinessEvent(new LoanDisbursalBusinessEvent(loan));

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Map<String, Object> changes = new LinkedHashMap<>();

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        if (paymentDetail != null && paymentDetail.getPaymentType() != null && paymentDetail.getPaymentType().getIsCashPayment()) {
            BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
            this.cashierTransactionDataValidator.validateOnLoanDisbursal(currentUser, loan.getCurrencyCode(), transactionAmount);
        }
        final boolean isPaymentTypeApplicableForDisbursementCharge = configurationDomainService
                .isPaymentTypeApplicableForDisbursementCharge();

        // Recalculate first repayment date based in actual disbursement date.
        updateLoanCounters(loan, actualDisbursementDate);
        Money amountBeforeAdjust = loan.getPrincipal();
        boolean canDisburse = loan.canDisburse(actualDisbursementDate);
        ChangedTransactionDetail changedTransactionDetail = null;
        if (canDisburse) {

            // Get netDisbursalAmount from disbursal screen field.
            final BigDecimal netDisbursalAmount = command
                    .bigDecimalValueOfParameterNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName);
            if (netDisbursalAmount != null) {
                loan.setNetDisbursalAmount(netDisbursalAmount);
            }
            Money disburseAmount = loan.adjustDisburseAmount(command, actualDisbursementDate);
            Money amountToDisburse = disburseAmount.copy();
            boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincipal());
            final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

            if (loan.isTopup() && loan.getClientId() != null) {
                final Long loanIdToClose = loan.getTopupLoanDetails().getLoanIdToClose();
                final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, loan.getClientId());
                if (loanToClose == null) {
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.with.topup.is.not.active",
                            "Loan to be closed with this topup is not active.");
                }
                final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                if (loan.getDisbursementDate().isBefore(lastUserTransactionOnLoanToClose)) {
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                            "Disbursal date of this loan application " + loan.getDisbursementDate()
                                    + " should be after last transaction date of loan to be closed " + lastUserTransactionOnLoanToClose);
                }

                BigDecimal loanOutstanding = this.loanReadPlatformService
                        .retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanIdToClose, actualDisbursementDate).getAmount();
                final BigDecimal firstDisbursalAmount = loan.getFirstDisbursalAmount();
                if (loanOutstanding.compareTo(firstDisbursalAmount) > 0) {
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                            "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                }

                amountToDisburse = disburseAmount.minus(loanOutstanding);

                disburseLoanToLoan(loan, command, loanOutstanding);
            }

            if (isAccountTransfer) {
                disburseLoanToSavings(loan, command, amountToDisburse, paymentDetail);
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
            } else {
                existingTransactionIds.addAll(loan.findExistingTransactionIds());
                existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
                LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), amountToDisburse, paymentDetail,
                        actualDisbursementDate, txnExternalId);
                disbursementTransaction.updateLoan(loan);
                loan.addLoanTransaction(disbursementTransaction);
            }

            if (loan.getRepaymentScheduleInstallments().size() == 0) {
                /*
                 * If no schedule, generate one (applicable to non-tranche multi-disbursal loans)
                 */
                recalculateSchedule = true;
            }
            regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO, nextPossibleRepaymentDate,
                    rescheduledRepaymentDate);
            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
            }
            if (isPaymentTypeApplicableForDisbursementCharge) {
                changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, paymentDetail);
            } else {
                changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, null);
            }

            loan.adjustNetDisbursalAmount(amountToDisburse.getAmount());
        }
        if (!changes.isEmpty()) {

            loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.loanNote(loan, noteText);
                this.noteRepository.save(note);
            }

            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }

            // auto create standing instruction
            createStandingInstruction(loan);

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }

        final Set<LoanCharge> loanCharges = loan.getActiveCharges();
        final Map<Long, BigDecimal> disBuLoanCharges = new HashMap<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement() && loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()
                    && loanCharge.isChargePending()) {
                disBuLoanCharges.put(loanCharge.getId(), loanCharge.amountOutstanding());
            }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        for (final Map.Entry<Long, BigDecimal> entrySet : disBuLoanCharges.entrySet()) {
            final PortfolioAccountData savingAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            final SavingsAccount fromSavingsAccount = null;
            final boolean isRegularTransaction = true;
            final boolean isExceptionForBalanceCheck = false;
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.getId(), loanId, "Loan Charge Payment",
                    locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(), entrySet.getKey(), null,
                    AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                    isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        }

        updateRecurringCalendarDatesForInterestRecalculation(loan);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        // Post Dated Checks
        if (command.parameterExists("postDatedChecks")) {
            // get repayment with post dates checks to update
            Set<PostDatedChecks> postDatedChecks = this.repaymentWithPostDatedChecksAssembler.fromParsedJson(command.json(), loan);
            updatePostDatedChecks(postDatedChecks);
        }

        businessEventNotifierService.notifyPostBusinessEvent(new LoanDisbursalBusinessEvent(loan));

        Long entityId = loan.getId();

        // During a disbursement, the entityId should be the disbursement transaction id
        if (!isAccountTransfer) {
            LoanTransaction disbursalTransaction = loan.getLoanTransactions().get(loan.getLoanTransactions().size() - 1);
            entityId = disbursalTransaction.getId();
            businessEventNotifierService.notifyPostBusinessEvent(new LoanDisbursalTransactionBusinessEvent(disbursalTransaction));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(entityId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void updatePostDatedChecks(Set<PostDatedChecks> postDatedChecks) {
        this.postDatedChecksRepository.saveAll(postDatedChecks);
    }

    private void createAndSaveLoanScheduleArchive(final Loan loan, ScheduleGeneratorDTO scheduleGeneratorDTO) {
        LoanRescheduleRequest loanRescheduleRequest = null;
        LoanScheduleModel loanScheduleModel = loan.regenerateScheduleModel(scheduleGeneratorDTO);
        List<LoanRepaymentScheduleInstallment> installments = retrieveRepaymentScheduleFromModel(loanScheduleModel);
        this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(installments, loan, loanRescheduleRequest);
    }

    /**
     * create standing instruction for disbursed loan
     *
     * @param loan
     *            the disbursed loan
     *
     **/
    private void createStandingInstruction(Loan loan) {

        if (loan.shouldCreateStandingInstructionAtDisbursement()) {
            AccountAssociations accountAssociations = this.accountAssociationRepository.findByLoanIdAndType(loan.getId(),
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());

            if (accountAssociations != null) {

                SavingsAccount linkedSavingsAccount = accountAssociations.linkedSavingsAccount();

                // name is auto-generated
                final String name = "To loan " + loan.getAccountNumber() + " from savings " + linkedSavingsAccount.getAccountNumber();
                final Office fromOffice = loan.getOffice();
                final Client fromClient = loan.getClient();
                final Office toOffice = loan.getOffice();
                final Client toClient = loan.getClient();
                final Integer priority = StandingInstructionPriority.MEDIUM.getValue();
                final Integer transferType = AccountTransferType.LOAN_REPAYMENT.getValue();
                final Integer instructionType = StandingInstructionType.DUES.getValue();
                final Integer status = StandingInstructionStatus.ACTIVE.getValue();
                final Integer recurrenceType = AccountTransferRecurrenceType.AS_PER_DUES.getValue();
                final LocalDate validFrom = DateUtils.getBusinessLocalDate();

                AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToLoanTransfer(fromOffice, fromClient,
                        linkedSavingsAccount, toOffice, toClient, loan, transferType);

                AccountTransferStandingInstruction accountTransferStandingInstruction = AccountTransferStandingInstruction.create(
                        accountTransferDetails, name, priority, instructionType, status, null, validFrom, null, recurrenceType, null, null,
                        null);
                accountTransferDetails.updateAccountTransferStandingInstruction(accountTransferStandingInstruction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        }
    }

    private void updateRecurringCalendarDatesForInterestRecalculation(final Loan loan) {

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && loan.loanInterestRecalculationDetails().getRestFrequencyType().isSameAsRepayment()) {
            final CalendarInstance calendarInstanceForInterestRecalculation = this.calendarInstanceRepository
                    .findByEntityIdAndEntityTypeIdAndCalendarTypeId(loan.loanInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue(), CalendarType.COLLECTION.getValue());

            Calendar calendarForInterestRecalculation = calendarInstanceForInterestRecalculation.getCalendar();
            calendarForInterestRecalculation.updateStartAndEndDate(loan.getDisbursementDate(), loan.getMaturityDate());
            this.calendarRepository.save(calendarForInterestRecalculation);
        }

    }

    private Loan saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        /*
         * Due to the "saveAndFlushLoanWithDataIntegrityViolationChecks" method the loan is saved and flushed in the
         * middle of the transaction. EclipseLink is in some situations are saving inconsistently the newly created
         * associations, like the newly created repayment schedule installments. The save and flush cannot be removed
         * safely till any native queries are used as part of this transaction either. See:
         * this.loanAccountDomainService.recalculateAccruals(loan);
         */
        try {
            loanRepaymentScheduleInstallmentRepository.saveAll(loan.getRepaymentScheduleInstallments());
            return this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
            throw e;
        }
    }

    private void saveLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            this.loanRepositoryWrapper.save(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }
    }

    /****
     * TODO Vishwas: Pair with Ashok and re-factor collection sheet code-base
     *
     * May of the changes made to disburseLoan aren't being made here, should refactor to reuse disburseLoan ASAP
     *****/
    @Transactional
    @Override
    public Map<String, Object> bulkLoanDisbursal(final JsonCommand command, final CollectionSheetBulkDisbursalCommand bulkDisbursalCommand,
            Boolean isAccountTransfer) {
        final AppUser currentUser = getAppUserIfPresent();

        final SingleDisbursalCommand[] disbursalCommand = bulkDisbursalCommand.getDisburseTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        if (disbursalCommand == null) {
            return changes;
        }

        final LocalDate nextPossibleRepaymentDate = null;
        final LocalDate rescheduledRepaymentDate = null;

        for (final SingleDisbursalCommand singleLoanDisbursalCommand : disbursalCommand) {
            Loan loan = this.loanAssembler.assembleFrom(singleLoanDisbursalCommand.getLoanId());
            final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");

            // validate ActualDisbursement Date Against Expected Disbursement
            // Date
            LoanProduct loanProduct = loan.loanProduct();
            if (loanProduct.syncExpectedWithDisbursementDate()) {
                syncExpectedDateWithActualDisbursementDate(loan, actualDisbursementDate);
            }
            checkClientOrGroupActive(loan);
            businessEventNotifierService.notifyPreBusinessEvent(new LoanDisbursalBusinessEvent(loan));

            final List<Long> existingTransactionIds = new ArrayList<>();
            final List<Long> existingReversedTransactionIds = new ArrayList<>();

            final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

            // Bulk disbursement should happen on meeting date (mostly from
            // collection sheet).
            // FIXME: AA - this should be first meeting date based on
            // disbursement date and next available meeting dates
            // assuming repayment schedule won't regenerate because expected
            // disbursement and actual disbursement happens on same date
            loan.validateAccountStatus(LoanEvent.LOAN_DISBURSED);
            updateLoanCounters(loan, actualDisbursementDate);
            boolean canDisburse = loan.canDisburse(actualDisbursementDate);
            ChangedTransactionDetail changedTransactionDetail = null;
            if (canDisburse) {
                Money amountBeforeAdjust = loan.getPrincipal();
                Money disburseAmount = loan.adjustDisburseAmount(command, actualDisbursementDate);
                boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincipal());
                final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
                if (isAccountTransfer) {
                    disburseLoanToSavings(loan, command, disburseAmount, paymentDetail);
                    existingTransactionIds.addAll(loan.findExistingTransactionIds());
                    existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());

                } else {
                    existingTransactionIds.addAll(loan.findExistingTransactionIds());
                    existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
                    LoanTransaction disbursementTransaction = LoanTransaction.disbursement(loan.getOffice(), disburseAmount, paymentDetail,
                            actualDisbursementDate, txnExternalId);
                    disbursementTransaction.updateLoan(loan);
                    loan.addLoanTransaction(disbursementTransaction);
                    businessEventNotifierService
                            .notifyPostBusinessEvent(new LoanDisbursalTransactionBusinessEvent(disbursementTransaction));
                }
                LocalDate recalculateFrom = null;
                final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO, nextPossibleRepaymentDate,
                        rescheduledRepaymentDate);
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
                }
                if (configurationDomainService.isPaymentTypeApplicableForDisbursementCharge()) {
                    changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, paymentDetail);
                } else {
                    changedTransactionDetail = loan.disburse(currentUser, command, changes, scheduleGeneratorDTO, null);
                }
            }
            if (!changes.isEmpty()) {

                loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

                final String noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
                if (changedTransactionDetail != null) {
                    for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings()
                            .entrySet()) {
                        this.loanTransactionRepository.save(mapEntry.getValue());
                        this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            }
            final Set<LoanCharge> loanCharges = loan.getActiveCharges();
            final Map<Long, BigDecimal> disBuLoanCharges = new HashMap<>();
            for (final LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isDueAtDisbursement() && loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()
                        && loanCharge.isChargePending()) {
                    disBuLoanCharges.put(loanCharge.getId(), loanCharge.amountOutstanding());
                }
            }
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            for (final Map.Entry<Long, BigDecimal> entrySet : disBuLoanCharges.entrySet()) {
                final PortfolioAccountData savingAccountData = this.accountAssociationsReadPlatformService
                        .retriveLoanLinkedAssociation(loan.getId());
                final SavingsAccount fromSavingsAccount = null;
                final boolean isRegularTransaction = true;
                final boolean isExceptionForBalanceCheck = false;
                final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                        PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.getId(), loan.getId(),
                        "Loan Charge Payment", locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(),
                        entrySet.getKey(), null, AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null,
                        fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
            }
            updateRecurringCalendarDatesForInterestRecalculation(loan);
            loanAccountDomainService.recalculateAccruals(loan);
            loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
            businessEventNotifierService.notifyPostBusinessEvent(new LoanDisbursalBusinessEvent(loan));
        }

        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoGLIMLoanDisbursal(final Long loanId, final JsonCommand command) {
        final Long parentLoanId = loanId;
        GroupLoanIndividualMonitoringAccount parentLoan = glimRepository.findById(parentLoanId).orElseThrow();
        List<Loan> childLoans = this.loanRepository.findByGlimId(loanId);
        CommandProcessingResult result = null;
        int count = 0;
        for (Loan loan : childLoans) {
            result = undoLoanDisbursal(loan.getId(), command);
            if (result.getLoanId() != null) {
                count++;
                // if all the child loans are approved, mark the parent loan as
                // approved
                if (count == parentLoan.getChildAccountsCount()) {
                    parentLoan.setLoanStatus(LoanStatus.APPROVED.getValue());
                    glimRepository.save(parentLoan);
                }
            }
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoLoanDisbursal(final Long loanId, final JsonCommand command) {

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoDisbursalBusinessEvent(loan));
        removeLoanCycle(loan);
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        //
        final MonetaryCurrency currency = loan.getCurrency();

        final LocalDate recalculateFrom = null;
        loan.setActualDisbursementDate(null);
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        // Remove post dated checks if added.
        loan.removePostDatedChecks();

        final Map<String, Object> changes = loan.undoDisbursal(scheduleGeneratorDTO, existingTransactionIds,
                existingReversedTransactionIds);

        if (!changes.isEmpty()) {
            if (loan.isTopup() && loan.getClientId() != null) {
                final Long loanIdToClose = loan.getTopupLoanDetails().getLoanIdToClose();
                final LocalDate expectedDisbursementDate = command
                        .localDateValueOfParameterNamed(LoanApiConstants.expectedDisbursementDateParameterName);
                BigDecimal loanOutstanding = this.loanReadPlatformService
                        .retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanIdToClose, expectedDisbursementDate).getAmount();
                BigDecimal netDisbursalAmount = loan.getApprovedPrincipal().subtract(loanOutstanding);
                loan.adjustNetDisbursalAmount(netDisbursalAmount);
            }
            loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            this.accountTransfersWritePlatformService.reverseAllTransactions(loanId, PortfolioAccountType.LOAN);
            String noteText;
            if (command.hasParameter("note")) {
                noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
            }
            boolean isAccountTransfer = false;
            final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                    existingReversedTransactionIds, isAccountTransfer);
            journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);

            // Remove All the Disbursement Details If the Loan Product is disabled and exists one
            if (loan.loanProduct().isDisallowExpectedDisbursements()) {
                if (!loan.getDisbursementDetails().isEmpty()) {
                    List<LoanDisbursementDetails> reversedDisbursementDetails = new ArrayList<LoanDisbursementDetails>();
                    for (LoanDisbursementDetails disbursementDetail : loan.getAllDisbursementDetails()) {
                        disbursementDetail.reverse();
                        reversedDisbursementDetails.add(disbursementDetail);
                    }
                    this.loanDisbursementDetailsRepository.saveAllAndFlush(reversedDisbursementDetails);
                }
            }

            businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoDisbursalBusinessEvent(loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
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

    private CommandProcessingResult makeLoanRepaymentWithChargeRefundChargeType(final LoanTransactionType repaymentTransactionType,
            final Long loanId, final JsonCommand command, final boolean isRecoveryRepayment, final String chargeRefundChargeType) {

        this.loanUtilService.validateRepaymentTransactionType(repaymentTransactionType);
        this.loanEventApiJsonValidator.validateNewRepaymentTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.stringValueOfParameterNamed("paymentTypeId"));

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        final Boolean isHolidayValidationDone = false;
        final HolidayDetailDTO holidayDetailDto = null;
        boolean isAccountTransfer = false;
        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(repaymentTransactionType, loan,
                commandProcessingResultBuilder, transactionDate, transactionAmount, paymentDetail, noteText, txnExternalId,
                isRecoveryRepayment, chargeRefundChargeType, isAccountTransfer, holidayDetailDto, isHolidayValidationDone);

        // Update loan transaction on repayment.
        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                loanCollateralManagement.setLoanTransactionData(loanTransaction);
                ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();

                if (loan.getStatus().isClosed()) {
                    loanCollateralManagement.setIsReleased(true);
                    BigDecimal quantity = loanCollateralManagement.getQuantity();
                    clientCollateralManagement.updateQuantity(clientCollateralManagement.getQuantity().add(quantity));
                    loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
                }
            }
            this.loanAccountDomainService.updateLoanCollateralTransaction(loanCollateralManagements);
        }

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
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
                Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(singleLoanRepaymentCommand.getLoanId());
                final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                        singleLoanRepaymentCommand.getTransactionDate());
                final WorkingDays workingDays = this.workingDaysRepository.findOne();
                final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
                boolean isHolidayEnabled;
                isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
                holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                        allowTransactionsOnNonWorkingDay);
                loan.validateRepaymentDateIsOnHoliday(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
                loan.validateRepaymentDateIsOnNonWorkingDay(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
                isHolidayValidationDone = true;
                break;
            }

        }
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            if (singleLoanRepaymentCommand != null) {
                final Loan loan = this.loanAssembler.assembleFrom(singleLoanRepaymentCommand.getLoanId());
                final PaymentDetail paymentDetail = singleLoanRepaymentCommand.getPaymentDetail();
                if (paymentDetail != null && paymentDetail.getId() == null) {
                    this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
                }
                final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
                final String chargeRefundChargeType = null;
                LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT, loan,
                        commandProcessingResultBuilder, bulkRepaymentCommand.getTransactionDate(),
                        singleLoanRepaymentCommand.getTransactionAmount(), paymentDetail, bulkRepaymentCommand.getNote(), null,
                        isRecoveryRepayment, chargeRefundChargeType, isAccountTransfer, holidayDetailDTO, isHolidayValidationDone);
                transactionIds.add(loanTransaction.getId());
            }
        }
        changes.put("loanTransactions", transactionIds);
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        if (loan.getStatus().isClosed() && loan.getLoanSubStatus() != null
                && loan.getLoanSubStatus().equals(LoanSubStatus.FORECLOSED.getValue())) {
            final String defaultUserMessage = "The loan cannot reopened as it is foreclosed.";
            throw new LoanForeclosureException("loan.cannot.be.reopened.as.it.is.foreclosured", defaultUserMessage, loanId);
        }
        checkClientOrGroupActive(loan);
        final LoanTransaction transactionToAdjust = this.loanTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(transactionId));
        businessEventNotifierService.notifyPreBusinessEvent(
                new LoanAdjustTransactionBusinessEvent(new LoanAdjustTransactionBusinessEvent.Data(transactionToAdjust)));
        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.LOAN)) {
            throw new PlatformServiceUnavailableException("error.msg.loan.transfer.transaction.update.not.allowed",
                    "Loan transaction:" + transactionId + " update not allowed as it involves in account transfer", transactionId);
        }
        if (loan.isClosedWrittenOff()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.written.off.update.not.allowed",
                    "Loan transaction:" + transactionId + " update not allowed as loan status is written off", transactionId);
        }

        if (transactionToAdjust.hasLoanTransactionRelations()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.transaction.update.not.allowed",
                    "Loan transaction:" + transactionId + " update not allowed as loan transaction is linked to other transactions",
                    transactionId);
        }

        if (transactionToAdjust.isChargeback()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.transaction.update.not.allowed", "Loan transaction:"
                    + transactionId + " update not allowed as loan transaction is charge back and is linked to other transaction",
                    transactionId);
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
        final String reversalExternalId = command.stringValueOfParameterNamedAllowingNull(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.stringValueOfParameterNamed("paymentTypeId"));

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        LoanTransaction newTransactionDetail = LoanTransaction.repayment(loan.getOffice(), transactionAmountAsMoney, paymentDetail,
                transactionDate, txnExternalId);
        if (transactionToAdjust.isInterestWaiver()) {
            Money unrecognizedIncome = transactionAmountAsMoney.zero();
            Money interestComponent = transactionAmountAsMoney;
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                Money receivableInterest = loan.getReceivableInterest(transactionDate);
                if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                    interestComponent = receivableInterest;
                    unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
                }
            }
            newTransactionDetail = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney, transactionDate,
                    interestComponent, unrecognizedIncome);
        }

        LocalDate recalculateFrom = null;

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = transactionToAdjust.getTransactionDate().isAfter(transactionDate) ? transactionDate
                    : transactionToAdjust.getTransactionDate();
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        final ChangedTransactionDetail changedTransactionDetail = loan.adjustExistingTransaction(newTransactionDetail,
                defaultLoanLifecycleStateMachine(), transactionToAdjust, existingTransactionIds, existingReversedTransactionIds,
                scheduleGeneratorDTO, reversalExternalId);

        if (newTransactionDetail.isGreaterThanZero(loan.getPrincipal().getCurrency())) {
            if (paymentDetail != null) {
                this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
            }
            this.loanTransactionRepository.saveAndFlush(newTransactionDetail);
        }

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a adjustment is made
         * before the latest payment recorded against the loan)
         ***/
        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.addLoanTransaction(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note;
            /**
             * If a new transaction is not created, associate note with the transaction to be adjusted
             **/
            if (newTransactionDetail.isGreaterThanZero(loan.getPrincipal().getCurrency())) {
                note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
            } else {
                note = Note.loanTransactionNote(loan, transactionToAdjust, noteText);
            }
            this.noteRepository.save(note);
        }

        Collection<Long> transactionIds = new ArrayList<>();
        List<LoanTransaction> transactions = loan.getLoanTransactions();
        for (LoanTransaction transaction : transactions) {
            if (transaction.isRefund() && transaction.isNotReversed()) {
                transactionIds.add(transaction.getId());
            }
        }

        if (!transactionIds.isEmpty()) {
            this.accountTransfersWritePlatformService.reverseTransfersWithFromAccountTransactions(transactionIds,
                    PortfolioAccountType.LOAN);
            loan.updateLoanSummaryAndStatus();
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        this.loanAccountDomainService.recalculateAccruals(loan);

        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        LoanAdjustTransactionBusinessEvent.Data eventData = new LoanAdjustTransactionBusinessEvent.Data(transactionToAdjust);
        if (newTransactionDetail.isRepaymentType() && newTransactionDetail.isGreaterThanZero(loan.getPrincipal().getCurrency())) {
            eventData.setNewTransactionDetail(newTransactionDetail);
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(eventData));

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(transactionId)
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult chargebackLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {
        this.loanEventApiJsonValidator.validateChargebackTransaction(command.json());

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.LOAN)) {
            throw new PlatformServiceUnavailableException("error.msg.loan.transfer.transaction.update.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as it involves in account transfer", transactionId);
        }
        if (loan.isClosedWrittenOff()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as loan status is written off", transactionId);
        }
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as loan product is interest recalculation enabled",
                    transactionId);
        }

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        checkClientOrGroupActive(loan);

        LoanTransaction loanTransaction = this.loanTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(transactionId));

        if (loanTransaction.isReversed()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + transactionId + " chargeback not allowed as loan transaction repayment is reversed",
                    transactionId);
        }

        if (!loanTransaction.isRepayment()) {
            throw new PlatformServiceUnavailableException(
                    "error.msg.loan.chargeback.operation.not.allowed", "Loan transaction:" + transactionId
                            + " chargeback not allowed as loan transaction is not repayment, is " + loanTransaction.getTypeOf().getCode(),
                    transactionId);
        }

        businessEventNotifierService.notifyPreBusinessEvent(new LoanChargebackTransactionBusinessEvent(loanTransaction));

        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.TRANSACTION_AMOUNT_PARAMNAME);
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull(LoanApiConstants.externalIdParameterName);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionAmount", command.stringValueOfParameterNamed(LoanApiConstants.TRANSACTION_AMOUNT_PARAMNAME));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        changes.put("paymentTypeId", command.stringValueOfParameterNamed(LoanApiConstants.PAYMENT_TYPE_PARAMNAME));

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        LoanTransaction newTransaction = LoanTransaction.chargeback(loan.getOffice(), transactionAmountAsMoney, paymentDetail,
                transactionDate, txnExternalId);

        validateLoanTransactionAmountChargeBack(loanTransaction, newTransaction);

        this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);

        // Store the Loan Transaction Relation
        LoanTransactionRelation loanTransactionRelation = LoanTransactionRelation.instance(loanTransaction, newTransaction,
                LoanTransactionRelationTypeEnum.CHARGEBACK);
        this.loanTransactionRelationRepository.save(loanTransactionRelation);

        this.loanTransactionRepository.save(newTransaction);

        loan.handleChargebackTransaction(newTransaction, defaultLoanLifecycleStateMachine());

        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed(LoanApiConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note = Note.loanTransactionNote(loan, newTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargebackTransactionBusinessEvent(loanTransaction));

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(transactionId)
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .with(changes).build();
    }

    private void validateLoanTransactionAmountChargeBack(LoanTransaction loanTransaction, LoanTransaction chargebackTransaction) {
        BigDecimal actualAmount = BigDecimal.ZERO;
        for (LoanTransactionRelation loanTransactionRelation : loanTransaction.getLoanTransactionRelations()) {
            if (loanTransactionRelation.getRelationType().equals(LoanTransactionRelationTypeEnum.CHARGEBACK)) {
                actualAmount = actualAmount.add(loanTransactionRelation.getToTransaction().getPrincipalPortion());
            }
        }
        actualAmount = actualAmount.add(chargebackTransaction.getAmount());
        if (loanTransaction.getPrincipalPortion() != null && actualAmount.compareTo(loanTransaction.getPrincipalPortion()) > 0) {
            throw new PlatformServiceUnavailableException("error.msg.loan.chargeback.operation.not.allowed",
                    "Loan transaction:" + loanTransaction.getId() + " chargeback not allowed as loan transaction amount is not enough",
                    loanTransaction.getId());
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveInterestOnLoan(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateTransaction(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        Money unrecognizedIncome = transactionAmountAsMoney.zero();
        Money interestComponent = transactionAmountAsMoney;
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Money receivableInterest = loan.getReceivableInterest(transactionDate);
            if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                interestComponent = receivableInterest;
                unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
            }
        }
        final LoanTransaction waiveInterestTransaction = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney,
                transactionDate, interestComponent, unrecognizedIncome);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWaiveInterestBusinessEvent(waiveInterestTransaction));
        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = transactionDate;
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        final ChangedTransactionDetail changedTransactionDetail = loan.waiveInterest(waiveInterestTransaction,
                defaultLoanLifecycleStateMachine(), existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO);

        this.loanTransactionRepository.saveAndFlush(waiveInterestTransaction);

        /***
         * TODO Vishwas Batch save is giving me a HibernateOptimisticLockingFailureException, looping and saving for the
         * time being, not a major issue for now as this loop is entered only in edge cases (when a waiver is made
         * before the latest payment recorded against the loan)
         ***/
        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created transactions
                loan.addLoanTransaction(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, waiveInterestTransaction, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        loanAccountDomainService.recalculateAccruals(loan);
        loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveInterestBusinessEvent(waiveInterestTransaction));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(waiveInterestTransaction.getId())
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult writeOff(final Long loanId, final JsonCommand command) {
        final AppUser currentUser = getAppUserIfPresent();

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        if (command.hasParameter("writeoffReasonId")) {
            Long writeoffReasonId = command.longValueOfParameterNamed("writeoffReasonId");
            CodeValue writeoffReason = this.codeValueRepository
                    .findOneByCodeNameAndIdWithNotFoundDetection(LoanApiConstants.WRITEOFFREASONS, writeoffReasonId);
            changes.put("writeoffReasonId", writeoffReasonId);
            loan.updateWriteOffReason(writeoffReason);
        }

        checkClientOrGroupActive(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWrittenOffPreBusinessEvent(loan));
        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.WRITE_OFF.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        removeLoanCycle(loan);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        updateLoanCounters(loan, loan.getDisbursementDate());

        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = command.localDateValueOfParameterNamed("transactionDate");
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        final ChangedTransactionDetail changedTransactionDetail = loan.closeAsWrittenOff(command, defaultLoanLifecycleStateMachine(),
                changes, existingTransactionIds, existingReversedTransactionIds, currentUser, scheduleGeneratorDTO);
        LoanTransaction writeOff = changedTransactionDetail.getNewTransactionMappings().remove(0L);
        this.loanTransactionRepository.saveAndFlush(writeOff);
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            this.loanTransactionRepository.save(mapEntry.getValue());
            this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
        }
        saveLoanWithDataIntegrityViolationChecks(loan);
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, writeOff, noteText);
            this.noteRepository.save(note);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        loanAccountDomainService.recalculateAccruals(loan);
        loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanWrittenOffPostBusinessEvent(writeOff));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(writeOff.getId())
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult closeLoan(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanCloseBusinessEvent(loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        updateLoanCounters(loan, loan.getDisbursementDate());

        LocalDate recalculateFrom = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculateFrom = command.localDateValueOfParameterNamed("transactionDate");
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        ChangedTransactionDetail changedTransactionDetail = loan.close(command, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO);
        final LoanTransaction possibleClosingTransaction = changedTransactionDetail.getNewTransactionMappings().remove(0L);
        if (possibleClosingTransaction != null) {
            this.loanTransactionRepository.saveAndFlush(possibleClosingTransaction);
        }
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            this.loanTransactionRepository.save(mapEntry.getValue());
            this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
        }
        saveLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }

        if (possibleClosingTransaction != null) {
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }
        loanAccountDomainService.recalculateAccruals(loan);

        loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanCloseBusinessEvent(loan));

        // Update loan transaction on repayment.
        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();

                if (loan.getStatus().isClosed()) {
                    loanCollateralManagement.setIsReleased(true);
                    BigDecimal quantity = loanCollateralManagement.getQuantity();
                    clientCollateralManagement.updateQuantity(clientCollateralManagement.getQuantity().add(quantity));
                    loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
                }
            }
            this.loanAccountDomainService.updateLoanCollateralTransaction(loanCollateralManagements);
        }

        // disable all active standing instructions linked to the loan
        this.loanAccountDomainService.disableStandingInstructionsLinkedToClosedLoan(loan);

        CommandProcessingResult result;
        if (possibleClosingTransaction != null) {

            result = new CommandProcessingResultBuilder().withCommandId(command.commandId())
                    .withEntityId(possibleClosingTransaction.getId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                    .withGroupId(loan.getGroupId()).withLoanId(loanId).with(changes).build();
        } else {
            result = new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                    .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                    .with(changes).build();
        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult closeAsRescheduled(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateTransactionWithNoAmount(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        removeLoanCycle(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanCloseAsRescheduleBusinessEvent(loan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        loan.closeAsMarkedForReschedule(command, defaultLoanLifecycleStateMachine(), changes);

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
        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();

                if (loan.getStatus().isClosed()) {
                    loanCollateralManagement.setIsReleased(true);
                    BigDecimal quantity = loanCollateralManagement.getQuantity();
                    clientCollateralManagement.updateQuantity(clientCollateralManagement.getQuantity().add(quantity));
                    loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
                }
            }
            this.loanAccountDomainService.updateLoanCollateralTransaction(loanCollateralManagements);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void validateAddingNewChargeAllowed(List<LoanDisbursementDetails> loanDisburseDetails) {
        boolean pendingDisbursementAvailable = false;
        for (LoanDisbursementDetails disbursementDetail : loanDisburseDetails) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                pendingDisbursementAvailable = true;
                break;
            }
        }
        if (!pendingDisbursementAvailable) {
            throw new ChargeCannotBeUpdatedException("error.msg.charge.cannot.be.updated.no.pending.disbursements.in.loan",
                    "This charge cannot be added, No disbursement is pending");
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult addLoanCharge(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateAddLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        List<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
        final Long chargeDefinitionId = command.longValueOfParameterNamed("chargeId");
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        if (loan.isDisbursed() && chargeDefinition.isDisbursementCharge()) {
            // validates whether any pending disbursements are available to
            // apply this charge
            validateAddingNewChargeAllowed(loanDisburseDetails);
        }
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        boolean isAppliedOnBackDate = false;
        LoanCharge loanCharge = null;
        LocalDate recalculateFrom = loan.fetchInterestRecalculateFromDate();
        if (chargeDefinition.isPercentageOfDisbursementAmount()) {
            LoanTrancheDisbursementCharge loanTrancheDisbursementCharge;
            for (LoanDisbursementDetails disbursementDetail : loanDisburseDetails) {
                if (disbursementDetail.actualDisbursementDate() == null) {
                    loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, disbursementDetail.principal(), null, null, null,
                            disbursementDetail.expectedDisbursementDateAsLocalDate(), null, null);
                    loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, disbursementDetail);
                    loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                    businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
                    validateAddLoanCharge(loan, chargeDefinition, loanCharge);
                    addCharge(loan, chargeDefinition, loanCharge);
                    isAppliedOnBackDate = true;
                    if (recalculateFrom.isAfter(disbursementDetail.expectedDisbursementDateAsLocalDate())) {
                        recalculateFrom = disbursementDetail.expectedDisbursementDateAsLocalDate();
                    }
                }
            }
            if (loanCharge == null) {
                final String errorMessage = "Charge with identifier " + chargeDefinition.getId()
                        + " cannot be applied: No valid loan disbursement available";
                throw new ChargeCannotBeAppliedToException("loan", errorMessage, chargeDefinition.getId());
            }
            loan.addTrancheLoanCharge(chargeDefinition);
        } else {
            loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command);
            businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));

            validateAddLoanCharge(loan, chargeDefinition, loanCharge);
            isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
            if (loanCharge.getDueLocalDate() == null || recalculateFrom.isAfter(loanCharge.getDueLocalDate())) {
                isAppliedOnBackDate = true;
                recalculateFrom = loanCharge.getDueLocalDate();
            }
        }

        boolean reprocessRequired = true;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            if (isAppliedOnBackDate && loan.isFeeCompoundingEnabledForInterestRecalculation()) {

                runScheduleRecalculation(loan, recalculateFrom);
                reprocessRequired = false;
            }
            updateOriginalSchedule(loan);
        }
        if (reprocessRequired) {
            ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.addLoanTransaction(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }
            saveLoanWithDataIntegrityViolationChecks(loan);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && isAppliedOnBackDate
                && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
            this.loanAccountDomainService.recalculateAccruals(loan);
        }
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanCharge.getId())
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .build();
    }

    private void validateAddLoanCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {
        if (chargeDefinition.isOverdueInstallment()) {
            final String defaultUserMessage = "Installment charge cannot be added to the loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "overdue.charge", defaultUserMessage, null,
                    chargeDefinition.getName());
        } else if (loanCharge.getDueLocalDate() != null
                && loanCharge.getDueLocalDate().isBefore(loan.getLastUserTransactionForChargeCalc())) {
            final String defaultUserMessage = "charge with date before last transaction date can not be added to loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "date.is.before.last.transaction.date", defaultUserMessage, null,
                    chargeDefinition.getName());
        } else if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {

            if (loanCharge.isInstalmentFee() && loan.getStatus().isActive()) {
                final String defaultUserMessage = "installment charge addition not allowed after disbursement";
                throw new LoanChargeCannotBeAddedException("loanCharge", "installment.charge", defaultUserMessage, null,
                        chargeDefinition.getName());
            }
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final Set<LoanCharge> loanCharges = new HashSet<>(1);
            loanCharges.add(loanCharge);
            this.loanApplicationCommandFromApiJsonHelper.validateLoanCharges(loanCharges, dataValidationErrors);
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
    }

    public void runScheduleRecalculation(final Loan loan, final LocalDate recalculateFrom) {
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            ScheduleGeneratorDTO generatorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            ChangedTransactionDetail changedTransactionDetail = loan
                    .handleRegenerateRepaymentScheduleWithInterestRecalculation(generatorDTO);
            saveLoanWithDataIntegrityViolationChecks(loan);
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.addLoanTransaction(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }
    }

    public void updateOriginalSchedule(Loan loan) {
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            final LocalDate recalculateFrom = null;
            ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }
    }

    private boolean addCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {

        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            final String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
        }

        if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                    .retriveLoanLinkedAssociation(loan.getId());
            if (portfolioAccountData == null) {
                final String errorMessage = loanCharge.name() + "Charge  requires linked savings account for payment";
                throw new LinkedAccountRequiredException("loanCharge.add", errorMessage, loanCharge.name());
            }
        }

        if (!loan.isInterestBearing() && loanCharge.isSpecifiedDueDate() && loanCharge.getDueDate().isAfter(loan.getMaturityDate())) {
            LoanRepaymentScheduleInstallment latestRepaymentScheduleInstalment = loan.getRepaymentScheduleInstallments()
                    .get(loan.getLoanRepaymentScheduleInstallmentsSize() - 1);
            if (loanCharge.getDueDate().isAfter(latestRepaymentScheduleInstalment.getDueDate())) {
                if (latestRepaymentScheduleInstalment.isAdditional()) {
                    latestRepaymentScheduleInstalment.updateDueDate(loanCharge.getDueDate());
                } else {
                    final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan,
                            (loan.getLoanRepaymentScheduleInstallmentsSize() + 1), latestRepaymentScheduleInstalment.getDueDate(),
                            loanCharge.getDueDate(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null);
                    installment.markAsAdditional();
                    loan.addLoanRepaymentScheduleInstallment(installment);
                }
            }
        }

        loan.addLoanCharge(loanCharge);

        this.loanChargeRepository.saveAndFlush(loanCharge);

        /**
         * we want to apply charge transactions only for those loans charges that are applied when a loan is active and
         * the loan product uses Upfront Accruals
         **/
        if (loan.getStatus().isActive() && loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
            final LoanTransaction applyLoanChargeTransaction = loan.handleChargeAppliedTransaction(loanCharge, null);
            this.loanTransactionRepository.saveAndFlush(applyLoanChargeTransaction);
        }
        return loanCharge.getDueLocalDate() == null || DateUtils.getBusinessLocalDate().isAfter(loanCharge.getDueLocalDate());
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateUpdateOfLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be edited only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.getStatus().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeUpdatedException(LoanChargeCannotBeUpdatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUpdateChargeBusinessEvent(loanCharge));

        final Map<String, Object> changes = loan.updateLoanCharge(loanCharge, command);

        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult loanChargeRefund(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateLoanChargeRefundTransaction(command.json());

        final Long loanChargeId = command.longValueOfParameterNamed("loanChargeId");
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final LoanInstallmentCharge installmentChargeEntry = loanChargeRefundEntranceValidation(loanCharge, installmentNumber, dueDate);
        Integer installmentNumberIdentified = null;
        if (installmentChargeEntry != null) {
            installmentNumberIdentified = installmentChargeEntry.getRepaymentInstallment().getInstallmentNumber();
        }
        final BigDecimal fullRefundAbleAmount = loanChargeValidateRefundAmount(loanCharge, installmentChargeEntry, transactionAmount);

        JsonCommand repaymentJsonCommand = adaptLoanChargeRefundCommandForFurtherRepaymentProcessing(command, fullRefundAbleAmount);

        boolean isRecoveryRepayment = false;
        String chargeRefundChargeType = "F";
        if (loanCharge.isPenaltyCharge()) {
            chargeRefundChargeType = "P";
        }
        // chargeRefundChargeType only included as a parameter for accounting reason - in order to identify whether fee
        // or penalty GL account is relevant
        CommandProcessingResult result = makeLoanRepaymentWithChargeRefundChargeType(LoanTransactionType.CHARGE_REFUND,
                repaymentJsonCommand.getLoanId(), repaymentJsonCommand, isRecoveryRepayment, chargeRefundChargeType);

        Long loanChargeRefundTransactionId = result.getResourceId();
        LoanTransaction newChargeRefundTxn = null;
        for (LoanTransaction chargeRefundTxn : loanCharge.getLoan().getLoanTransactions()) {
            if (loanChargeRefundTransactionId.equals(chargeRefundTxn.getId())) {
                newChargeRefundTxn = chargeRefundTxn;
                final BigDecimal appliedRefundAmount = newChargeRefundTxn.getAmount(loanCharge.getLoan().getCurrency()).getAmount()
                        .multiply(BigDecimal.valueOf(-1));
                final LoanChargePaidBy loanChargePaidByForChargeRefund = new LoanChargePaidBy(newChargeRefundTxn, loanCharge,
                        appliedRefundAmount, installmentNumberIdentified);
                newChargeRefundTxn.getLoanChargesPaid().add(loanChargePaidByForChargeRefund);
                loanCharge.getLoanChargePaidBySet().add(loanChargePaidByForChargeRefund);
                break;
            }
        }

        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(newChargeRefundTxn.getLoan()));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargeRefundBusinessEvent(newChargeRefundTxn));
        return result;
    }

    private JsonCommand adaptLoanChargeRefundCommandForFurtherRepaymentProcessing(JsonCommand command, BigDecimal fullRefundAbleAmount) {
        // creates JsonCommand for onward repayment processing
        JsonObject jsonObject = (JsonObject) this.fromApiJsonHelper.parse(command.json());

        String dateFormat;
        if (this.fromApiJsonHelper.parameterExists("dateFormat", jsonObject)) {
            dateFormat = this.fromApiJsonHelper.extractStringNamed("dateFormat", jsonObject);
        } else {
            dateFormat = "dd MMMM yyyy";
            jsonObject.addProperty("dateFormat", dateFormat);
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate transactionDate = DateUtils.getLocalDateOfTenant();
        String transactionDateString = transactionDate.format(dateTimeFormatter);
        jsonObject.addProperty("transactionDate", transactionDateString);
        if (!this.fromApiJsonHelper.parameterExists("transactionAmount", jsonObject)) {
            jsonObject.addProperty("transactionAmount", fullRefundAbleAmount.toString());
        }
        jsonObject.remove("loanChargeId");
        jsonObject.remove("installmentNumber");
        jsonObject.remove("dueDate");

        return JsonCommand.fromExistingCommand(command, jsonObject);
    }

    private BigDecimal loanChargeValidateRefundAmount(LoanCharge loanCharge, LoanInstallmentCharge installmentChargeEntry,
            BigDecimal transactionAmount) {
        // if transactionAmount not provided return max refundable amount (amount paid minus previous refunds)
        BigDecimal chargeAmountPaid;
        BigDecimal chargeAmountRefunded = BigDecimal.ZERO;
        MonetaryCurrency loanCurrency = loanCharge.getLoan().getCurrency();
        if (loanCharge.isInstalmentFee()) {
            final Integer installmentNumber = installmentChargeEntry.getRepaymentInstallment().getInstallmentNumber();
            chargeAmountPaid = installmentChargeEntry.getAmountPaid(loanCurrency).getAmount();
            for (LoanChargePaidBy loanChargePaidBy : loanCharge.getLoanChargePaidBySet()) {
                if (installmentNumber.equals(loanChargePaidBy.getInstallmentNumber())) {
                    if (isRefundElementOfChargeRefund(loanChargePaidBy)) {
                        chargeAmountRefunded = chargeAmountRefunded.add(loanChargePaidBy.getAmount());
                    }
                }
            }
        } else {
            chargeAmountPaid = loanCharge.getAmountPaid(loanCurrency).getAmount();
            for (LoanChargePaidBy loanChargePaidBy : loanCharge.getLoanChargePaidBySet()) {
                if (isRefundElementOfChargeRefund(loanChargePaidBy)) {
                    chargeAmountRefunded = chargeAmountRefunded.add(loanChargePaidBy.getAmount());
                }
            }
        }
        chargeAmountRefunded = chargeAmountRefunded.multiply(BigDecimal.valueOf(-1));

        if (chargeAmountRefunded.compareTo(chargeAmountPaid) > 0) {
            final String errorMessage = "loan.charge.more.refunded.than.paid.unexpected.system.error";
            final String details = "Paid: " + chargeAmountPaid.toString() + "  Refunded: " + chargeAmountPaid;
            throw new LoanChargeRefundException(errorMessage, details);
        }

        BigDecimal refundableAmount = chargeAmountPaid.subtract(chargeAmountRefunded);
        if (transactionAmount != null) { // refund amount was provided.
            if (transactionAmount.compareTo(refundableAmount) > 0) {
                final String errorMessage = "loan.charge.transaction.amount.is.more.than.is.refundable";
                final String details = "transactionAmount: " + transactionAmount + "  Refundable: " + refundableAmount;
                throw new LoanChargeRefundException(errorMessage, details);
            }
        }

        return refundableAmount;
    }

    private boolean isRefundElementOfChargeRefund(LoanChargePaidBy loanChargePaidBy) {
        // The Refund Element is always negative
        return (loanChargePaidBy.getLoanTransaction().isChargeRefund() && loanChargePaidBy.getAmount().compareTo(BigDecimal.ZERO) < 0);
    }

    private LoanInstallmentCharge loanChargeRefundEntranceValidation(LoanCharge loanCharge, Integer installmentNumber, LocalDate dueDate) {

        LoanInstallmentCharge installmentChargeEntry = null;

        Loan loan = loanCharge.getLoan();
        if (!(loan.isOpen() || loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid())) {
            final String errorMessage = "loan.charge.refund.invalid.status";
            throw new LoanChargeRefundException(errorMessage, loan.getStatus().toString());
        }

        if (dueDate != null && installmentNumber != null) {
            throwLoanChargeRefundException("loan.charge.refund.dueDate.and.installmentNumber.provided.use.only.one", installmentNumber,
                    dueDate);
        }

        if (loanCharge.isInstalmentFee()) { // identify specific installment
            if (dueDate == null && installmentNumber == null) {
                throwLoanChargeRefundException(
                        "loan.charge.refund.neither.dueDate.nor.installmentNumber.provided.for.this.installment.charge", installmentNumber,
                        dueDate);
            }

            if (dueDate != null) {
                installmentChargeEntry = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                installmentChargeEntry = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }

            if (installmentChargeEntry == null) {
                throwLoanChargeRefundException("loan.charge.refund.installment.not.found", installmentNumber, dueDate);
            }
        } else {

            if (dueDate != null || installmentNumber != null) {
                throwLoanChargeRefundException(
                        "loan.charge.refund.dueDate.or.installmentNumber.provided.but.this.is.not.an.installment.charge", installmentNumber,
                        dueDate);
            }
        }

        return installmentChargeEntry;
    }

    private void throwLoanChargeRefundException(String errorMessage, Integer installmentNumber, LocalDate dueDate) {
        String dueDateValue = "";
        String installmentNumberValue = "";
        if (dueDate != null) {
            dueDateValue = dueDate.toString();
        }
        if (installmentNumber != null) {
            installmentNumberValue = installmentNumber.toString();
        }
        throw new LoanChargeRefundException(errorMessage, "dueDate: " + dueDateValue + "  installmentNumber: " + installmentNumberValue);
    }

    @Transactional
    @Override
    public CommandProcessingResult undoWaiveLoanCharge(final JsonCommand command) {

        LoanTransaction loanTransaction = this.loanTransactionRepository.findById(command.entityId())
                .orElseThrow(() -> new LoanTransactionNotFoundException(command.entityId()));

        if (!loanTransaction.getTypeOf().getCode().equals(LoanTransactionType.WAIVE_CHARGES.getCode())) {
            throw new InvalidLoanTransactionTypeException("Undo Waive Charge", "Waive an Installment Charge First",
                    "Transaction is not a waive charge type.");
        }

        Set<LoanChargePaidBy> loanChargePaidBySet = loanTransaction.getLoanChargesPaid();
        Integer installmentNumber = null;
        Long loanChargeId = null;
        final Long loanId = loanTransaction.getLoan().getId();

        for (LoanChargePaidBy loanChargePaidBy : loanChargePaidBySet) {
            installmentNumber = loanChargePaidBy.getInstallmentNumber();
            loanChargeId = loanChargePaidBy.getLoanCharge().getId();
            break;
        }

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.LOAN_INACTIVE, loanCharge.getId());
        }

        // Validate loan charge is not already paid
        if (loanCharge.isPaid()) {
            throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.ALREADY_PAID, loanCharge.getId());
        }

        final Map<String, Object> changes = new LinkedHashMap<>(3);

        businessEventNotifierService.notifyPreBusinessEvent(new LoanWaiveChargeUndoBusinessEvent(loanCharge));

        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment;

            // final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (installmentNumber != null) {

                // Get installment charge.
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);

                if (!loanTransaction.isNotReversed()) {
                    throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.ALREADY_REVERSED,
                            loanTransaction.getId());
                }

                // Reverse waived transaction
                loanTransaction.setReversed();

                // Get installment amount waived.
                BigDecimal amountWaived = chargePerInstallment.getAmountWaived(loan.getCurrency()).getAmount();

                // Set manually adjusted value to `1`
                loanTransaction.setManuallyAdjustedOrReversed();

                // Save updated data
                this.loanTransactionRepository.saveAndFlush(loanTransaction);

                // Get installment outstanding amount
                BigDecimal amountOutstandingPerInstallment = chargePerInstallment.getAmountOutstanding();

                // Check whether the installment charge is not waived. If so throw new error
                if (!chargePerInstallment.isWaived() || amountWaived == null) {
                    throw new LoanChargeWaiveCannotBeReversedException(LoanChargeWaiveCannotUndoReason.NOT_WAIVED, loanChargeId);
                }

                // Get loan charge total amount waived
                BigDecimal totalAmountWaved = loanCharge.getAmountWaived(loan.getCurrency()).getAmount();

                // Get loan charge outstanding amount
                BigDecimal amountOutstanding = loanCharge.getAmountOutstanding(loan.getCurrency()).getAmount();

                // Add the amount waived to outstanding amount
                loanCharge.resetOutstandingAmount(amountOutstanding.add(amountWaived));

                // Subtract the amount waived from the existing amount waived.
                loanCharge.setAmountWaived(totalAmountWaved.subtract(amountWaived));

                // Add the amount waived to the outstanding amount of the installment
                chargePerInstallment.resetOutstandingAmount(amountOutstandingPerInstallment.add(amountWaived));

                // Set the amount waived value to ZERO
                chargePerInstallment.resetAmountWaived(BigDecimal.ZERO);

                // Reset waived flag
                chargePerInstallment.undoWaiveFlag();

                // Get the fee charges waived amount per installment
                BigDecimal feeChargesWaivedAmount = chargePerInstallment.getInstallment().getFeeChargesWaived(loan.getCurrency())
                        .getAmount();

                // Subtract the amount waived from the existing fee charges waived amount.
                chargePerInstallment.getInstallment().setFeeChargesWaived(feeChargesWaivedAmount.subtract(amountWaived));

                // Update loan charge.
                loanCharge.setInstallmentLoanCharge(chargePerInstallment, chargePerInstallment.getInstallment().getInstallmentNumber());

                if (loanCharge.getAmount(loan.getCurrency()).compareTo(loanCharge.getAmountOutstanding(loan.getCurrency())) == 0
                        && loanCharge.isWaived()) {
                    loanCharge.undoWaived();
                }

                this.loanChargeRepository.saveAndFlush(loanCharge);

                loan.updateLoanSummaryForUndoWaiveCharge(amountWaived);

                changes.put("amount", amountWaived);

            } else {
                throw new InstallmentNotFoundException(command.entityId());
            }
        }

        saveLoanWithDataIntegrityViolationChecks(loan);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveChargeUndoBusinessEvent(loanCharge));

        LoanTransaction loanTransactionData = this.loanTransactionRepository.getReferenceById(command.entityId());
        changes.put("principalPortion", loanTransactionData.getPrincipalPortion());
        changes.put("interestPortion", loanTransactionData.getInterestPortion(loan.getCurrency()));
        changes.put("feeChargesPortion", loanTransactionData.getFeeChargesPortion(loan.getCurrency()));
        changes.put("penaltyChargesPortion", loanTransactionData.getPenaltyChargesPortion(loan.getCurrency()));
        changes.put("outstandingLoanBalance", loanTransactionData.getOutstandingLoanBalance());
        changes.put("id", loanTransactionData.getId());
        changes.put("date", loanTransactionData.getTransactionDate());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withLoanId(loanId) //
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.loanEventApiJsonValidator.validateInstallmentChargeTransaction(command.json());
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedReason.LOAN_INACTIVE, loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedReason.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedReason.ALREADY_PAID, loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWaiveChargeBusinessEvent(loanCharge));
        Integer loanInstallmentNumber = null;
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            if (!StringUtils.isBlank(command.json())) {
                final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
                final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
                if (dueDate != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
                } else if (installmentNumber != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
                }
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
            }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
        }

        final Map<String, Object> changes = new LinkedHashMap<>(3);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        Money accruedCharge = Money.zero(loan.getCurrency());
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Collection<LoanChargePaidByData> chargePaidByCollection = this.loanChargeReadPlatformService
                    .retriveLoanChargesPaidBy(loanCharge.getId(), LoanTransactionType.ACCRUAL, loanInstallmentNumber);
            for (LoanChargePaidByData chargePaidByData : chargePaidByCollection) {
                accruedCharge = accruedCharge.plus(chargePaidByData.getAmount());
            }
        }

        final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine(), changes,
                existingTransactionIds, existingReversedTransactionIds, loanInstallmentNumber, scheduleGeneratorDTO, accruedCharge);

        this.loanTransactionRepository.saveAndFlush(waiveTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveChargeBusinessEvent(loanCharge));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be deleted only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.getStatus().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeDeletedException(LoanChargeCannotBeDeletedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanDeleteChargeBusinessEvent(loanCharge));

        loan.removeLoanCharge(loanCharge);
        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanDeleteChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult payLoanCharge(final Long loanId, Long loanChargeId, final JsonCommand command,
            final boolean isChargeIdIncludedInJson) {

        this.loanEventApiJsonValidator.validateChargePaymentTransaction(command.json(), isChargeIdIncludedInJson);
        if (isChargeIdIncludedInJson) {
            loanChargeId = command.longValueOfParameterNamed("chargeId");
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.LOAN_INACTIVE, loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
        }

        if (!loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.CHARGE_NOT_ACCOUNT_TRANSFER, loanCharge.getId());
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        Integer loanInstallmentNumber = null;
        BigDecimal amount = loanCharge.amountOutstanding();
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
            final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (dueDate != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_WAIVED, loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedReason.ALREADY_PAID, loanCharge.getId());
            }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
            amount = chargePerInstallment.getAmountOutstanding();
        }

        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
        if (portfolioAccountData == null) {
            final String errorMessage = "Charge with id:" + loanChargeId + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loanCharge.pay", errorMessage, loanChargeId);
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.SAVINGS,
                PortfolioAccountType.LOAN, portfolioAccountData.getId(), loanId, "Loan Charge Payment", locale, fmt, null, null,
                LoanTransactionType.CHARGE_PAYMENT.getValue(), loanChargeId, loanInstallmentNumber,
                AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withSavingsId(portfolioAccountData.getId()).build();
    }

    private void disburseLoanToLoan(final Loan loan, final JsonCommand command, final BigDecimal amount) {

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.LOAN,
                PortfolioAccountType.LOAN, loan.getId(), loan.getTopupLoanDetails().getLoanIdToClose(), "Loan Topup", locale, fmt,
                LoanTransactionType.DISBURSEMENT.getValue(), LoanTransactionType.REPAYMENT.getValue(), txnExternalId, loan, null);
        AccountTransferDetails accountTransferDetails = this.accountTransfersWritePlatformService.repayLoanWithTopup(accountTransferDTO);
        loan.getTopupLoanDetails().setAccountTransferDetails(accountTransferDetails.getId());
        loan.getTopupLoanDetails().setTopupAmount(amount);
    }

    private void disburseLoanToSavings(final Loan loan, final JsonCommand command, final Money amount, final PaymentDetail paymentDetail) {

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                .retriveLoanLinkedAssociation(loan.getId());
        if (portfolioAccountData == null) {
            final String errorMessage = "Disburse Loan with id:" + loan.getId() + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loan.disburse.to.savings", errorMessage, loan.getId());
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isExceptionForBalanceCheck = false;
        final boolean isRegularTransaction = true;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount.getAmount(), PortfolioAccountType.LOAN,
                PortfolioAccountType.SAVINGS, loan.getId(), portfolioAccountData.getId(), "Loan Disbursement", locale, fmt, paymentDetail,
                LoanTransactionType.DISBURSEMENT.getValue(), null, null, null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null,
                txnExternalId, loan, null, fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);

    }

    private LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
        final LoanCharge loanCharge = this.loanChargeRepository.findById(loanChargeId)
                .orElseThrow(() -> new LoanChargeNotFoundException(loanChargeId));

        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) {
            throw new LoanChargeNotFoundException(loanChargeId, loanId);
        }
        return loanCharge;
    }

    @Transactional
    @Override
    public LoanTransaction initiateLoanTransfer(final Loan loan, final LocalDate transferDate) {

        this.loanAssembler.setHelpers(loan);
        checkClientOrGroupActive(loan);
        validateTransactionsForTransfer(loan, transferDate);

        businessEventNotifierService.notifyPreBusinessEvent(new LoanInitiateTransferBusinessEvent(loan));

        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferTransaction = LoanTransaction.initiateTransfer(loan.getOffice(), loan, transferDate);
        loan.addLoanTransaction(newTransferTransaction);
        LoanLifecycleStateMachine loanLifecycleStateMachine = defaultLoanLifecycleStateMachine();
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_INITIATE_TRANSFER, loan);

        this.loanTransactionRepository.saveAndFlush(newTransferTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanInitiateTransferBusinessEvent(loan));
        return newTransferTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction acceptLoanTransfer(final Loan loan, final LocalDate transferDate, final Office acceptedInOffice,
            final Staff loanOfficer) {
        this.loanAssembler.setHelpers(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanAcceptTransferBusinessEvent(loan));
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.approveTransfer(acceptedInOffice, loan, transferDate);
        loan.addLoanTransaction(newTransferAcceptanceTransaction);
        LoanLifecycleStateMachine loanLifecycleStateMachine = defaultLoanLifecycleStateMachine();
        if (loan.getTotalOverpaid() != null) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_OVERPAYMENT, loan);
        } else {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, loan);
        }
        if (loanOfficer != null) {
            loan.reassignLoanOfficer(loanOfficer, transferDate);
        }

        this.loanTransactionRepository.saveAndFlush(newTransferAcceptanceTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAcceptTransferBusinessEvent(loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public LoanTransaction withdrawLoanTransfer(final Loan loan, final LocalDate transferDate) {
        this.loanAssembler.setHelpers(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWithdrawTransferBusinessEvent(loan));

        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        final LoanTransaction newTransferAcceptanceTransaction = LoanTransaction.withdrawTransfer(loan.getOffice(), loan, transferDate);
        loan.addLoanTransaction(newTransferAcceptanceTransaction);
        LoanLifecycleStateMachine loanLifecycleStateMachine = defaultLoanLifecycleStateMachine();
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_WITHDRAW_TRANSFER, loan);

        this.loanTransactionRepository.saveAndFlush(newTransferAcceptanceTransaction);
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanWithdrawTransferBusinessEvent(loan));

        return newTransferAcceptanceTransaction;
    }

    @Transactional
    @Override
    public void rejectLoanTransfer(final Loan loan) {
        this.loanAssembler.setHelpers(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanRejectTransferBusinessEvent(loan));
        LoanLifecycleStateMachine loanLifecycleStateMachine = defaultLoanLifecycleStateMachine();
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_REJECT_TRANSFER, loan);
        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRejectTransferBusinessEvent(loan));
    }

    @Transactional
    @Override
    public CommandProcessingResult loanReassignment(final Long loanId, final JsonCommand command) {

        this.loanEventApiJsonValidator.validateUpdateOfLoanOfficer(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);
        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));
        if (!loan.hasLoanOfficer(fromLoanOfficer)) {
            throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId);
        }

        loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);

        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult bulkLoanReassignment(final JsonCommand command) {

        this.loanEventApiJsonValidator.validateForBulkLoanReassignment(command.json());

        final Long fromLoanOfficerId = command.longValueOfParameterNamed("fromLoanOfficerId");
        final Long toLoanOfficerId = command.longValueOfParameterNamed("toLoanOfficerId");
        final String[] loanIds = command.arrayValueOfParameterNamed("loans");

        final LocalDate dateOfLoanOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        final Staff fromLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(fromLoanOfficerId);
        final Staff toLoanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(toLoanOfficerId);

        for (final String loanIdString : loanIds) {
            final Long loanId = Long.valueOf(loanIdString);
            final Loan loan = this.loanAssembler.assembleFrom(loanId);
            businessEventNotifierService.notifyPreBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));
            checkClientOrGroupActive(loan);

            if (!loan.hasLoanOfficer(fromLoanOfficer)) {
                throw new LoanOfficerAssignmentException(loanId, fromLoanOfficerId);
            }

            loan.reassignLoanOfficer(toLoanOfficer, dateOfLoanOfficerAssignment);
            saveLoanWithDataIntegrityViolationChecks(loan);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanReassignOfficerBusinessEvent(loan));
        }
        this.loanRepositoryWrapper.flush();

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

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        if (loan.getLoanOfficer() == null) {
            throw new LoanOfficerUnassignmentException(loanId);
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanRemoveOfficerBusinessEvent(loan));

        loan.removeLoanOfficer(dateOfLoanOfficerUnassigned);

        saveLoanWithDataIntegrityViolationChecks(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanRemoveOfficerBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = loan.getCurrency();
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
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
        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        final Collection<Integer> loanTypes = new ArrayList<>(Arrays.asList(AccountType.GROUP.getValue(), AccountType.JLG.getValue()));
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

                Boolean isSkipRepaymentOnFirstMonth = false;
                int numberOfDays = 0;
                boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
                if (isSkipRepaymentOnFirstMonthEnabled) {
                    isSkipRepaymentOnFirstMonth = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), calendar);
                    if (isSkipRepaymentOnFirstMonth) {
                        numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue();
                    }
                }

                holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan.getDisbursementDate());
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                    loan.setHelpers(null, this.loanSummaryWrapper, this.transactionProcessingStrategy);
                    loan.recalculateScheduleFromLastTransaction(scheduleGeneratorDTO, existingTransactionIds,
                            existingReversedTransactionIds);
                    createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
                } else if (rescheduleBasedOnMeetingDates != null && rescheduleBasedOnMeetingDates) {
                    loan.updateLoanRepaymentScheduleDates(calendar.getRecurrence(), isHolidayEnabled, holidays, workingDays,
                            presentMeetingDate, newMeetingDate, isSkipRepaymentOnFirstMonth, numberOfDays);
                } else {
                    loan.updateLoanRepaymentScheduleDates(calendar.getStartDateLocalDate(), calendar.getRecurrence(), isHolidayEnabled,
                            holidays, workingDays, isSkipRepaymentOnFirstMonth, numberOfDays);
                }

                saveLoanWithDataIntegrityViolationChecks(loan);
                businessEventNotifierService.notifyPostBusinessEvent(new LoanRescheduledDueCalendarChangeBusinessEvent(loan));
            }
        }
    }

    private void removeLoanCycle(final Loan loan) {
        final List<Loan> loansToUpdate;
        if (loan.isGroupLoan()) {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                loansToUpdate = this.loanRepositoryWrapper.getGroupLoansToUpdateLoanCounter(loan.getCurrentLoanCounter(), loan.getGroupId(),
                        AccountType.GROUP.getValue());
            } else {
                loansToUpdate = this.loanRepositoryWrapper.getGroupLoansToUpdateLoanProductCounter(loan.getLoanProductLoanCounter(),
                        loan.getGroupId(), AccountType.GROUP.getValue());
            }

        } else {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                loansToUpdate = this.loanRepositoryWrapper.getClientOrJLGLoansToUpdateLoanCounter(loan.getCurrentLoanCounter(),
                        loan.getClientId());
            } else {
                loansToUpdate = this.loanRepositoryWrapper.getClientLoansToUpdateLoanProductCounter(loan.getLoanProductLoanCounter(),
                        loan.getClientId());
            }

        }
        if (loansToUpdate != null) {
            updateLoanCycleCounter(loansToUpdate, loan);
        }
        loan.updateClientLoanCounter(null);
        loan.updateLoanProductLoanCounter(null);

    }

    private void updateLoanCounters(final Loan loan, final LocalDate actualDisbursementDate) {

        if (loan.isGroupLoan()) {
            final List<Loan> loansToUpdateForLoanCounter = this.loanRepositoryWrapper.getGroupLoansDisbursedAfter(actualDisbursementDate,
                    loan.getGroupId(), AccountType.GROUP.getValue());
            final Integer newLoanCounter = getNewGroupLoanCounter(loan);
            final Integer newLoanProductCounter = getNewGroupLoanProductCounter(loan);
            updateLoanCounter(loan, loansToUpdateForLoanCounter, newLoanCounter, newLoanProductCounter);
        } else {
            final List<Loan> loansToUpdateForLoanCounter = this.loanRepositoryWrapper
                    .getClientOrJLGLoansDisbursedAfter(actualDisbursementDate, loan.getClientId());
            final Integer newLoanCounter = getNewClientOrJLGLoanCounter(loan);
            final Integer newLoanProductCounter = getNewClientOrJLGLoanProductCounter(loan);
            updateLoanCounter(loan, loansToUpdateForLoanCounter, newLoanCounter, newLoanProductCounter);
        }
    }

    private Integer getNewGroupLoanCounter(final Loan loan) {

        Integer maxClientLoanCounter = this.loanRepositoryWrapper.getMaxGroupLoanCounter(loan.getGroupId(), AccountType.GROUP.getValue());
        if (maxClientLoanCounter == null) {
            maxClientLoanCounter = 1;
        } else {
            maxClientLoanCounter = maxClientLoanCounter + 1;
        }
        return maxClientLoanCounter;
    }

    private Integer getNewGroupLoanProductCounter(final Loan loan) {

        Integer maxLoanProductLoanCounter = this.loanRepositoryWrapper.getMaxGroupLoanProductCounter(loan.loanProduct().getId(),
                loan.getGroupId(), AccountType.GROUP.getValue());
        if (maxLoanProductLoanCounter == null) {
            maxLoanProductLoanCounter = 1;
        } else {
            maxLoanProductLoanCounter = maxLoanProductLoanCounter + 1;
        }
        return maxLoanProductLoanCounter;
    }

    private void updateLoanCounter(final Loan loan, final List<Loan> loansToUpdateForLoanCounter, Integer newLoanCounter,
            Integer newLoanProductCounter) {

        final boolean includeInBorrowerCycle = loan.loanProduct().isIncludeInBorrowerCycle();
        for (final Loan loanToUpdate : loansToUpdateForLoanCounter) {
            // Update client loan counter if loan product includeInBorrowerCycle
            // is true
            if (loanToUpdate.loanProduct().isIncludeInBorrowerCycle()) {
                Integer currentLoanCounter = loanToUpdate.getCurrentLoanCounter() == null ? 1 : loanToUpdate.getCurrentLoanCounter();
                if (newLoanCounter > currentLoanCounter) {
                    newLoanCounter = currentLoanCounter;
                }
                loanToUpdate.updateClientLoanCounter(++currentLoanCounter);
            }

            if (Objects.equals(loan.loanProduct().getId(), loanToUpdate.loanProduct().getId())) {
                Integer loanProductLoanCounter = loanToUpdate.getLoanProductLoanCounter();
                if (newLoanProductCounter > loanProductLoanCounter) {
                    newLoanProductCounter = loanProductLoanCounter;
                }
                loanToUpdate.updateLoanProductLoanCounter(++loanProductLoanCounter);
            }
        }

        if (includeInBorrowerCycle) {
            loan.updateClientLoanCounter(newLoanCounter);
        } else {
            loan.updateClientLoanCounter(null);
        }
        loan.updateLoanProductLoanCounter(newLoanProductCounter);
        this.loanRepositoryWrapper.save(loansToUpdateForLoanCounter);
    }

    private Integer getNewClientOrJLGLoanCounter(final Loan loan) {

        Integer maxClientLoanCounter = this.loanRepositoryWrapper.getMaxClientOrJLGLoanCounter(loan.getClientId());
        if (maxClientLoanCounter == null) {
            maxClientLoanCounter = 1;
        } else {
            maxClientLoanCounter = maxClientLoanCounter + 1;
        }
        return maxClientLoanCounter;
    }

    private Integer getNewClientOrJLGLoanProductCounter(final Loan loan) {

        Integer maxLoanProductLoanCounter = this.loanRepositoryWrapper.getMaxClientOrJLGLoanProductCounter(loan.loanProduct().getId(),
                loan.getClientId());
        if (maxLoanProductLoanCounter == null) {
            maxLoanProductLoanCounter = 1;
        } else {
            maxLoanProductLoanCounter = maxLoanProductLoanCounter + 1;
        }
        return maxLoanProductLoanCounter;
    }

    private void updateLoanCycleCounter(final List<Loan> loansToUpdate, final Loan loan) {

        final Integer currentLoanCounter = loan.getCurrentLoanCounter();
        final Integer currentLoanProductCounter = loan.getLoanProductLoanCounter();

        for (final Loan loanToUpdate : loansToUpdate) {
            if (loan.loanProduct().isIncludeInBorrowerCycle()) {
                Integer runningLoanCounter = loanToUpdate.getCurrentLoanCounter();
                if (runningLoanCounter > currentLoanCounter) {
                    loanToUpdate.updateClientLoanCounter(--runningLoanCounter);
                }
            }
            if (Objects.equals(loan.loanProduct().getId(), loanToUpdate.loanProduct().getId())) {
                Integer runningLoanProductCounter = loanToUpdate.getLoanProductLoanCounter();
                if (runningLoanProductCounter > currentLoanProductCounter) {
                    loanToUpdate.updateLoanProductLoanCounter(--runningLoanProductCounter);
                }
            }
        }
        this.loanRepositoryWrapper.save(loansToUpdate);
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
    @Transactional
    public void applyOverdueChargesForLoan(final Long loanId, Collection<OverdueLoanScheduleData> overdueLoanScheduleDataList) {

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        final List<Long> existingTransactionIds = loan.findExistingTransactionIds();
        final List<Long> existingReversedTransactionIds = loan.findExistingReversedTransactionIds();
        boolean runInterestRecalculation = false;
        LocalDate recalculateFrom = DateUtils.getBusinessLocalDate();
        LocalDate lastChargeDate = null;
        for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduleDataList) {

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(overdueInstallment.toString());
            final JsonCommand command = JsonCommand.from(overdueInstallment.toString(), parsedCommand, this.fromApiJsonHelper, null, null,
                    null, null, null, loanId, null, null, null, null, null, null);
            LoanOverdueDTO overdueDTO = applyChargeToOverdueLoanInstallment(loan, overdueInstallment.getChargeId(),
                    overdueInstallment.getPeriodNumber(), command);
            loan = overdueDTO.getLoan();
            runInterestRecalculation = runInterestRecalculation || overdueDTO.isRunInterestRecalculation();
            if (recalculateFrom.isAfter(overdueDTO.getRecalculateFrom())) {
                recalculateFrom = overdueDTO.getRecalculateFrom();
            }
            if (lastChargeDate == null || overdueDTO.getLastChargeAppliedDate().isAfter(lastChargeDate)) {
                lastChargeDate = overdueDTO.getLastChargeAppliedDate();
            }
        }
        if (loan != null) {
            boolean reprocessRequired = true;
            LocalDate recalculatedTill = loan.fetchInterestRecalculateFromDate();
            if (recalculateFrom.isAfter(recalculatedTill)) {
                recalculateFrom = recalculatedTill;
            }

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                if (runInterestRecalculation && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                    runScheduleRecalculation(loan, recalculateFrom);
                    reprocessRequired = false;
                }
                updateOriginalSchedule(loan);
            }

            if (reprocessRequired) {
                addInstallmentIfPenaltyAppliedAfterLastDueDate(loan, lastChargeDate);
                ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
                if (changedTransactionDetail != null) {
                    for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings()
                            .entrySet()) {
                        this.loanTransactionRepository.save(mapEntry.getValue());
                        // update loan with references to the newly created
                        // transactions
                        loan.addLoanTransaction(mapEntry.getValue());
                        this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            }

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && runInterestRecalculation
                    && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                this.loanAccountDomainService.recalculateAccruals(loan);
            }
            this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
            businessEventNotifierService.notifyPostBusinessEvent(new LoanApplyOverdueChargeBusinessEvent(loan));
        }
    }

    private void addInstallmentIfPenaltyAppliedAfterLastDueDate(Loan loan, LocalDate lastChargeDate) {
        if (lastChargeDate != null) {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            LoanRepaymentScheduleInstallment lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
            if (lastChargeDate.isAfter(lastInstallment.getDueDate())) {
                if (lastInstallment.isRecalculatedInterestComponent()) {
                    installments.remove(lastInstallment);
                    lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
                }
                boolean recalculatedInterestComponent = true;
                BigDecimal principal = BigDecimal.ZERO;
                BigDecimal interest = BigDecimal.ZERO;
                BigDecimal feeCharges = BigDecimal.ZERO;
                BigDecimal penaltyCharges = BigDecimal.ONE;
                final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
                LoanRepaymentScheduleInstallment newEntry = new LoanRepaymentScheduleInstallment(loan, installments.size() + 1,
                        lastInstallment.getDueDate(), lastChargeDate, principal, interest, feeCharges, penaltyCharges,
                        recalculatedInterestComponent, compoundingDetails);
                loan.addLoanRepaymentScheduleInstallment(newEntry);
            }
        }
    }

    public LoanOverdueDTO applyChargeToOverdueLoanInstallment(final Loan loan, final Long loanChargeId, final Integer periodNumber,
            final JsonCommand command) {
        boolean runInterestRecalculation = false;
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(loanChargeId);

        Collection<Integer> frequencyNumbers = loanChargeReadPlatformService.retrieveOverdueInstallmentChargeFrequencyNumber(loan,
                chargeDefinition, periodNumber);

        Integer feeFrequency = chargeDefinition.feeFrequency();
        final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        Map<Integer, LocalDate> scheduleDates = new HashMap<>();
        final Long penaltyWaitPeriodValue = this.configurationDomainService.retrievePenaltyWaitPeriod();
        final Long penaltyPostingWaitPeriodValue = this.configurationDomainService.retrieveGraceOnPenaltyPostingPeriod();
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        long diff = penaltyWaitPeriodValue + 1 - penaltyPostingWaitPeriodValue;
        if (diff < 1) {
            diff = 1L;
        }
        LocalDate startDate = dueDate.plusDays(penaltyWaitPeriodValue.intValue() + 1);
        int frequencyNumber = 1;
        if (feeFrequency == null) {
            scheduleDates.put(frequencyNumber++, startDate.minusDays(diff));
        } else {
            while (!startDate.isAfter(DateUtils.getBusinessLocalDate())) {
                scheduleDates.put(frequencyNumber++, startDate.minusDays(diff));

                startDate = scheduledDateGenerator.getRepaymentPeriodDate(PeriodFrequencyType.fromInt(feeFrequency),
                        chargeDefinition.feeInterval(), startDate);
            }
        }

        for (Integer frequency : frequencyNumbers) {
            scheduleDates.remove(frequency);
        }

        LoanRepaymentScheduleInstallment installment = null;
        LocalDate lastChargeAppliedDate = dueDate;
        if (!scheduleDates.isEmpty()) {
            installment = loan.fetchRepaymentScheduleInstallment(periodNumber);
            lastChargeAppliedDate = installment.getDueDate();
        }
        LocalDate recalculateFrom = DateUtils.getBusinessLocalDate();

        if (loan != null) {
            businessEventNotifierService.notifyPreBusinessEvent(new LoanApplyOverdueChargeBusinessEvent(loan));
            for (Map.Entry<Integer, LocalDate> entry : scheduleDates.entrySet()) {

                final LoanCharge loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command, entry.getValue());

                if (BigDecimal.ZERO.compareTo(loanCharge.amount()) == 0) {
                    continue;
                }
                LoanOverdueInstallmentCharge overdueInstallmentCharge = new LoanOverdueInstallmentCharge(loanCharge, installment,
                        entry.getKey());
                loanCharge.updateOverdueInstallmentCharge(overdueInstallmentCharge);

                boolean isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
                runInterestRecalculation = runInterestRecalculation || isAppliedOnBackDate;
                if (entry.getValue().isBefore(recalculateFrom)) {
                    recalculateFrom = entry.getValue();
                }
                if (entry.getValue().isAfter(lastChargeAppliedDate)) {
                    lastChargeAppliedDate = entry.getValue();
                }
            }
        }

        return new LoanOverdueDTO(loan, runInterestRecalculation, recalculateFrom, lastChargeAppliedDate);
    }

    @Override
    public CommandProcessingResult undoWriteOff(Long loanId) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        if (!loan.isClosedWrittenOff()) {
            throw new PlatformServiceUnavailableException("error.msg.loan.status.not.written.off.update.not.allowed",
                    "Loan :" + loanId + " update not allowed as loan status is not written off", loanId);
        }
        LocalDate recalculateFrom = null;
        LoanTransaction writeOffTransaction = loan.findWriteOffTransaction();
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoWrittenOffBusinessEvent(writeOffTransaction));

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        ChangedTransactionDetail changedTransactionDetail = loan.undoWrittenOff(defaultLoanLifecycleStateMachine(), existingTransactionIds,
                existingReversedTransactionIds, scheduleGeneratorDTO);
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        saveLoanWithDataIntegrityViolationChecks(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        if (writeOffTransaction != null) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
            businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoWrittenOffBusinessEvent(writeOffTransaction));
        }
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void validateMultiDisbursementData(final JsonCommand command, LocalDate expectedDisbursementDate,
            boolean isDisallowExpectedDisbursements) {
        final String json = command.json();
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);

        if (isDisallowExpectedDisbursements) {
            if (!disbursementDataArray.isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details are not allowed";
                throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        } else {
            if (disbursementDataArray == null || disbursementDataArray.size() == 0) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        }

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("approvedLoanAmount", element);

        loanApplicationCommandFromApiJsonHelper.validateLoanMultiDisbursementDate(element, baseDataValidator, expectedDisbursementDate,
                principal);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateForAddAndDeleteTranche(final Loan loan) {

        BigDecimal totalDisbursedAmount = BigDecimal.ZERO;
        Collection<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
        for (LoanDisbursementDetails disbursementDetails : loanDisburseDetails) {
            if (disbursementDetails.actualDisbursementDate() != null) {
                totalDisbursedAmount = totalDisbursedAmount.add(disbursementDetails.principal());
            }
        }
        if (totalDisbursedAmount.compareTo(loan.getApprovedPrincipal()) == 0) {
            final String errorMessage = "loan.disbursement.cannot.be.a.edited";
            throw new LoanMultiDisbursementException(errorMessage);
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        LocalDate expectedDisbursementDate = loan.getExpectedDisbursedOnLocalDate();
        if (!loan.loanProduct().isMultiDisburseLoan()) {
            final String errorMessage = "loan.product.does.not.support.multiple.disbursals";
            throw new LoanMultiDisbursementException(errorMessage);
        }
        if (loan.isSubmittedAndPendingApproval() || loan.isClosed() || loan.isClosedWrittenOff()
                || loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid()) {
            final String errorMessage = "cannot.modify.tranches.if.loan.is.pendingapproval.closed.overpaid.writtenoff";
            throw new LoanMultiDisbursementException(errorMessage);
        }
        validateMultiDisbursementData(command, expectedDisbursementDate, loan.loanProduct().isDisallowExpectedDisbursements());

        this.validateForAddAndDeleteTranche(loan);

        loan.updateDisbursementDetails(command, actualChanges);

        if (loan.loanProduct().isDisallowExpectedDisbursements()) {
            if (!loan.getDisbursementDetails().isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details are not allowed";
                throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        } else {
            if (loan.getDisbursementDetails().isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        }

        if (loan.getDisbursementDetails().size() > loan.loanProduct().maxTrancheCount()) {
            final String errorMessage = "Number of tranche shouldn't be greater than " + loan.loanProduct().maxTrancheCount();
            throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                    loan.loanProduct().maxTrancheCount(), loan.getDisbursementDetails().size());
        }
        LoanDisbursementDetails updateDetails = null;
        CommandProcessingResult result = processLoanDisbursementDetail(loan, loanId, command, updateDetails);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateDisbursementDataBusinessEvent(loan));
        return result;

    }

    private CommandProcessingResult processLoanDisbursementDetail(Loan loan, Long loanId, JsonCommand command,
            LoanDisbursementDetails loanDisbursementDetails) {
        final List<Long> existingTransactionIds = loan.findExistingTransactionIds();
        final List<Long> existingReversedTransactionIds = loan.findExistingReversedTransactionIds();
        final Map<String, Object> changes = new LinkedHashMap<>();
        LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        ChangedTransactionDetail changedTransactionDetail = null;

        if (command.entityId() != null) {
            changedTransactionDetail = loan.updateDisbursementDateAndAmountForTranche(loanDisbursementDetails, command, changes,
                    scheduleGeneratorDTO);
        } else {
            loan.repaymentScheduleDetail().setPrincipal(loan.getPrincipalAmountForRepaymentSchedule());

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                loan.regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
            } else {
                loan.regenerateRepaymentSchedule(scheduleGeneratorDTO);
                loan.processPostDisbursementTransactions();
            }
        }

        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (command.entityId() != null && changedTransactionDetail != null) {
            for (Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.recalculateAccruals(loan);
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult updateDisbursementDateAndAmountForTranche(final Long loanId, final Long disbursementId,
            final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        LoanDisbursementDetails loanDisbursementDetails = loan.fetchLoanDisbursementsById(disbursementId);
        this.loanEventApiJsonValidator.validateUpdateDisbursementDateAndAmount(command.json(), loanDisbursementDetails);

        CommandProcessingResult result = processLoanDisbursementDetail(loan, loanId, command, loanDisbursementDetails);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateDisbursementDataBusinessEvent(loan));
        return result;

    }

    @Transactional
    @Override
    public void recalculateInterest(final long loanId) {
        Loan loan = this.loanAssembler.assembleFrom(loanId);
        LocalDate recalculateFrom = loan.fetchInterestRecalculateFromDate();
        businessEventNotifierService.notifyPreBusinessEvent(new LoanInterestRecalculationBusinessEvent(loan));
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        ScheduleGeneratorDTO generatorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        ChangedTransactionDetail changedTransactionDetail = loan.recalculateScheduleFromLastTransaction(generatorDTO,
                existingTransactionIds, existingReversedTransactionIds);

        saveLoanWithDataIntegrityViolationChecks(loan);

        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                this.loanTransactionRepository.save(mapEntry.getValue());
                // update loan with references to the newly created
                // transactions
                loan.addLoanTransaction(mapEntry.getValue());
                this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        loanAccountDomainService.recalculateAccruals(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanInterestRecalculationBusinessEvent(loan));
    }

    @Override
    public CommandProcessingResult recoverFromGuarantor(final Long loanId) {
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        this.guarantorDomainService.transaferFundsFromGuarantor(loan);
        return new CommandProcessingResultBuilder().withLoanId(loanId).build();
    }

    private void updateLoanTransaction(final Long loanTransactionId, final LoanTransaction newLoanTransaction) {
        final AccountTransferTransaction transferTransaction = this.accountTransferRepository.findByToLoanTransactionId(loanTransactionId);
        if (transferTransaction != null) {
            transferTransaction.updateToLoanTransaction(newLoanTransaction);
            this.accountTransferRepository.save(transferTransaction);
        }
    }

    private void createLoanScheduleArchive(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);

    }

    private void regenerateScheduleOnDisbursement(final JsonCommand command, final Loan loan, final boolean recalculateSchedule,
            final ScheduleGeneratorDTO scheduleGeneratorDTO, final LocalDate nextPossibleRepaymentDate,
            final LocalDate rescheduledRepaymentDate) {
        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        BigDecimal emiAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.emiAmountParameterName);
        loan.regenerateScheduleOnDisbursement(scheduleGeneratorDTO, recalculateSchedule, actualDisbursementDate, emiAmount,
                nextPossibleRepaymentDate, rescheduledRepaymentDate);
    }

    private List<LoanRepaymentScheduleInstallment> retrieveRepaymentScheduleFromModel(LoanScheduleModel model) {
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        for (final LoanScheduleModelPeriod scheduledLoanInstallment : model.getPeriods()) {
            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(null,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent(),
                        scheduledLoanInstallment.getLoanCompoundingDetails());
                installments.add(installment);
            }
        }
        return installments;
    }

    @Override
    public CommandProcessingResult creditBalanceRefund(Long loanId, JsonCommand command) {
        this.loanEventApiJsonValidator.validateNewRefundTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final String noteText = command.stringValueOfParameterNamedAllowingNull("note");
        final String externalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        if (StringUtils.isNotBlank(externalId)) {
            changes.put("externalId", externalId);
        }

        final CommandProcessingResultBuilder commandProcessingResultBuilder = this.loanAccountDomainService.creditBalanceRefund(loanId,
                transactionDate, transactionAmount, noteText, externalId);

        return commandProcessingResultBuilder //
                .withCommandId(command.commandId()).with(changes) //
                .build();

    }

    @Override
    @Transactional
    public CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command) {

        this.loanEventApiJsonValidator.validateNewRefundTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        // checkRefundDateIsAfterAtLeastOneRepayment(loanId, transactionDate);

        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        checkIfLoanIsPaidInAdvance(loanId, transactionAmount);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("transactionAmount", command.stringValueOfParameterNamed("transactionAmount"));
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        final PaymentDetail paymentDetail = null;

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();

        this.loanAccountDomainService.makeRefundForActiveLoan(loanId, commandProcessingResultBuilder, transactionDate, transactionAmount,
                paymentDetail, noteText, null);

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();

    }

    private void checkIfLoanIsPaidInAdvance(final Long loanId, final BigDecimal transactionAmount) {
        BigDecimal overpaid = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId).getPaidInAdvance();

        if (overpaid == null || overpaid.compareTo(BigDecimal.ZERO) == 0 ? Boolean.TRUE
                : Boolean.FALSE || transactionAmount.floatValue() > overpaid.floatValue()) {
            if (overpaid == null) {
                overpaid = BigDecimal.ZERO;
            }
            throw new InvalidPaidInAdvanceAmountException(overpaid.toPlainString());
        }
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    @Override
    @Transactional
    public CommandProcessingResult undoLastLoanDisbursal(Long loanId, JsonCommand command) {

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        final LocalDate recalculateFromDate = loan.getLastRepaymentDate();
        validateIsMultiDisbursalLoanAndDisbursedMoreThanOneTranche(loan);
        checkClientOrGroupActive(loan);
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoLastDisbursalBusinessEvent(loan));

        final MonetaryCurrency currency = loan.getCurrency();
        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFromDate);

        final Map<String, Object> changes = loan.undoLastDisbursal(scheduleGeneratorDTO, existingTransactionIds,
                existingReversedTransactionIds, loan);
        if (!changes.isEmpty()) {
            loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            String noteText;
            if (command.hasParameter("note")) {
                noteText = command.stringValueOfParameterNamed("note");
                if (StringUtils.isNotBlank(noteText)) {
                    final Note note = Note.loanNote(loan, noteText);
                    this.noteRepository.save(note);
                }
            }
            boolean isAccountTransfer = false;
            final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                    existingReversedTransactionIds, isAccountTransfer);
            journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoLastDisbursalBusinessEvent(loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult forecloseLoan(final Long loanId, final JsonCommand command) {
        final String json = command.json();
        final JsonElement element = fromApiJsonHelper.parse(json);
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.transactionDateParamName, element);
        this.loanEventApiJsonValidator.validateLoanForeclosure(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", transactionDate);

        String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        LoanRescheduleRequest loanRescheduleRequest = null;
        for (LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
            if (!loanDisbursementDetails.expectedDisbursementDateAsLocalDate().isAfter(transactionDate)
                    && loanDisbursementDetails.actualDisbursementDate() == null) {
                final String defaultUserMessage = "The loan with undisbursed tranche before foreclosure cannot be foreclosed.";
                throw new LoanForeclosureException("loan.with.undisbursed.tranche.before.foreclosure.cannot.be.foreclosured",
                        defaultUserMessage, transactionDate);
            }
        }
        this.loanScheduleHistoryWritePlatformService.createAndSaveLoanScheduleArchive(loan.getRepaymentScheduleInstallments(), loan,
                loanRescheduleRequest);

        final Map<String, Object> modifications = this.loanAccountDomainService.foreCloseLoan(loan, transactionDate, noteText);
        changes.putAll(modifications);

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        return commandProcessingResultBuilder.withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void validateIsMultiDisbursalLoanAndDisbursedMoreThanOneTranche(Loan loan) {
        if (!loan.isMultiDisburmentLoan()) {
            final String errorMessage = "loan.product.does.not.support.multiple.disbursals.cannot.undo.last.disbursal";
            throw new LoanMultiDisbursementException(errorMessage);
        }
        Integer trancheDisbursedCount = 0;
        for (LoanDisbursementDetails disbursementDetails : loan.getDisbursementDetails()) {
            if (disbursementDetails.actualDisbursementDate() != null) {
                trancheDisbursedCount++;
            }
        }
        if (trancheDisbursedCount <= 1) {
            final String errorMessage = "tranches.should.be.disbursed.more.than.one.to.undo.last.disbursal";
            throw new LoanMultiDisbursementException(errorMessage);
        }

    }

    private void syncExpectedDateWithActualDisbursementDate(final Loan loan, LocalDate actualDisbursementDate) {
        if (!loan.getExpectedDisbursedOnLocalDate().equals(actualDisbursementDate)) {
            throw new DateMismatchException(actualDisbursementDate, loan.getExpectedDisbursedOnLocalDate());
        }

    }

    private void validateTransactionsForTransfer(final Loan loan, final LocalDate transferDate) {

        for (LoanTransaction transaction : loan.getLoanTransactions()) {
            if ((transaction.getTransactionDate().isEqual(transferDate) && transaction.getSubmittedOnDate().isEqual(transferDate))
                    || transaction.getTransactionDate().isAfter(transferDate)) {
                throw new GeneralPlatformDomainRuleException(TransferApiConstants.transferClientLoanException,
                        TransferApiConstants.transferClientLoanExceptionMessage, transaction.getCreatedDateTime().toLocalDate(),
                        transferDate);
            }

        }

    }

}
