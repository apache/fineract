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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanChargebackTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestPaymentWaiverPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestPaymentWaiverPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionInterestRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWaiveInterestBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSubStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidPaidInAdvanceAmountException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanForeclosureException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentParameter;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanSupportedInterestRefundTypes;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class LoanTransactionWritePlatformServiceImpl implements LoanTransactionWritePlatformService {

    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAssembler loanAssembler;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanUtilService loanUtilService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanScheduleService loanScheduleService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanAccountService loanAccountService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanBalanceService loanBalanceService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final GLIMAccountInfoRepository glimRepository;
    private final LoanAdjustmentService loanAdjustmentService;
    private final GuarantorDomainService guarantorDomainService;
    private final LoanTransactionAssembler loanTransactionAssembler;
    private final LoanTransactionRelationRepository loanTransactionRelationRepository;
    private final LoanTransactionService loanTransactionService;
    private final LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    private final LoanDownPaymentHandlerService loanDownPaymentHandlerService;

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

        loan = this.loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        this.loanUtilService.createTransactionNote(loan, newInterestPaymentWaiverTransaction, noteText, null);

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
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {
        this.loanTransactionValidator.validateTransaction(command.json());
        LoanTransaction transactionToAdjust = this.loanTransactionRepository.findByIdAndLoanId(command.entityId(), command.getLoanId())
                .orElseThrow(() -> new LoanTransactionNotFoundException(command.entityId(), command.getLoanId()));

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        if (loan.getStatus().isClosed() && loan.getLoanSubStatus() != null && loan.getLoanSubStatus().equals(LoanSubStatus.FORECLOSED)) {
            final String defaultUserMessage = "The loan cannot reopened as it is foreclosed.";
            throw new LoanForeclosureException("loan.cannot.be.reopened.as.it.is.foreclosured", defaultUserMessage, loanId);
        }
        this.loanUtilService.checkClientOrGroupActive(loan);

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
        this.loanUtilService.checkClientOrGroupActive(loan);

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

        loan = this.loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed(LoanApiConstants.noteParamName);
        this.loanUtilService.createTransactionNote(loan, newTransaction, noteText, changes);

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
        this.loanUtilService.checkClientOrGroupActive(loan);

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
        loan = this.loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        final String noteText = command.stringValueOfParameterNamed("note");
        this.loanUtilService.createTransactionNote(loan, waiveInterestTransaction, noteText, changes);

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

    private void waiveInterest(final Loan loan, final LoanTransaction waiveInterestTransaction,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        loanDownPaymentHandlerService.handleRepaymentOrRecoveryOrWaiverTransaction(loan, waiveInterestTransaction, null,
                scheduleGeneratorDTO);
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
    public CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command) {
        this.loanTransactionValidator.validateNewRefundTransaction(command.json());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

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
        this.loanUtilService.createNote(loan, command, changes);
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

        loan = this.loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
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

        this.loanUtilService.createNote(loan, command, changes);

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

        loan = this.loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
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

    /* package */ void handleChargebackTransaction(final Loan loan, LoanTransaction chargebackTransaction) {
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
}
