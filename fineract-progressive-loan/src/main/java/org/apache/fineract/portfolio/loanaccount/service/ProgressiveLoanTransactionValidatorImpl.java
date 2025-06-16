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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.common.service.Validator;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Slf4j
@RequiredArgsConstructor
public class ProgressiveLoanTransactionValidatorImpl implements ProgressiveLoanTransactionValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public void validateCapitalizedIncome(final JsonCommand command, final Long loanId) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, getCapitalizedIncomeParameters());

        Validator.validateOrThrow("loan.capitalized.income", baseDataValidator -> {
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            validateLoanClientIsActive(loan);
            validateLoanGroupIsActive(loan);

            // Validate that loan is disbursed
            if (!loan.isDisbursed()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("capitalized.income.only.after.disbursement",
                        "Capitalized income can be added to the loan only after Disbursement");
            }

            // Validate loan is progressive
            if (!loan.isProgressiveSchedule()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.progressive.loan");
            }

            // Validate income capitalization is enabled
            if (!loan.getLoanProductRelatedDetail().isEnableIncomeCapitalization()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("income.capitalization.not.enabled");
            }

            // Validate loan is active, or closed or overpaid
            final LoanStatus loanStatus = loan.getStatus();
            if (!loanStatus.isActive() && !loanStatus.isClosed() && !loanStatus.isOverpaid()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.valid.loan.status");
            }

            final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
            baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

            // Validate transaction date is not before disbursement date
            if (transactionDate != null && loan.getDisbursementDate() != null && transactionDate.isBefore(loan.getDisbursementDate())) {
                baseDataValidator.reset().parameter("transactionDate").failWithCode("before.disbursement.date",
                        "Transaction date cannot be before disbursement date");
            }

            // Validate transaction date is not in the future
            if (transactionDate != null && transactionDate.isAfter(DateUtils.getBusinessLocalDate())) {
                baseDataValidator.reset().parameter("transactionDate").failWithCode("cannot.be.in.the.future",
                        "Transaction date cannot be in the future");
            }

            final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
            baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();

            // Validate total disbursement + capitalized income <= applied amount
            if (transactionAmount != null) {
                final BigDecimal totalDisbursed = loan.getDisbursedAmount();
                final BigDecimal capitalizedIncome = loan.getSummary().getTotalCapitalizedIncome();
                final BigDecimal newTotal = totalDisbursed.add(capitalizedIncome).add(transactionAmount);

                if (loan.loanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
                    final BigDecimal maxAppliedAmount = getOverAppliedMax(loan);
                    if (newTotal.compareTo(maxAppliedAmount) > 0) {
                        baseDataValidator.reset().parameter("transactionAmount").failWithCode("exceeds.approved.amount",
                                "Sum of disbursed amount and capitalized income can't be greater than maximum applied loan amount calculation.");
                    }
                } else {
                    if (newTotal.compareTo(loan.getApprovedPrincipal()) > 0) {
                        baseDataValidator.reset().parameter("transactionAmount").failWithCode("exceeds.approved.amount",
                                "Sum of disbursed amount and capitalized income can't be greater than approved loan principal.");
                    }
                }
            }

            validatePaymentDetails(baseDataValidator, element);
            validateNote(baseDataValidator, element);
            validateExternalId(baseDataValidator, element);
        });
    }

    @Override
    public void validateCapitalizedIncomeAdjustment(JsonCommand command, Long loanId, Long capitalizedIncomeTransactionId) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, getCapitalizedIncomeAdjustmentParameters());

        Validator.validateOrThrow("loan.capitalizedIncomeAdjustment", baseDataValidator -> {
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            validateLoanClientIsActive(loan);
            validateLoanGroupIsActive(loan);

            // Validate loan is progressive
            if (!loan.isProgressiveSchedule()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.progressive.loan");
            }

            // Validate income capitalization is enabled
            if (!loan.getLoanProductRelatedDetail().isEnableIncomeCapitalization()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("income.capitalization.not.enabled");
            }

            // Validate loan is active, or closed or overpaid
            final LoanStatus loanStatus = loan.getStatus();
            if (!loanStatus.isActive() && !loanStatus.isClosed() && !loanStatus.isOverpaid()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.valid.loan.status");
            }

            final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
            baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

            // Validate transaction date is not before disbursement date
            if (transactionDate != null && loan.getDisbursementDate() != null && transactionDate.isBefore(loan.getDisbursementDate())) {
                baseDataValidator.reset().parameter("transactionDate").failWithCode("before.disbursement.date",
                        "Transaction date cannot be before disbursement date");
            }

            // Validate transaction date is not in the future
            if (transactionDate != null && transactionDate.isAfter(DateUtils.getBusinessLocalDate())) {
                baseDataValidator.reset().parameter("transactionDate").failWithCode("cannot.be.in.the.future",
                        "Transaction date cannot be in the future");
            }

            final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
            baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();

            Optional<LoanTransaction> capitalizedIncomeTransactionOpt = loanTransactionRepository.findById(capitalizedIncomeTransactionId);
            if (capitalizedIncomeTransactionOpt.isEmpty()) {
                baseDataValidator.reset().parameter("capitalizedIncomeTransactionId").failWithCode("loan.transaction.not.found",
                        "Capitalized Income transaction not found.");
            } else {
                // Validate not before capitalized income transaction
                if (transactionDate != null && transactionDate.isBefore(capitalizedIncomeTransactionOpt.get().getTransactionDate())) {
                    baseDataValidator.reset().parameter("transactionDate").failWithCode("before.capitalizedIncome.transaction.date",
                            "Transaction date cannot be before capitalized income transaction date");

                }
                if (transactionAmount != null) {
                    LoanCapitalizedIncomeBalance capitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository
                            .findByLoanIdAndLoanTransactionId(loanId, capitalizedIncomeTransactionId);
                    if (MathUtil.isLessThan(capitalizedIncomeBalance.getAmount()
                            .subtract(MathUtil.nullToZero(capitalizedIncomeBalance.getAmountAdjustment())), transactionAmount)) {
                        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).failWithCode(
                                "cannot.be.more.than.remaining.amount",
                                " Capitalized income adjustment amount cannot be more than remaining amount");
                    }
                }
            }

            validatePaymentDetails(baseDataValidator, element);
            validateNote(baseDataValidator, element);
            validateExternalId(baseDataValidator, element);
        });
    }

    @Override
    public void validateContractTerminationUndo(final JsonCommand command, final Long loanId) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, getContractTerminationUndoParameters());

        Validator.validateOrThrow("loan.contract.termination.undo", baseDataValidator -> {
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            validateLoanClientIsActive(loan);
            validateLoanGroupIsActive(loan);

            if (!loan.isOpen()) {
                throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.active",
                        "Loan: " + loanId + " Undo Contract Termination is not allowed. Loan Account is not Active", loanId);
            }
            if (!loan.isContractTermination()) {
                throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.contract.terminated",
                        "Loan: " + loanId + " is not contract terminated", loanId);
            }
            final LoanTransaction contractTerminationTransaction = loan.findContractTerminationTransaction();
            if (contractTerminationTransaction == null) {
                throw new GeneralPlatformDomainRuleException("error.msg.loan.contract.termination.transaction.not.found",
                        "Loan: " + loanId + " contract termination transaction was not found", loanId);
            }
            if (!contractTerminationTransaction.equals(loan.getLastUserTransaction())) {
                throw new GeneralPlatformDomainRuleException("error.msg.loan.contract.termination.is.not.the.last.user.transaction",
                        "Loan: " + loanId
                                + " contract termination cannot be undone. User transaction was found after contract termination!",
                        loanId);
            }

            validateNote(baseDataValidator, element);
            validateReversalExternalId(baseDataValidator, element);
        });
    }

    // Delegates
    @Override
    public void validateDisbursement(JsonCommand command, boolean isAccountTransfer, Long loanId) {
        loanTransactionValidator.validateDisbursement(command, isAccountTransfer, loanId);
    }

    @Override
    public void validateUndoChargeOff(String json) {
        loanTransactionValidator.validateUndoChargeOff(json);
    }

    @Override
    public void validateTransaction(String json) {
        loanTransactionValidator.validateTransaction(json);
    }

    @Override
    public void validateChargebackTransaction(String json) {
        loanTransactionValidator.validateChargebackTransaction(json);
    }

    @Override
    public void validateNewRepaymentTransaction(String json) {
        loanTransactionValidator.validateNewRepaymentTransaction(json);
    }

    @Override
    public void validateTransactionWithNoAmount(String json) {
        loanTransactionValidator.validateTransactionWithNoAmount(json);
    }

    @Override
    public void validateChargeOffTransaction(String json) {
        loanTransactionValidator.validateChargeOffTransaction(json);
    }

    @Override
    public void validateUpdateOfLoanOfficer(String json) {
        loanTransactionValidator.validateUpdateOfLoanOfficer(json);
    }

    @Override
    public void validateForBulkLoanReassignment(String json) {
        loanTransactionValidator.validateForBulkLoanReassignment(json);
    }

    @Override
    public void validateMarkAsFraudLoan(String json) {
        loanTransactionValidator.validateMarkAsFraudLoan(json);
    }

    @Override
    public void validateUpdateDisbursementDateAndAmount(String json, LoanDisbursementDetails loanDisbursementDetails) {
        loanTransactionValidator.validateUpdateDisbursementDateAndAmount(json, loanDisbursementDetails);
    }

    @Override
    public void validateNewRefundTransaction(String json) {
        loanTransactionValidator.validateNewRefundTransaction(json);

    }

    @Override
    public void validateLoanForeclosure(String json) {
        loanTransactionValidator.validateLoanForeclosure(json);
    }

    @Override
    public void validateLoanClientIsActive(Loan loan) {
        loanTransactionValidator.validateLoanClientIsActive(loan);
    }

    @Override
    public void validateLoanGroupIsActive(Loan loan) {
        loanTransactionValidator.validateLoanGroupIsActive(loan);
    }

    @Override
    public void validateActivityNotBeforeLastTransactionDate(Loan loan, LocalDate activityDate, LoanEvent event) {
        loanTransactionValidator.validateActivityNotBeforeLastTransactionDate(loan, activityDate, event);
    }

    @Override
    public void validateRepaymentDateIsOnNonWorkingDay(LocalDate repaymentDate, WorkingDays workingDays,
            boolean allowTransactionsOnNonWorkingDay) {
        loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(repaymentDate, workingDays, allowTransactionsOnNonWorkingDay);
    }

    @Override
    public void validateRepaymentDateIsOnHoliday(LocalDate repaymentDate, boolean allowTransactionsOnHoliday, List<Holiday> holidays) {
        loanTransactionValidator.validateRepaymentDateIsOnHoliday(repaymentDate, allowTransactionsOnHoliday, holidays);
    }

    @Override
    public void validateLoanTransactionInterestPaymentWaiver(JsonCommand command) {
        loanTransactionValidator.validateLoanTransactionInterestPaymentWaiver(command);
    }

    @Override
    public void validateLoanTransactionInterestPaymentWaiverAfterRecalculation(Loan loan) {
        loanTransactionValidator.validateLoanTransactionInterestPaymentWaiverAfterRecalculation(loan);
    }

    @Override
    public void validateRefund(String json) {
        loanTransactionValidator.validateRefund(json);
    }

    @Override
    public void validateRefund(Loan loan, LoanTransactionType loanTransactionType, LocalDate transactionDate,
            ScheduleGeneratorDTO scheduleGeneratorDTO) {
        loanTransactionValidator.validateRefund(loan, loanTransactionType, transactionDate, scheduleGeneratorDTO);
    }

    @Override
    public void validateRefundDateIsAfterLastRepayment(Loan loan, LocalDate refundTransactionDate) {
        loanTransactionValidator.validateRefundDateIsAfterLastRepayment(loan, refundTransactionDate);
    }

    @Override
    public void validateActivityNotBeforeClientOrGroupTransferDate(Loan loan, LoanEvent event, LocalDate activityDate) {
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, event, activityDate);
    }

    @Override
    public void validatePaymentDetails(DataValidatorBuilder baseDataValidator, JsonElement element) {
        loanTransactionValidator.validatePaymentDetails(baseDataValidator, element);
    }

    @Override
    public void validateIfTransactionIsChargeback(LoanTransaction chargebackTransaction) {
        loanTransactionValidator.validateIfTransactionIsChargeback(chargebackTransaction);
    }

    @Override
    public void validateLoanRescheduleDate(Loan loan) {
        loanTransactionValidator.validateLoanRescheduleDate(loan);
    }

    @Override
    public void validateNote(DataValidatorBuilder baseDataValidator, JsonElement element) {
        loanTransactionValidator.validateNote(baseDataValidator, element);
    }

    @Override
    public void validateExternalId(DataValidatorBuilder baseDataValidator, JsonElement element) {
        loanTransactionValidator.validateExternalId(baseDataValidator, element);
    }

    @Override
    public void validateReversalExternalId(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        loanTransactionValidator.validateReversalExternalId(baseDataValidator, element);
    }

    private Set<String> getCapitalizedIncomeParameters() {
        return new HashSet<>(
                Arrays.asList("transactionDate", "dateFormat", "locale", "transactionAmount", "paymentTypeId", "note", "externalId"));
    }

    private Set<String> getCapitalizedIncomeAdjustmentParameters() {
        return new HashSet<>(
                Arrays.asList("transactionDate", "dateFormat", "locale", "transactionAmount", "paymentTypeId", "note", "externalId"));
    }

    private Set<String> getContractTerminationUndoParameters() {
        return new HashSet<>(Arrays.asList("note", "reversalExternalId"));
    }

    private BigDecimal getOverAppliedMax(final Loan loan) {
        final LoanProduct loanProduct = loan.getLoanProduct();
        if ("percentage".equals(loanProduct.getOverAppliedCalculationType())) {
            final BigDecimal overAppliedNumber = BigDecimal.valueOf(loanProduct.getOverAppliedNumber());
            final BigDecimal totalPercentage = BigDecimal.valueOf(1)
                    .add(overAppliedNumber.divide(BigDecimal.valueOf(100L), MoneyHelper.getMathContext()));
            return loan.getProposedPrincipal().multiply(totalPercentage);
        } else {
            return loan.getProposedPrincipal().add(BigDecimal.valueOf(loanProduct.getOverAppliedNumber()));
        }
    }
}
