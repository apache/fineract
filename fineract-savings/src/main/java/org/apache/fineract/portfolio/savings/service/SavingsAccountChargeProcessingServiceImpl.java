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
package org.apache.fineract.portfolio.savings.service;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargePaidBy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsEvent;

/**
 * Default implementation of {@link SavingsAccountChargeProcessingService}. The method bodies were extracted from
 * {@code SavingsAccount}; behaviour is intentionally unchanged. Account state is read/written through the public API of
 * the {@link SavingsAccount} entity, and the summary recalculation it triggers relies only on the stateless
 * {@code SavingsAccountTransactionSummaryWrapper} helper, so the charge orchestration no longer lives on the domain
 * entity.
 */
public class SavingsAccountChargeProcessingServiceImpl implements SavingsAccountChargeProcessingService {

    @Override
    public void addCharge(final SavingsAccount account, final DateTimeFormatter formatter, final SavingsAccountCharge savingsAccountCharge,
            final Charge chargeDefinition) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (account.isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (!account.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            baseDataValidator.reset()
                    .failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.currency.and.charge.currency.not.same");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final LocalDate chargeDueDate = savingsAccountCharge.getDueDate();

        if (savingsAccountCharge.isOnSpecifiedDueDate()) {
            if (DateUtils.isBefore(chargeDueDate, account.getActivatedOnDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(account.getActivatedOnDate().format(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("before.activationDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            } else if (DateUtils.isBefore(chargeDueDate, account.getSubmittedOnDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(account.getSubmittedOnDate().format(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("before.submittedOnDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (savingsAccountCharge.isSavingsActivation()
                && !(account.isSubmittedAndPendingApproval() || (account.isApproved() && account.isNotActive()))) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.valid.account.status.cannot.add.activation.time.charge");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        // Only one annual fee is supported per account
        if (savingsAccountCharge.isAnnualFee()) {
            if (isAnnualFeeExists(account)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("multiple.annual.fee.per.account.not.supported");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

        }

        if (savingsAccountCharge.isAnnualFee() || savingsAccountCharge.isMonthlyFee() || savingsAccountCharge.isWeeklyFee()) {
            // update due date
            if (account.isActive()) {
                savingsAccountCharge.updateToNextDueDateFrom(account.getActivatedOnDate());
            } else if (account.isApproved()) {
                savingsAccountCharge.updateToNextDueDateFrom(account.getApprovedOnDate());
            }
        }

        // activation charge and withdrawal charges not required this validation
        if (savingsAccountCharge.isOnSpecifiedDueDate()) {
            account.validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLY_CHARGE, chargeDueDate);
        }

        // add new charge to savings account
        account.charges().add(savingsAccountCharge);
    }

    @Override
    public void removeCharge(final SavingsAccount account, final SavingsAccountCharge charge) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (account.isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("delete.transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (account.isActive() || account.isApproved()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("delete.transaction.invalid.account.is.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        account.charges().remove(charge);
    }

    @Override
    public void waiveCharge(final SavingsAccount account, final Long savingsAccountChargeId, final boolean backdatedTxnsAllowedTill) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (account.isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (account.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.not.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final SavingsAccountCharge savingsAccountCharge = account.getCharge(savingsAccountChargeId);

        if (savingsAccountCharge.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("charge.is.not.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (savingsAccountCharge.isWithdrawalFee()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.waiver.of.withdrawal.fee.not.supported");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // validate charge is not already paid or waived
        if (savingsAccountCharge.isWaived()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.already.waived");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        } else if (savingsAccountCharge.isPaid()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.paid");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // waive charge
        final Money amountWaived = savingsAccountCharge.waive(account.getCurrency());
        handleWaiverChargeTransactions(account, savingsAccountCharge, amountWaived, backdatedTxnsAllowedTill);
    }

    @Override
    public SavingsAccountTransaction payCharge(final SavingsAccount account, final SavingsAccountCharge savingsAccountCharge,
            final BigDecimal amountPaid, final LocalDate transactionDate, final DateTimeFormatter formatter,
            final boolean backdatedTxnsAllowedTill, final String refNo) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (account.isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (account.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.not.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (savingsAccountCharge.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("charge.is.not.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (DateUtils.isBefore(transactionDate, account.getActivatedOnDate())) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(account.getActivatedOnDate().format(formatter))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.before.activationDate");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isDateInTheFuture(transactionDate)) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate.format(formatter))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.is.futureDate");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (savingsAccountCharge.isSavingsActivation()) {
            baseDataValidator.reset()
                    .failWithCodeNoParameterAddedToErrorCode("transaction.not.valid.cannot.pay.activation.time.charge.is.automated");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (savingsAccountCharge.isAnnualFee()) {
            final LocalDate annualFeeDueDate = savingsAccountCharge.getDueDate();
            if (annualFeeDueDate == null) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.annualfee.settings");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            if (DateUtils.isBefore(transactionDate, annualFeeDueDate)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.before.dueDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            LocalDate currentAnnualFeeNextDueDate = account.findLatestAnnualFeeTransactionDueDate();
            if (DateUtils.isEqual(currentAnnualFeeNextDueDate, transactionDate)) {
                baseDataValidator.reset().parameter("dueDate").value(transactionDate.format(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("transaction.exists.on.date");

                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // validate charge is not already paid or waived
        if (savingsAccountCharge.isWaived()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.already.waived");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        } else if (savingsAccountCharge.isPaid()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.paid");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final Money chargePaid = Money.of(account.getCurrency(), amountPaid);
        if (!savingsAccountCharge.getAmountOutstanding(account.getCurrency()).isGreaterThanOrEqualTo(chargePaid)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.charge.amount.paid.in.access");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        return payCharge(account, savingsAccountCharge, chargePaid, transactionDate, backdatedTxnsAllowedTill, refNo);
    }

    @Override
    public SavingsAccountTransaction payCharge(final SavingsAccount account, final SavingsAccountCharge savingsAccountCharge,
            final Money amountPaid, final LocalDate transactionDate, final boolean backdatedTxnsAllowedTill, final String refNo) {
        savingsAccountCharge.pay(account.getCurrency(), amountPaid);
        return handlePayChargeTransactions(account, savingsAccountCharge, amountPaid, transactionDate, backdatedTxnsAllowedTill, refNo);
    }

    @Override
    public void payWithdrawalFee(final SavingsAccount account, final BigDecimal transactionAmount, final LocalDate transactionDate,
            final PaymentDetail paymentDetail, final boolean backdatedTxnsAllowedTill, final String refNo) {
        for (SavingsAccountCharge charge : account.charges()) {
            if (charge.isWithdrawalFee() && charge.isActive()) {

                if (charge.getFreeWithdrawalCount() == null) {
                    charge.setFreeWithdrawalCount(0);
                }

                if (charge.isEnablePaymentType() && charge.isEnableFreeWithdrawal()) { // discount transaction to
                                                                                       // specific paymentType
                    if (paymentDetail != null && paymentDetail.getPaymentType() != null
                            && paymentDetail.getPaymentType().getName().equals(charge.getCharge().getPaymentType().getName())) {
                        resetFreeChargeDaysCount(account, charge, transactionAmount, transactionDate, refNo);
                    }
                } else if (charge.isEnablePaymentType()) { // normal charge-transaction to specific paymentType
                    if (paymentDetail != null && paymentDetail.getPaymentType() != null
                            && paymentDetail.getPaymentType().getName().equals(charge.getCharge().getPaymentType().getName())) {
                        charge.updateWithdralFeeAmount(transactionAmount);
                        payCharge(account, charge, charge.getAmountOutstanding(account.getCurrency()), transactionDate,
                                backdatedTxnsAllowedTill, refNo);
                    }
                } else if (!charge.isEnablePaymentType() && charge.isEnableFreeWithdrawal()) { // discount transaction
                                                                                               // irrespective of
                                                                                               // PaymentTypes.
                    resetFreeChargeDaysCount(account, charge, transactionAmount, transactionDate, refNo);

                } else { // normal-withdraw
                    charge.updateWithdralFeeAmount(transactionAmount);
                    payCharge(account, charge, charge.getAmountOutstanding(account.getCurrency()), transactionDate,
                            backdatedTxnsAllowedTill, refNo);
                }
            }
        }
    }

    @Override
    public void setSubStatusInactive(final SavingsAccount account, final boolean backdatedTxnsAllowedTill) {
        account.markSubStatusInactive();
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        for (SavingsAccountCharge charge : account.charges()) {
            if (charge.isSavingsNoActivity() && charge.isActive()) {
                charge.updateWithdralFeeAmount(account.getAccountBalance());
                UUID refNo = UUID.randomUUID();
                payCharge(account, charge, charge.getAmountOutstanding(account.getCurrency()), transactionDate, backdatedTxnsAllowedTill,
                        refNo.toString());
            }
        }
        boolean postReversals = false;
        account.recalculateDailyBalances(Money.zero(account.getCurrency()), transactionDate, backdatedTxnsAllowedTill, postReversals);
        account.getSummary().updateSummary(account.getCurrency(), account.getTransactions());
    }

    private void resetFreeChargeDaysCount(final SavingsAccount account, final SavingsAccountCharge charge,
            final BigDecimal transactionAmount, final LocalDate transactionDate, final String refNo) {
        LocalDate resetDate = charge.getResetChargeDate();

        Integer restartPeriod = charge.getRestartFrequency();
        if (charge.getRestartFrequencyEnum() == 2) { // calculation for months
            LocalDate localDate = DateUtils.getBusinessLocalDate();

            LocalDate resetLocalDate;
            if (resetDate == null) {
                resetLocalDate = account.getActivatedOnDate();
            } else {
                resetLocalDate = resetDate;
            }

            LocalDate gapIntervalMonth = resetLocalDate.plusMonths(restartPeriod);

            YearMonth gapYearMonth = YearMonth.from(gapIntervalMonth);
            YearMonth localYearMonth = YearMonth.from(localDate);
            if (localYearMonth.isBefore(gapYearMonth)) {
                countValidation(account, charge, transactionAmount, transactionDate, refNo);
            } else {
                discountCharge(1, charge);
            }
        } else { // calculation for days
            long completedDays;

            if (resetDate == null) {
                completedDays = ChronoUnit.DAYS.between(DateUtils.getBusinessLocalDate(), account.getActivatedOnDate());

            } else {
                completedDays = ChronoUnit.DAYS.between(DateUtils.getBusinessLocalDate(), resetDate);
            }

            int totalDays = (int) completedDays;

            if (totalDays < restartPeriod) {
                countValidation(account, charge, transactionAmount, transactionDate, refNo);
            } else {
                discountCharge(1, charge);
            }
        }
    }

    private void countValidation(final SavingsAccount account, final SavingsAccountCharge charge, final BigDecimal transactionAmount,
            final LocalDate transactionDate, final String refNo) {
        boolean backdatedTxnsAllowedTill = false;
        if (charge.getFreeWithdrawalCount() < charge.getFrequencyFreeWithdrawalCharge()) {
            final Integer count = charge.getFreeWithdrawalCount() + 1;
            charge.setFreeWithdrawalCount(count);
            charge.updateNoWithdrawalFee();
        } else {
            charge.updateWithdralFeeAmount(transactionAmount);
            payCharge(account, charge, charge.getAmountOutstanding(account.getCurrency()), transactionDate, backdatedTxnsAllowedTill,
                    refNo);
        }
    }

    private void discountCharge(final Integer freeWithdrawalCount, final SavingsAccountCharge charge) {
        charge.setFreeWithdrawalCount(freeWithdrawalCount);
        charge.setDiscountDueDate(DateUtils.getBusinessLocalDate());
        charge.updateNoWithdrawalFee();
    }

    private SavingsAccountTransaction handlePayChargeTransactions(final SavingsAccount account,
            final SavingsAccountCharge savingsAccountCharge, final Money transactionAmount, final LocalDate transactionDate,
            final boolean backdatedTxnsAllowedTill, final String refNo) {
        SavingsAccountTransaction chargeTransaction;

        if (savingsAccountCharge.isWithdrawalFee()) {
            chargeTransaction = SavingsAccountTransaction.withdrawalFee(account, account.office(), transactionDate, transactionAmount,
                    refNo);
        } else if (savingsAccountCharge.isAnnualFee()) {
            chargeTransaction = SavingsAccountTransaction.annualFee(account, account.office(), transactionDate, transactionAmount);
        } else {
            chargeTransaction = SavingsAccountTransaction.charge(account, account.office(), transactionDate, transactionAmount);
        }

        handleChargeTransactions(account, savingsAccountCharge, chargeTransaction, backdatedTxnsAllowedTill);
        return chargeTransaction;
    }

    private void handleWaiverChargeTransactions(final SavingsAccount account, final SavingsAccountCharge savingsAccountCharge,
            final Money transactionAmount, final boolean backdatedTxnsAllowedTill) {
        final SavingsAccountTransaction chargeTransaction = SavingsAccountTransaction.waiver(account, account.office(),
                DateUtils.getBusinessLocalDate(), transactionAmount);
        handleChargeTransactions(account, savingsAccountCharge, chargeTransaction, backdatedTxnsAllowedTill);
    }

    private void handleChargeTransactions(final SavingsAccount account, final SavingsAccountCharge savingsAccountCharge,
            final SavingsAccountTransaction transaction, final boolean backdatedTxnsAllowedTill) {
        // Provide a link between transaction and savings charge for which
        // amount is waived.
        final SavingsAccountChargePaidBy chargePaidBy = SavingsAccountChargePaidBy.instance(transaction, savingsAccountCharge,
                transaction.getAmount(account.getCurrency()).getAmount());
        transaction.getSavingsAccountChargesPaid().add(chargePaidBy);
        if (backdatedTxnsAllowedTill) {
            account.addTransactionToExisting(transaction);
            account.getSummary().updateSummaryWithPivotConfig(account.getCurrency(), transaction,
                    account.getSavingsAccountTransactionsWithPivotConfig());
        } else {
            account.addTransaction(transaction);
        }
    }

    private boolean isAnnualFeeExists(final SavingsAccount account) {
        for (SavingsAccountCharge charge : account.charges()) {
            if (charge.isAnnualFee()) {
                return true;
            }
        }
        return false;
    }
}
