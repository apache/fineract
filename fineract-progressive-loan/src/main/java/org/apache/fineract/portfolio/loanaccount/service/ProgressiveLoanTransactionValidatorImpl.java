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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.common.service.Validator;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;

@Slf4j
@RequiredArgsConstructor
public class ProgressiveLoanTransactionValidatorImpl implements ProgressiveLoanTransactionValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;

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

            // Validate loan is active
            if (!loan.getStatus().isActive()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.active");
            }

            final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
            baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull();

            // Validate transaction date is not before disbursement date
            if (transactionDate != null && loan.getDisbursementDate() != null && transactionDate.isBefore(loan.getDisbursementDate())) {
                baseDataValidator.reset().parameter("transactionDate").failWithCode("before.disbursement.date",
                        "Transaction date cannot be before disbursement date");
            }

            final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("transactionAmount", element);
            baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).notNull().positiveAmount();

            // Validate total disbursement + capitalized income <= approved amount
            if (transactionAmount != null) {
                final BigDecimal totalDisbursed = loan.getDisbursedAmount();
                final BigDecimal existingCapitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository.findAllByLoanId(loanId).stream()
                        .map(LoanCapitalizedIncomeBalance::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                final BigDecimal approvedAmount = loan.getApprovedPrincipal();
                final BigDecimal newTotal = totalDisbursed.add(existingCapitalizedIncomeBalance).add(transactionAmount);

                if (newTotal.compareTo(approvedAmount) > 0) {
                    baseDataValidator.reset().parameter("transactionAmount").failWithCode("exceeds.approved.amount",
                            "Sum of disbursed amount and capitalized income cannot exceed approved amount");
                }
            }

            validatePaymentDetails(baseDataValidator, element);
            validateNote(baseDataValidator, element);
            validateExternalId(baseDataValidator, element);
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

    private Set<String> getCapitalizedIncomeParameters() {
        return new HashSet<>(
                Arrays.asList("transactionDate", "dateFormat", "locale", "transactionAmount", "paymentTypeId", "note", "externalId"));
    }
}
