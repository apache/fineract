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

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeAdjustmentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeAdjustmentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeRefundBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBePayedException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeRefundException;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pays loan charges from a linked savings account, adjusts them and refunds them.
 */
@RequiredArgsConstructor
public class LoanChargePaymentWritePlatformService {

    private final LoanChargeApiJsonValidator loanChargeApiJsonValidator;
    private final LoanAssembler loanAssembler;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanWritePlatformService loanWritePlatformService;
    private final LoanUtilService loanUtilService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final ExternalIdFactory externalIdFactory;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final NoteRepository noteRepository;
    private final LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanScheduleService loanScheduleService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanAccountService loanAccountService;
    private final LoanJournalEntryPoster loanJournalEntryPoster;
    private final LoanChargeWriteHelper loanChargeWriteHelper;

    @Transactional
    public CommandProcessingResult payLoanCharge(final Long loanId, Long loanChargeId, final JsonCommand command,
            final boolean isChargeIdIncludedInJson) {

        this.loanChargeApiJsonValidator.validateChargePaymentTransaction(command.json(), isChargeIdIncludedInJson);
        if (isChargeIdIncludedInJson) {
            loanChargeId = command.longValueOfParameterNamed("chargeId");
        }
        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        this.loanChargeWriteHelper.checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = this.loanChargeWriteHelper.retrieveLoanChargeBy(loanId, loanChargeId);
        final ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.LOAN_INACTIVE,
                    loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_WAIVED,
                    loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_PAID,
                    loanCharge.getId());
        }

        if (!loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            throw new LoanChargeCannotBePayedException(
                    LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.CHARGE_NOT_ACCOUNT_TRANSFER, loanCharge.getId());
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
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_WAIVED,
                        loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_PAID,
                        loanCharge.getId());
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
                AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, externalId, null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        Long transferTransactionId = this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        AccountTransferDetails transferDetails = this.accountTransferDetailRepository.findById(transferTransactionId)
                .orElseThrow(() -> new AccountTransferNotFoundException(transferTransactionId));
        LoanTransaction loanTransaction = transferDetails.getAccountTransferTransactions().get(0).getToLoanTransaction();
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withSubEntityId(loanTransaction.getId()) //
                .withSubEntityExternalId(loanTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withSavingsId(portfolioAccountData.getId()) //
                .build();
    }

    @Transactional
    public CommandProcessingResult adjustmentForLoanCharge(Long loanId, Long loanChargeId, JsonCommand command) {
        this.loanChargeApiJsonValidator.validateLoanChargeAdjustmentRequest(loanId, loanChargeId, command.json());

        final LoanCharge loanCharge = this.loanChargeWriteHelper.retrieveLoanChargeBy(loanId, loanChargeId);
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("amount");
        final ExternalId externalId = externalIdFactory.createFromCommand(command, "externalId");
        final String locale = command.locale();

        Map<String, Object> changes = new HashMap<>();
        changes.put("externalId", externalId);
        changes.put("amount", transactionAmount);
        changes.put("transactionDate", transactionDate);
        changes.put("locale", locale);

        loanChargeAdjustmentEntranceValidation(loanCharge, transactionAmount);
        final Loan loan = loanAssembler.assembleFromInUserScope(loanId);

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createPaymentDetail(command, changes);
        if (paymentDetail != null) {
            paymentDetail = this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
        }
        LoanTransaction loanTransaction = applyChargeAdjustment(loan, loanCharge, transactionAmount, transactionDate, externalId,
                paymentDetail);

        // Update loan transaction on repayment.
        loanAccountDomainService.updateAndSaveLoanCollateralTransactionsForIndividualAccounts(loan, loanTransaction);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanNote(loan, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
        }

        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargeAdjustmentPostBusinessEvent(loanTransaction));

        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .withEntityId(loanChargeId) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withSubEntityId(loanTransaction.getId()) //
                .withSubEntityExternalId(loanTransaction.getExternalId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    public CommandProcessingResult loanChargeRefund(final Long loanId, final JsonCommand command) {

        this.loanChargeApiJsonValidator.validateLoanChargeRefundTransaction(command.json());

        final Long loanChargeId = command.longValueOfParameterNamed("loanChargeId");
        final LoanCharge loanCharge = this.loanChargeWriteHelper.retrieveLoanChargeBy(loanId, loanChargeId);

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
        CommandProcessingResult result = loanWritePlatformService.makeLoanRepaymentWithChargeRefundChargeType(
                LoanTransactionType.CHARGE_REFUND, repaymentJsonCommand.getLoanId(), repaymentJsonCommand, isRecoveryRepayment,
                chargeRefundChargeType);

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
        if (newChargeRefundTxn != null) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(newChargeRefundTxn.getLoan()));
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargeRefundBusinessEvent(newChargeRefundTxn));
        return result;
    }

    private LoanTransaction applyChargeAdjustment(final Loan loan, final LoanCharge loanCharge, final BigDecimal transactionAmount,
            final LocalDate transactionDate, final ExternalId txnExternalId, PaymentDetail paymentDetail) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanChargeAdjustmentPreBusinessEvent(loan));

        LoanTransaction loanChargeAdjustmentTransaction = LoanTransaction.chargeAdjustment(loan, transactionAmount, transactionDate,
                txnExternalId, paymentDetail);
        LoanTransactionRelation loanTransactionRelation = LoanTransactionRelation.linkToCharge(loanChargeAdjustmentTransaction, loanCharge,
                LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT);
        loanChargeAdjustmentTransaction.getLoanTransactionRelations().add(loanTransactionRelation);

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            if (loan.isProgressiveSchedule()) {
                final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            }
            reprocessLoanTransactionsService.reprocessTransactions(loan, List.of(loanChargeAdjustmentTransaction));
        } else {
            reprocessLoanTransactionsService.processLatestTransaction(loanChargeAdjustmentTransaction, loan);
        }
        loan.addLoanTransaction(loanChargeAdjustmentTransaction);
        loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(loanChargeAdjustmentTransaction);
        loanLifecycleStateMachine.determineAndTransition(loan, loan.getLastUserTransactionDate());

        loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        loanJournalEntryPoster.postJournalEntriesForLoanTransaction(loanChargeAdjustmentTransaction, false, false);
        loanAccountDomainService.setLoanDelinquencyTag(loan, transactionDate);

        return loanChargeAdjustmentTransaction;
    }

    private void loanChargeAdjustmentEntranceValidation(final LoanCharge loanCharge, final BigDecimal transactionAmount) {
        final Loan loan = loanCharge.getLoan();
        loanChargeValidator.validateLoanStatusForChargeAdjustment(loan);
        loanChargeValidator.validateChargeAdjustmentAmount(loanCharge, transactionAmount, null);
        this.loanChargeWriteHelper.checkClientOrGroupActive(loan);
        loanDownPaymentTransactionValidator.validateAccountStatus(loan, LoanEvent.LOAN_CHARGE_ADJUSTMENT);
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
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();
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
        if (loanCharge.isInstalmentFee() && installmentChargeEntry != null) {
            final Integer installmentNumber = installmentChargeEntry.getRepaymentInstallment().getInstallmentNumber();
            chargeAmountPaid = installmentChargeEntry.getAmountPaid(loanCurrency).getAmount();
            for (LoanChargePaidBy loanChargePaidBy : loanCharge.getLoanChargePaidBySet()) {
                if (installmentNumber.equals(loanChargePaidBy.getInstallmentNumber()) && isRefundElementOfChargeRefund(loanChargePaidBy)) {
                    chargeAmountRefunded = chargeAmountRefunded.add(loanChargePaidBy.getAmount());
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
        // refund amount was provided.
        if (transactionAmount != null && transactionAmount.compareTo(refundableAmount) > 0) {
            final String errorMessage = "loan.charge.transaction.amount.is.more.than.is.refundable";
            final String details = "transactionAmount: " + transactionAmount + "  Refundable: " + refundableAmount;
            throw new LoanChargeRefundException(errorMessage, details);
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
}
