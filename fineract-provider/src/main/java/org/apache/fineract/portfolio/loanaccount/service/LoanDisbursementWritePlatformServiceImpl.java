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
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.PARAM_STATUS;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanUndoDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanUndoLastDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanUpdateDisbursementDataBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanDisbursalTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionFlagsData;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionPriority;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.DateMismatchException;
import org.apache.fineract.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanMultiDisbursementException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanaccount.exception.UndoLastTrancheDisbursementException;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanDisbursementService.DownPaymentFeesPreparation;
import org.apache.fineract.portfolio.loanaccount.service.LoanDownPaymentFeeEventNotifier.FeeState;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class LoanDisbursementWritePlatformServiceImpl implements LoanDisbursementWritePlatformService {

    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final CalendarRepository calendarRepository;
    private final LoanApplicationValidator loanApplicationValidator;
    private final AccountAssociationsRepository accountAssociationRepository;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanUtilService loanUtilService;
    private final CashierTransactionDataValidator cashierTransactionDataValidator;
    private final GLIMAccountInfoRepository glimRepository;
    private final LoanRepository loanRepository;
    private final RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler;
    private final PostDatedChecksRepository postDatedChecksRepository;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;
    private final LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    private final LoanDisbursementService loanDisbursementService;
    private final LoanScheduleService loanScheduleService;
    private final LoanChargeValidator loanChargeValidator;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanBalanceService loanBalanceService;
    private final LoanOriginatorLinkingService loanOriginatorLinkingService;
    private final LoanChargeService loanChargeService;
    private final LoanDownPaymentFeeEventNotifier loanDownPaymentFeeEventNotifier;
    private final LoanWriteServiceHelper loanWriteServiceHelper;

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

    @Override
    public CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer) {
        return disburseLoan(loanId, command, isAccountTransfer, false);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command, Boolean isAccountTransfer,
            Boolean isWithoutAutoPayment) {
        loanTransactionValidator.validateDisbursement(command, isAccountTransfer, loanId);

        Loan loan = loanAssembler.assembleFromInUserScope(loanId);
        final Map<Long, FeeState> downPaymentFeesBefore = loanDownPaymentFeeEventNotifier.captureFees(loan);

        if (loan.loanProduct().isDisallowExpectedDisbursements()) {
            List<LoanDisbursementDetails> filteredList = loan.getDisbursementDetails().stream()
                    .filter(disbursementDetails -> disbursementDetails.actualDisbursementDate() == null).toList();
            // Check whether a new LoanDisbursementDetails is required
            if (filteredList.isEmpty()) {
                // create artificial 'tranche/expected disbursal' as current disburse code expects it for
                // multi-disbursal products
                final LocalDate artificialExpectedDate = loan.getExpectedDisbursedOnLocalDate();
                LoanDisbursementDetails disbursementDetail = new LoanDisbursementDetails(artificialExpectedDate, null,
                        loan.getDisbursedAmount(), null, false);
                disbursementDetail.updateLoan(loan);
                loan.getAllDisbursementDetails().add(disbursementDetail);
            }
        }

        final LocalDate nextPossibleRepaymentDate = loan.getNextPossibleRepaymentDateForRescheduling();
        final LocalDate rescheduledRepaymentDate = command.localDateValueOfParameterNamed("adjustRepaymentDate");
        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        if (!loan.isMultiDisburmentLoan()) {
            loan.setActualDisbursementDate(actualDisbursementDate);
        }

        // validate actual disbursement date against meeting date
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, null);

        businessEventNotifierService.notifyPreBusinessEvent(new LoanDisbursalBusinessEvent(loan));

        final AppUser currentUser = loanWriteServiceHelper.getAppUserIfPresent();
        final Map<String, Object> changes = new LinkedHashMap<>();

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        if (paymentDetail != null && paymentDetail.getPaymentType() != null && paymentDetail.getPaymentType().getIsCashPayment()) {
            BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
            this.cashierTransactionDataValidator.validateOnLoanDisbursal(currentUser, loan.getCurrencyCode(), transactionAmount);
        }
        final boolean isPaymentTypeApplicableForDisbursementCharge = configurationDomainService
                .isPaymentTypeApplicableForDisbursementCharge();

        // Recalculate first repayment date based in actual disbursement date.
        loanWriteServiceHelper.updateLoanCounters(loan, actualDisbursementDate);
        Money amountBeforeAdjust = loan.getPrincipal();
        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final long termsBefore = loan.getTermsCount();

        if (canDisburse(loan)) {
            // Attach loan to originator before downpayment/disbursement
            if (command.hasNonNullParameter(LoanApiConstants.ORIGINATORS_PARAM)) {
                final JsonArray originatorsArray = command.arrayOfParameterNamed(LoanApiConstants.ORIGINATORS_PARAM);
                loanOriginatorLinkingService.processOriginatorsForLoanDisbursement(loanId, originatorsArray);
            }

            // Get netDisbursalAmount from disbursal screen field.
            final BigDecimal netDisbursalAmount = command
                    .bigDecimalValueOfParameterNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName);
            if (netDisbursalAmount != null) {
                loan.setNetDisbursalAmount(netDisbursalAmount);
            }
            final List<LoanDisbursementDetails> undisbursedTranches = loan.getDisbursementDetails().stream()
                    .filter(tranche -> tranche.actualDisbursementDate() == null).toList();
            Money disburseAmount = loanDisbursementService.adjustDisburseAmount(loan, command, actualDisbursementDate);
            final List<LoanDisbursementDetails> disbursedTranches = undisbursedTranches.stream()
                    .filter(tranche -> tranche.actualDisbursementDate() != null).toList();
            final boolean skipDownPaymentFee = command
                    .booleanPrimitiveValueOfParameterNamed(LoanApiConstants.skipDownPaymentFeeParameterName);
            final BigDecimal downPaymentFeeAmount = command
                    .bigDecimalValueOfParameterNamed(LoanApiConstants.downPaymentFeeAmountParameterName);
            final DownPaymentFeesPreparation downPaymentFees = loanDisbursementService.prepareDownPaymentFeesForDisbursement(loan,
                    disbursedTranches, actualDisbursementDate, skipDownPaymentFee, downPaymentFeeAmount);
            boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincipal()) || downPaymentFees.feesDeactivated();
            Money amountToDisburse = disburseAmount.copy();
            final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

            if (loan.isTopup() && loan.getClientId() != null) {
                final BigDecimal loanOutstanding = loanApplicationValidator.validateTopupLoan(loan, actualDisbursementDate);

                amountToDisburse = disburseAmount.minus(loanOutstanding);
                disburseLoanToLoan(loan, command, loanOutstanding);
            }

            LoanTransaction disbursementTransaction = null;
            if (isAccountTransfer) {
                disburseLoanToSavings(loan, command, amountToDisburse, paymentDetail);
            } else {
                disbursementTransaction = LoanTransaction.disbursement(loan, amountToDisburse, paymentDetail, actualDisbursementDate,
                        txnExternalId, loan.getTotalOverpaidAsMoney());
                disbursementTransaction.updateLoan(loan);
                loan.addLoanTransaction(disbursementTransaction);
                loanTransactionRepository.saveAndFlush(disbursementTransaction);
                journalEntryPoster.postJournalEntriesForLoanTransaction(disbursementTransaction, false, false);
            }
            if (loan.getRepaymentScheduleInstallments().isEmpty()) {
                /*
                 * If no schedule, generate one (applicable to non-tranche multi-disbursal loans)
                 */
                recalculateSchedule = true;
            }

            regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO, nextPossibleRepaymentDate,
                    rescheduledRepaymentDate);
            loanDisbursementService.completeDownPaymentFeesForDisbursement(loan, disbursedTranches, actualDisbursementDate,
                    skipDownPaymentFee, downPaymentFeeAmount, downPaymentFees.replacedFee());
            boolean downPaymentEnabled = loan.getLoanProductRelatedDetail().isEnableDownPayment();
            if (loan.isInterestBearingAndInterestRecalculationEnabled() || downPaymentEnabled) {
                loanWriteServiceHelper.createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
            }
            disburseLoan(command, isPaymentTypeApplicableForDisbursementCharge, paymentDetail, loan, currentUser, changes,
                    scheduleGeneratorDTO, isAccountTransfer);
            loan.adjustNetDisbursalAmount(amountToDisburse.getAmount());

            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);

            LocalDate firstInstallmentDueDate = loan.fetchRepaymentScheduleInstallment(1).getDueDate();
            if (loan.isInterestBearingAndInterestRecalculationEnabled()
                    && (DateUtils.isBeforeBusinessDate(firstInstallmentDueDate) || loan.isDisbursementMissed())) {
                loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
            }

            if (loan.isAutoRepaymentForDownPaymentEnabled() && !isWithoutAutoPayment) {
                // updating linked savings account for auto down payment transaction for disbursement to savings account
                if (isAccountTransfer && loan.shouldCreateStandingInstructionAtDisbursement()) {
                    final PortfolioAccountData linkedSavingsAccountData = this.accountAssociationsReadPlatformService
                            .retriveLoanLinkedAssociation(loanId);
                    if (linkedSavingsAccountData == null) {
                        throw new LinkedAccountRequiredException("loan.disburse.downpayment",
                                "Loan with id:" + loanId + " requires a linked savings account for the down payment transfer", loanId);
                    }
                    final SavingsAccount fromSavingsAccount = null;
                    final boolean isRegularTransaction = true;
                    final boolean isExceptionForBalanceCheck = false;

                    BigDecimal disbursedAmountPercentageForDownPayment = loan.getLoanRepaymentScheduleDetail()
                            .getDisbursedAmountPercentageForDownPayment();
                    final Money downPaymentMoney = Money
                            .of(loan.getCurrency(),
                                    loanChargeService.calculateDownPaymentAmount(amountToDisburse.getAmount(),
                                            disbursedAmountPercentageForDownPayment,
                                            loan.getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf(), loan.getCurrency()))
                            .plus(loanChargeService.downPaymentFeesForNewAutoDownPayment(loan, actualDisbursementDate));
                    final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate,
                            downPaymentMoney.getAmount(), PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN,
                            linkedSavingsAccountData.getId(), loan.getId(),
                            "To loan " + loan.getAccountNumber() + " from savings " + linkedSavingsAccountData.getAccountNo()
                                    + " Standing instruction transfer ",
                            locale, fmt, null, null, LoanTransactionType.DOWN_PAYMENT.getValue(), null, null,
                            AccountTransferType.LOAN_DOWN_PAYMENT.getValue(), null, null, ExternalId.empty(), null, null,
                            fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                    this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
                } else {
                    loanDownPaymentHandlerService.handleDownPayment(scheduleGeneratorDTO, command, disbursementTransaction, loan);
                    loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
                    if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                        loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
                    }
                }
            }
        }
        if (!changes.isEmpty()) {
            loan.updateLoanScheduleDependentDerivedFields();
            loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

            loanWriteServiceHelper.createNote(loan, command, changes);
            // auto create standing instruction
            createStandingInstruction(loan);
        }

        final Set<LoanCharge> loanCharges = loan.getActiveCharges();
        final Map<Long, BigDecimal> disBuLoanCharges = new HashMap<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement() && loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()
                    && loanCharge.isChargePending()) {
                disBuLoanCharges.put(loanCharge.getId(), loanCharge.amountOutstanding());
            }
        }

        for (final Map.Entry<Long, BigDecimal> entrySet : disBuLoanCharges.entrySet()) {
            final PortfolioAccountData savingAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            if (savingAccountData == null) {
                throw new LinkedAccountRequiredException("loan.disburse.charge",
                        "Loan with id:" + loanId + " has a charge payable by account transfer but no linked savings account", loanId);
            }
            final SavingsAccount fromSavingsAccount = null;
            final boolean isRegularTransaction = true;
            final boolean isExceptionForBalanceCheck = false;
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.getId(), loanId, "Loan Charge Payment",
                    locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(), entrySet.getKey(), null,
                    AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, ExternalId.empty(), null, null, fromSavingsAccount,
                    isRegularTransaction, isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        }
        if (!disBuLoanCharges.isEmpty()) {
            // transferFunds marks each charge as paid and creates REPAYMENT_AT_DISBURSEMENT loan transactions,
            // but it does not recompute the LoanSummary fields (feeChargesPaid, feeChargesOutstanding,
            // totalOutstanding, etc.). Without this, those fields remain stale from the saveAndFlush above,
            // causing the loan balance to appear higher than what the client actually owes.
            loanBalanceService.updateLoanSummaryDerivedFields(loan);
            loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        }
        updateRecurringCalendarDatesForInterestRecalculation(loan);
        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        // Post Dated Checks
        if (command.parameterExists("postDatedChecks")) {
            // get repayment with post dates checks to update
            Set<PostDatedChecks> postDatedChecks = this.repaymentWithPostDatedChecksAssembler.fromParsedJson(command.json(), loan);
            updatePostDatedChecks(postDatedChecks);
        }

        businessEventNotifierService.notifyPostBusinessEvent(new LoanDisbursalBusinessEvent(loan));

        Long disbursalTransactionId = null;
        ExternalId disbursalTransactionExternalId = null;

        if (!isAccountTransfer) {
            // If accounting is not periodic accrual, the last transaction might be the accrual not the disbursement
            LoanTransaction disbursalTransaction = Lists.reverse(loan.getLoanTransactions()).stream()
                    .filter(e -> LoanTransactionType.DISBURSEMENT.equals(e.getTypeOf())).findFirst().orElseThrow();
            disbursalTransactionId = disbursalTransaction.getId();
            disbursalTransactionExternalId = disbursalTransaction.getExternalId();
            final long termsAfter = loan.getTermsCount();
            businessEventNotifierService.notifyPostBusinessEvent(new LoanDisbursalTransactionBusinessEvent(disbursalTransaction,
                    new LoanTransactionFlagsData(termsAfter != termsBefore)));
        }

        loanDownPaymentFeeEventNotifier.notifyFeeChanges(loan, downPaymentFeesBefore);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withSubEntityId(disbursalTransactionId) //
                .withSubEntityExternalId(disbursalTransactionExternalId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void disburseLoan(JsonCommand command, boolean isPaymentTypeApplicableForDisbursementCharge, PaymentDetail paymentDetail,
            Loan loan, AppUser currentUser, Map<String, Object> changes, ScheduleGeneratorDTO scheduleGeneratorDTO,
            boolean isAccountTransfer) {
        final PaymentDetail paymentDetail1 = isPaymentTypeApplicableForDisbursementCharge ? paymentDetail : null;
        final LocalDate actualDisbursementDate1 = command.localDateValueOfParameterNamed(ACTUAL_DISBURSEMENT_DATE);

        loan.setDisbursedBy(currentUser);
        loan.updateLoanScheduleDependentDerivedFields();

        changes.put(Loan.LOCALE, command.locale());
        changes.put(Loan.DATE_FORMAT, command.dateFormat());
        changes.put(ACTUAL_DISBURSEMENT_DATE, command.stringValueOfParameterNamed(ACTUAL_DISBURSEMENT_DATE));

        boolean disbursementMissedParam = loan.isDisbursementMissed();
        LocalDate firstInstallmentDueDate = loan.fetchRepaymentScheduleInstallment(1).getDueDate();
        if ((loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()
                && (DateUtils.isBeforeBusinessDate(firstInstallmentDueDate) || disbursementMissedParam))) {
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
        } else {
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }

        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
        loan.updateLoanRepaymentPeriodsDerivedFields(actualDisbursementDate1);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_DISBURSED,
                actualDisbursementDate1);
        loanDisbursementService.handleDisbursementTransaction(loan, actualDisbursementDate1, paymentDetail1, isAccountTransfer);
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        final Money interestApplied = Money.of(loan.getCurrency(), loan.getSummary().getTotalInterestCharged());

        /*
         * Add an interest applied transaction only when the interest is accrued upfront (Up front accrual). When
         * allow-cash-and-non-cash-accrual global config is enabled (legacy mode), accrual transactions are also created
         * for None and Cash based accounting for backward compatibility.
         */
        if (((loan.isMultiDisburmentLoan() && loan.getDisbursedLoanDisbursementDetails().size() == 1) || !loan.isMultiDisburmentLoan())
                && (configurationDomainService.isAllowCashAndNonCashAccrual()
                        ? loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()
                        : loan.isUpfrontAccrualAccountingEnabledOnLoanProduct())
                && interestApplied.isGreaterThanZero()) {
            ExternalId externalId = ExternalId.empty();
            if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                externalId = ExternalId.generate();
            }
            final LoanTransaction interestAppliedTransaction = LoanTransaction.accrueInterest(loan.getOffice(), loan, interestApplied,
                    actualDisbursementDate1, externalId);
            loan.addLoanTransaction(interestAppliedTransaction);
            loanTransactionRepository.saveAndFlush(interestAppliedTransaction);
            journalEntryPoster.postJournalEntriesForLoanTransaction(interestAppliedTransaction, false, false);
        }

        if (loan.getLoanProduct().isMultiDisburseLoan() || loan.isProgressiveSchedule()) {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = loanTransactionRepository
                    .findNonReversedTransactionsForReprocessingByLoan(loan);
            if (!allNonContraTransactionsPostDisbursement.isEmpty()) {
                reprocessLoanTransactionsService.reprocessTransactions(loan);
            }
            loanBalanceService.updateLoanSummaryDerivedFields(loan);
        }

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSED, loan);
        changes.put(PARAM_STATUS, LoanEnumerations.status(loan.getLoanStatus()));
    }

    private void updatePostDatedChecks(Set<PostDatedChecks> postDatedChecks) {
        this.postDatedChecksRepository.saveAll(postDatedChecks);
    }

    /**
     * create standing instruction for disbursed loan
     *
     * @param loan
     *            the disbursed loan
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
                // auto-generated instructions keep the all-or-nothing default; partial transfers are opted into per
                // instruction through the standing instruction API
                final boolean allowPartialTransfer = false;

                AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToLoanTransfer(fromOffice, fromClient,
                        linkedSavingsAccount, toOffice, toClient, loan, transferType);

                AccountTransferStandingInstruction accountTransferStandingInstruction = AccountTransferStandingInstruction.create(
                        accountTransferDetails, name, priority, instructionType, status, null, validFrom, null, recurrenceType, null, null,
                        null, allowPartialTransfer);
                accountTransferDetails.updateAccountTransferStandingInstruction(accountTransferStandingInstruction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        }
    }

    private void updateRecurringCalendarDatesForInterestRecalculation(final Loan loan) {

        if (loan.isInterestBearingAndInterestRecalculationEnabled()
                && loan.loanInterestRecalculationDetails().getRestFrequencyType().isSameAsRepayment()) {
            final CalendarInstance calendarInstanceForInterestRecalculation = this.calendarInstanceRepository
                    .findByEntityIdAndEntityTypeIdAndCalendarTypeId(loan.loanInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue(), CalendarType.COLLECTION.getValue());

            Calendar calendarForInterestRecalculation = calendarInstanceForInterestRecalculation.getCalendar();
            calendarForInterestRecalculation.updateStartAndEndDate(loan.getDisbursementDate(), loan.getMaturityDate());
            this.calendarRepository.save(calendarForInterestRecalculation);
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
        final AppUser currentUser = loanWriteServiceHelper.getAppUserIfPresent();

        final SingleDisbursalCommand[] disbursalCommand = bulkDisbursalCommand.getDisburseTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        if (disbursalCommand == null) {
            return changes;
        }

        final LocalDate nextPossibleRepaymentDate = null;
        final LocalDate rescheduledRepaymentDate = null;

        for (final SingleDisbursalCommand singleLoanDisbursalCommand : disbursalCommand) {
            Loan loan = this.loanAssembler.assembleFromInUserScope(singleLoanDisbursalCommand.getLoanId());
            final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");

            // validate ActualDisbursement Date Against Expected Disbursement
            // Date
            LoanProduct loanProduct = loan.loanProduct();
            if (loanProduct.isSyncExpectedWithDisbursementDate()) {
                syncExpectedDateWithActualDisbursementDate(loan, actualDisbursementDate);
            }
            loanWriteServiceHelper.checkClientOrGroupActive(loan);
            businessEventNotifierService.notifyPreBusinessEvent(new LoanDisbursalBusinessEvent(loan));

            final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

            // Bulk disbursement should happen on meeting date (mostly from
            // collection sheet).
            // FIXME: AA - this should be first meeting date based on
            // disbursement date and next available meeting dates
            // assuming repayment schedule won't regenerate because expected
            // disbursement and actual disbursement happens on same date
            loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_DISBURSED);
            loanWriteServiceHelper.updateLoanCounters(loan, actualDisbursementDate);
            if (canDisburse(loan)) {
                Money amountBeforeAdjust = loan.getPrincipal();
                Money disburseAmount = loanDisbursementService.adjustDisburseAmount(loan, command, actualDisbursementDate);
                boolean recalculateSchedule = amountBeforeAdjust.isNotEqualTo(loan.getPrincipal());
                final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);
                final long termsBefore = loan.getTermsCount();
                LoanTransaction disbursementTransaction = null;
                if (isAccountTransfer) {
                    disburseLoanToSavings(loan, command, disburseAmount, paymentDetail);
                } else {
                    disbursementTransaction = LoanTransaction.disbursement(loan, disburseAmount, paymentDetail, actualDisbursementDate,
                            txnExternalId, loan.getTotalOverpaidAsMoney());
                    disbursementTransaction.updateLoan(loan);
                    loan.addLoanTransaction(disbursementTransaction);
                    loanTransactionRepository.saveAndFlush(disbursementTransaction);
                    journalEntryPoster.postJournalEntriesForLoanTransaction(disbursementTransaction, false, false);
                }
                LocalDate recalculateFrom = null;
                final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                regenerateScheduleOnDisbursement(command, loan, recalculateSchedule, scheduleGeneratorDTO, nextPossibleRepaymentDate,
                        rescheduledRepaymentDate);
                boolean downPaymentEnabled = loan.getLoanProductRelatedDetail().isEnableDownPayment();
                if (loan.isInterestBearingAndInterestRecalculationEnabled() || downPaymentEnabled) {
                    loanWriteServiceHelper.createAndSaveLoanScheduleArchive(loan, scheduleGeneratorDTO);
                }
                disburseLoan(command, configurationDomainService.isPaymentTypeApplicableForDisbursementCharge(), paymentDetail, loan,
                        currentUser, changes, scheduleGeneratorDTO, isAccountTransfer);
                if (disbursementTransaction != null) {
                    final long termsAfter = loan.getTermsCount();
                    final LoanTransactionFlagsData additionalDisbursalFlags = new LoanTransactionFlagsData(termsAfter != termsBefore);
                    businessEventNotifierService.notifyPostBusinessEvent(
                            new LoanDisbursalTransactionBusinessEvent(disbursementTransaction, additionalDisbursalFlags));
                }

                loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);

                LocalDate firstInstallmentDueDate = loan.fetchRepaymentScheduleInstallment(1).getDueDate();
                if (loan.isInterestBearingAndInterestRecalculationEnabled()
                        && (DateUtils.isBeforeBusinessDate(firstInstallmentDueDate) || loan.isDisbursementMissed())) {
                    loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
                }
            }
            if (!changes.isEmpty()) {
                loanWriteServiceHelper.createNote(loan, command, changes);
                loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
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
                if (savingAccountData == null) {
                    throw new LinkedAccountRequiredException("loan.disburse.charge",
                            "Loan with id:" + loan.getId() + " has a charge payable by account transfer but no linked savings account",
                            loan.getId());
                }
                final SavingsAccount fromSavingsAccount = null;
                final boolean isRegularTransaction = true;
                final boolean isExceptionForBalanceCheck = false;
                final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(actualDisbursementDate, entrySet.getValue(),
                        PortfolioAccountType.SAVINGS, PortfolioAccountType.LOAN, savingAccountData.getId(), loan.getId(),
                        "Loan Charge Payment", locale, fmt, null, null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue(),
                        entrySet.getKey(), null, AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, ExternalId.empty(), null, null,
                        fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
                this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
            }
            updateRecurringCalendarDatesForInterestRecalculation(loan);
            loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan,
                    loan.isInterestBearingAndInterestRecalculationEnabled(), true);
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

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final Map<Long, FeeState> downPaymentFeesBefore = loanDownPaymentFeeEventNotifier.captureFees(loan);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.charged.off",
                    "Undo Loan: " + loanId + " disbursement is not allowed. Loan Account is Charged-off", loanId);
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoDisbursalBusinessEvent(loan));
        loanWriteServiceHelper.removeLoanCycle(loan);

        final LocalDate recalculateFrom = null;
        loan.setActualDisbursementDate(null);
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        // Remove post dated checks if added.
        loan.removePostDatedChecks();

        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_DISBURSAL_UNDO);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_DISBURSAL_UNDO,
                loan.getDisbursementDate());
        final Map<String, Object> changes = undoDisbursal(loan, scheduleGeneratorDTO);

        loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);

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
            loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            this.accountTransfersWritePlatformService.reverseAllTransactions(loanId, PortfolioAccountType.LOAN);
            loanWriteServiceHelper.createNote(loan, command, changes);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoDisbursalBusinessEvent(loan));
            loanDownPaymentFeeEventNotifier.notifyFeeChanges(loan, downPaymentFeesBefore);
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

    private void disburseLoanToLoan(final Loan loan, final JsonCommand command, final BigDecimal amount) {
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.LOAN,
                PortfolioAccountType.LOAN, loan.getId(), loan.getTopupLoanDetails().getLoanIdToClose(), "Loan Topup", locale, fmt,
                LoanTransactionType.DISBURSEMENT.getValue(), LoanTransactionType.REPAYMENT.getValue(), txnExternalId, loan, null);
        AccountTransferDetails accountTransferDetails = this.accountTransfersWritePlatformService.repayLoanWithTopup(accountTransferDTO);
        loan.getTopupLoanDetails().setAccountTransferDetails(accountTransferDetails.getId());
        loan.getTopupLoanDetails().setTopupAmount(amount);
    }

    protected Long disburseLoanToSavings(final Loan loan, final JsonCommand command, final Money amount,
            final PaymentDetail paymentDetail) {
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

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
        return this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
    }

    private void validateMultiDisbursementData(final JsonCommand command, LocalDate expectedDisbursementDate,
            boolean isDisallowExpectedDisbursements, Loan loan) {
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
            if (disbursementDataArray == null || disbursementDataArray.isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        }

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("approvedLoanAmount", element);

        loanApplicationValidator.validateLoanMultiDisbursementDate(element, baseDataValidator, expectedDisbursementDate, principal, loan);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateForAddAndDeleteTranche(final Loan loan) {

        BigDecimal totalDisbursedAmount = BigDecimal.ZERO;
        Collection<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
        for (LoanDisbursementDetails disbursementDetails : loanDisburseDetails) {
            if (disbursementDetails.actualDisbursementDate() != null) {
                totalDisbursedAmount = totalDisbursedAmount.add(disbursementDetails.getPrincipal());
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

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final Map<Long, FeeState> downPaymentFeesBefore = loanDownPaymentFeeEventNotifier.captureFees(loan);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.charged.off",
                    "Update Loan: " + loanId + " disbursement details is not allowed. Loan Account is Charged-off", loanId);
        }
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
        validateMultiDisbursementData(command, expectedDisbursementDate, loan.loanProduct().isDisallowExpectedDisbursements(), loan);

        this.validateForAddAndDeleteTranche(loan);

        loanDisbursementService.updateDisbursementDetails(loan, command, actualChanges);

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
        loanDownPaymentFeeEventNotifier.notifyFeeChanges(loan, downPaymentFeesBefore);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateDisbursementDataBusinessEvent(loan));
        return result;

    }

    private CommandProcessingResult processLoanDisbursementDetail(Loan loan, Long loanId, JsonCommand command,
            LoanDisbursementDetails loanDisbursementDetails) {
        final Map<String, Object> changes = new LinkedHashMap<>();
        LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        if (command.entityId() != null) {
            loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_EDIT_MULTI_DISBURSE_DATE);
            updateDisbursementDateAndAmountForTranche(loan, loanDisbursementDetails, command, changes, scheduleGeneratorDTO);
        } else {
            loan.getLoanProductRelatedDetail().setPrincipal(loan.getPrincipalAmountForRepaymentSchedule());
            if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
            } else if (loan.isProgressiveSchedule()) {
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
                reprocessLoanTransactionsService.reprocessTransactions(loan);
            }
        }

        loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

        loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanWriteServiceHelper.createLoanScheduleArchive(loan, scheduleGeneratorDTO);
        }
        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult updateDisbursementDateAndAmountForTranche(final Long loanId, final Long disbursementId,
            final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final Map<Long, FeeState> downPaymentFeesBefore = loanDownPaymentFeeEventNotifier.captureFees(loan);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.charged.off",
                    "Update Loan: " + loanId + " disbursement details is not allowed. Loan Account is Charged-off", loanId);
        }
        LoanDisbursementDetails loanDisbursementDetails = loan.fetchLoanDisbursementsById(disbursementId);
        this.loanTransactionValidator.validateUpdateDisbursementDateAndAmount(command.json(), loanDisbursementDetails);

        CommandProcessingResult result = processLoanDisbursementDetail(loan, loanId, command, loanDisbursementDetails);
        loanDownPaymentFeeEventNotifier.notifyFeeChanges(loan, downPaymentFeesBefore);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateDisbursementDataBusinessEvent(loan));
        return result;

    }

    private void regenerateScheduleOnDisbursement(final JsonCommand command, final Loan loan, final boolean recalculateSchedule,
            final ScheduleGeneratorDTO scheduleGeneratorDTO, final LocalDate nextPossibleRepaymentDate,
            final LocalDate rescheduledRepaymentDate) {
        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
        BigDecimal emiAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.fixedEmiAmountParameterName);

        boolean isEmiAmountChanged = false;
        LoanProduct loanProduct = loan.getLoanProduct();
        if ((loanProduct.isMultiDisburseLoan() || loanProduct.isCanDefineInstallmentAmount()) && emiAmount != null
                && emiAmount.compareTo(loan.retriveLastEmiAmount()) != 0) {
            if (loanProduct.isMultiDisburseLoan()) {
                final LocalDate dateValue = null;
                final boolean isSpecificToInstallment = false;
                final Boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled = scheduleGeneratorDTO
                        .isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled();
                LocalDate effectiveDateFrom = actualDisbursementDate;
                if (!isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled && actualDisbursementDate.equals(nextPossibleRepaymentDate)) {
                    effectiveDateFrom = nextPossibleRepaymentDate.plusDays(1);
                }
                LoanTermVariations loanVariationTerms = new LoanTermVariations(LoanTermVariationType.EMI_AMOUNT.getValue(),
                        effectiveDateFrom, emiAmount, dateValue, isSpecificToInstallment, loan, LoanStatus.ACTIVE.getValue());
                loan.getLoanTermVariations().add(loanVariationTerms);
            } else {
                loan.setFixedEmiAmount(emiAmount);
            }
            isEmiAmountChanged = true;
        }
        if (rescheduledRepaymentDate != null && loanProduct.isMultiDisburseLoan()) {
            final boolean isSpecificToInstallment = false;
            LoanTermVariations loanVariationTerms = new LoanTermVariations(LoanTermVariationType.DUE_DATE.getValue(),
                    nextPossibleRepaymentDate, emiAmount, rescheduledRepaymentDate, isSpecificToInstallment, loan,
                    LoanStatus.ACTIVE.getValue());
            loan.getLoanTermVariations().add(loanVariationTerms);
        }

        if (loan.isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate) || recalculateSchedule || isEmiAmountChanged
                || rescheduledRepaymentDate != null) {
            if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
            } else if (loan.isProgressiveSchedule()) {
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            }
        }
        loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

    }

    @Override
    @Transactional
    public CommandProcessingResult undoLastLoanDisbursal(Long loanId, JsonCommand command) {

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        final Map<Long, FeeState> downPaymentFeesBefore = loanDownPaymentFeeEventNotifier.captureFees(loan);
        final LocalDate recalculateFromDate = loan.getLastRepaymentDate();
        validateIsMultiDisbursalLoanAndDisbursedMoreThanOneTranche(loan);
        loanWriteServiceHelper.checkClientOrGroupActive(loan);
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.charged.off",
                    "Undo Loan: " + loanId + " last disbursement is not allowed. Loan Account is Charged-off", loanId);
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoLastDisbursalBusinessEvent(loan));

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFromDate);

        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_DISBURSAL_UNDO_LAST);
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_DISBURSAL_UNDO_LAST,
                loan.getDisbursementDate());
        final Map<String, Object> changes = undoLastDisbursal(scheduleGeneratorDTO, loan);

        if (!changes.isEmpty()) {
            loan = loanWriteServiceHelper.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
            }
            loanWriteServiceHelper.createNote(loan, command, changes);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoLastDisbursalBusinessEvent(loan));
            loanDownPaymentFeeEventNotifier.notifyFeeChanges(loan, downPaymentFeesBefore);
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

    private void validateIsMultiDisbursalLoanAndDisbursedMoreThanOneTranche(Loan loan) {
        if (!loan.isMultiDisburmentLoan()) {
            final String errorMessage = "loan.product.does.not.support.multiple.disbursals.cannot.undo.last.disbursal";
            throw new LoanMultiDisbursementException(errorMessage);
        }
        int trancheDisbursedCount = 0;
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

    private Map<String, Object> undoDisbursal(final Loan loan, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        final LoanStatus currentStatus = loan.getStatus();
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_DISBURSAL_UNDO, loan);
        if (!statusEnum.hasStateOf(currentStatus)) {
            this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSAL_UNDO, loan); // tis will update the loan
                                                                                            // status
            actualChanges.put(PARAM_STATUS, LoanEnumerations.status(loan.getLoanStatus()));

            final LocalDate actualDisbursementDate = loan.getDisbursementDate();
            final boolean isScheduleRegenerateRequired = loan.isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate);
            loan.setActualDisbursementDate(null);
            loan.setDisbursedBy(null);
            loan.setLastClosedBusinessDate(null);
            final boolean isDisbursedAmountChanged = !MathUtil.isEqualTo(loan.getApprovedPrincipal(),
                    loan.getLoanRepaymentScheduleDetail().getPrincipal().getAmount());
            loan.getLoanRepaymentScheduleDetail().setPrincipal(loan.getApprovedPrincipal());
            loanDisbursementService.restoreDownPaymentFeesReplacedByOverride(loan);
            // Remove All the Disbursement Details If the Loan Product is disabled and exists one
            if (loan.loanProduct().isDisallowExpectedDisbursements() && !loan.getDisbursementDetails().isEmpty()) {
                for (LoanDisbursementDetails disbursementDetail : loan.getAllDisbursementDetails()) {
                    loanDisbursementService.deactivateDownPaymentFeesOfTranche(loan, disbursementDetail);
                    disbursementDetail.reverse();
                }
            } else {
                for (final LoanDisbursementDetails details : loan.getDisbursementDetails()) {
                    details.updateActualDisbursementDate(null);
                }
            }
            final boolean isEmiAmountChanged = !loan.getLoanTermVariations().isEmpty();
            updateLoanToPreDisbursalState(loan);
            loanDisbursementService.moveDownPaymentFeesToExpectedDisbursementDate(loan, loan.getExpectedDisbursedOnLocalDate());
            loan.getDisbursementDetails().forEach(tranche -> loanDisbursementService.updateDownPaymentFeesOfTranche(loan, tranche));
            if (isScheduleRegenerateRequired || isDisbursedAmountChanged || isEmiAmountChanged
                    || loan.isInterestBearingAndInterestRecalculationEnabled()) {
                // clear off actual disbusrement date so schedule regeneration
                // uses expected date.

                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
                if (isDisbursedAmountChanged) {
                    loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
                }
            } else if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                for (final LoanRepaymentScheduleInstallment period : loan.getRepaymentScheduleInstallments()) {
                    period.resetAccrualComponents();
                }
            }

            if (loan.isTopup()) {
                loan.getLoanTopupDetails().setAccountTransferDetails(null);
                loan.getLoanTopupDetails().setTopupAmount(null);
            }

            loan.adjustNetDisbursalAmount(loan.getApprovedPrincipal());
            actualChanges.put(ACTUAL_DISBURSEMENT_DATE, "");
            loanBalanceService.updateLoanSummaryDerivedFields(loan);
        }

        return actualChanges;
    }

    public void updateLoanToPreDisbursalState(final Loan loan) {
        loan.setActualDisbursementDate(null);
        loan.setDisbursedBy(null);
        loan.setAccruedTill(null);
        reverseExistingTransactions(loan);
        BigDecimal principalForScheduleRegeneration = loan.getApprovedPrincipal();

        for (final LoanCharge charge : loan.getActiveCharges()) {
            if (charge.isOverdueInstallmentCharge()) {
                charge.setActive(false);
            } else {
                charge.resetToOriginal(loan.loanCurrency());
            }
        }

        loan.getLoanRepaymentScheduleDetail().setPrincipal(principalForScheduleRegeneration);

        final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.resetDerivedComponents();
        }

        for (LoanTermVariations variations : loan.getLoanTermVariations()) {
            if (variations.getOnLoanStatus().equals(LoanStatus.ACTIVE.getValue())) {
                variations.markAsInactive();
            }
        }

        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(loan.getCurrency(), loan.getDisbursementDate(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());

        loanBalanceService.refreshSummaryAndBalancesForDisbursedLoan(loan);
    }

    private void reverseExistingTransactions(final Loan loan) {
        final Collection<LoanTransaction> retainTransactions = new ArrayList<>();
        for (final LoanTransaction transaction : loan.getLoanTransactions()) {
            loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(transaction.getLoan(), transaction, "reversed");
            transaction.reverse();
            journalEntryPoster.postJournalEntriesForLoanTransaction(transaction, false, false);
            if (transaction.getId() != null) {
                retainTransactions.add(transaction);
            }
        }
        loan.getLoanTransactions().retainAll(retainTransactions);
    }

    private void updateDisbursementDateAndAmountForTranche(final Loan loan, final LoanDisbursementDetails disbursementDetails,
            final JsonCommand command, final Map<String, Object> actualChanges, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final Locale locale = command.extractLocale();
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed(LoanApiConstants.updatedDisbursementPrincipalParameterName,
                locale);
        final LocalDate expectedDisbursementDate = command
                .localDateValueOfParameterNamed(LoanApiConstants.updatedDisbursementDateParameterName);
        disbursementDetails.updateExpectedDisbursementDateAndAmount(expectedDisbursementDate, principal);
        actualChanges.put(LoanApiConstants.expectedDisbursementDateParameterName,
                command.stringValueOfParameterNamed(LoanApiConstants.expectedDisbursementDateParameterName));
        actualChanges.put(LoanApiConstants.disbursementIdParameterName,
                command.stringValueOfParameterNamed(LoanApiConstants.disbursementIdParameterName));
        actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName,
                command.bigDecimalValueOfParameterNamed(LoanApiConstants.disbursementPrincipalParameterName, locale));

        loan.getLoanRepaymentScheduleDetail().setPrincipal(loan.getPrincipalAmountForRepaymentSchedule());

        if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
        } else if (loan.isProgressiveSchedule()) {
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }

        reprocessLoanTransactionsService.reprocessTransactions(loan);
    }

    private Map<String, Object> undoLastDisbursal(final ScheduleGeneratorDTO scheduleGeneratorDTO, final Loan loan) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        List<LoanTransaction> loanTransactions = loan.retrieveListOfTransactionsByType(LoanTransactionType.DISBURSEMENT);
        loanTransactions.sort(Comparator.comparing(LoanTransaction::getId));
        final LoanTransaction lastDisbursalTransaction = loanTransactions.getLast();
        final LocalDate lastTransactionDate = lastDisbursalTransaction.getTransactionDate();

        loanTransactions = loanTransactionRepository.findNonReversedMonetaryTransactionsByLoan(loan);
        Collections.reverse(loanTransactions);
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (DateUtils.isBefore(lastTransactionDate, previousTransaction.getTransactionDate())
                    && (previousTransaction.isRepaymentLikeType() || previousTransaction.isWaiver()
                            || previousTransaction.isChargePayment())) {
                throw new UndoLastTrancheDisbursementException(previousTransaction.getId());
            }
            if (previousTransaction.getId().compareTo(lastDisbursalTransaction.getId()) < 0) {
                break;
            }
        }
        final LoanDisbursementDetails disbursementDetail = loan.getDisbursementDetails(lastTransactionDate,
                lastDisbursalTransaction.getAmount());
        loanBalanceService.updateLoanToLastDisbursalState(loan, disbursementDetail);
        loan.getLoanTermVariations()
                .removeIf(loanTermVariations -> (loanTermVariations.getTermType().isDueDateVariation()
                        && DateUtils.isAfter(loanTermVariations.fetchDateValue(), lastTransactionDate))
                        || (loanTermVariations.getTermType().isEMIAmountVariation()
                                && DateUtils.isEqual(loanTermVariations.getTermApplicableFrom(), lastTransactionDate))
                        || DateUtils.isAfter(loanTermVariations.getTermApplicableFrom(), lastTransactionDate));
        reverseExistingTransactionsTillLastDisbursal(loan, lastDisbursalTransaction);
        loanDisbursementService.deactivateDownPaymentFeesOfTranche(loan, disbursementDetail);
        loanScheduleService.regenerateScheduleWithReprocessingTransactions(loan, scheduleGeneratorDTO);
        actualChanges.put("undolastdisbursal", "true");
        actualChanges.put("disbursedAmount", loan.getDisbursedAmount());
        loanLifecycleStateMachine.determineAndTransition(loan, loan.getLastUserTransactionDate());

        return actualChanges;
    }

    /**
     * Reverse only disbursement, accruals, and repayments at disbursal transactions
     */
    private void reverseExistingTransactionsTillLastDisbursal(Loan loan, final LoanTransaction lastDisbursalTransaction) {
        LoanCharge chargeToDeactivate = null;

        for (final LoanCharge charge : loan.getCharges()) {
            if (charge.isTrancheDisbursementCharge()) {
                if (charge.getTrancheDisbursementCharge() != null
                        && charge.getTrancheDisbursementCharge().getloanDisbursementDetails() != null) {

                    LocalDate expectedDate = charge.getTrancheDisbursementCharge().getloanDisbursementDetails().expectedDisbursementDate();
                    LocalDate actualDate = charge.getTrancheDisbursementCharge().getloanDisbursementDetails().actualDisbursementDate();

                    if ((actualDate != null && actualDate.equals(lastDisbursalTransaction.getTransactionDate()))
                            || (expectedDate != null && expectedDate.equals(lastDisbursalTransaction.getTransactionDate()))) {
                        chargeToDeactivate = charge;
                        break;
                    }
                }
            }
        }

        if (chargeToDeactivate != null) {
            chargeToDeactivate.resetPaidAmount(loan.getCurrency());
            chargeToDeactivate.setActive(false);
        }

        // Now reverse all relevant transactions
        for (final LoanTransaction transaction : loan.getLoanTransactions()) {
            if (!DateUtils.isBefore(transaction.getTransactionDate(), lastDisbursalTransaction.getTransactionDate())
                    && transaction.getId().compareTo(lastDisbursalTransaction.getId()) >= 0
                    && transaction.isAllowTypeTransactionAtTheTimeOfLastUndo()) {
                loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(transaction.getLoan(), transaction, "reversed");
                transaction.reverse();
                journalEntryPoster.postJournalEntriesForLoanTransaction(transaction, false, false);
            }
        }

        loanBalanceService.updateLoanSummaryDerivedFields(loan);
    }

    private boolean canDisburse(final Loan loan) {
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_DISBURSED, loan);

        boolean isMultiTrancheDisburse = false;
        LoanStatus actualLoanStatus = loan.getStatus();
        if ((actualLoanStatus.isActive() || actualLoanStatus.isClosedObligationsMet() || actualLoanStatus.isOverpaid())
                && loan.isAllTranchesNotDisbursed()) {
            isMultiTrancheDisburse = true;
        }
        return !statusEnum.hasStateOf(actualLoanStatus) || isMultiTrancheDisburse;
    }
}
