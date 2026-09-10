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
package org.apache.fineract.portfolio.workingcapitalloan.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanChargeDataValidator {

    private final FromJsonHelper fromJsonHelper;

    public void validateChargeAdjustmentRequest(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> allowedParameters = new HashSet<>(
                Arrays.asList(WorkingCapitalLoanChargeConstants.amountParamName, WorkingCapitalLoanChargeConstants.externalIdParamName,
                        WorkingCapitalLoanChargeConstants.localeParamName, WorkingCapitalLoanChargeConstants.dateFormatParamName,
                        WorkingCapitalLoanChargeConstants.noteParamName, WorkingCapitalLoanChargeConstants.paymentDetailsParamName,
                        WorkingCapitalLoanChargeConstants.paymentTypeIdParamName, WorkingCapitalLoanChargeConstants.accountNumberParamName,
                        WorkingCapitalLoanChargeConstants.checkNumberParamName, WorkingCapitalLoanChargeConstants.routingCodeParamName,
                        WorkingCapitalLoanChargeConstants.receiptNumberParamName, WorkingCapitalLoanChargeConstants.bankNumberParamName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromJsonHelper.checkForUnsupportedParameters(typeOfMap, json, allowedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("workingCapitalLoanChargeAdjustment");

        final JsonElement element = this.fromJsonHelper.parse(json);

        final BigDecimal amount = this.fromJsonHelper.extractBigDecimalWithLocaleNamed(WorkingCapitalLoanChargeConstants.amountParamName,
                element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.amountParamName).value(amount).notNull().positiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateCreateLoanCharge(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> allowedParameters = new HashSet<>(
                Arrays.asList(WorkingCapitalLoanChargeConstants.chargeIdParamName, WorkingCapitalLoanChargeConstants.dueDateParamName,
                        WorkingCapitalLoanChargeConstants.amountParamName, WorkingCapitalLoanChargeConstants.externalIdParamName,
                        WorkingCapitalLoanChargeConstants.localeParamName, WorkingCapitalLoanChargeConstants.dateFormatParamName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromJsonHelper.checkForUnsupportedParameters(typeOfMap, json, allowedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("workingCapitalLoanCharge");

        final JsonElement element = this.fromJsonHelper.parse(json);
        final Long chargeId = this.fromJsonHelper.extractLongNamed(WorkingCapitalLoanChargeConstants.chargeIdParamName, element);
        baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.chargeIdParamName).value(chargeId).notNull()
                .integerGreaterThanZero();

        // Mandatory for specified-due-date charges; optional for disbursement charges, which fall back to the charge
        // definition.
        // Which one applies is only known once the charge is loaded, so the type-specific rule lives in
        // validateCreateLoanChargeAgainstLoan.
        if (this.fromJsonHelper.parameterExists(WorkingCapitalLoanChargeConstants.amountParamName, element)) {
            final BigDecimal amount = this.fromJsonHelper
                    .extractBigDecimalWithLocaleNamed(WorkingCapitalLoanChargeConstants.amountParamName, element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.amountParamName).value(amount).notNull().positiveAmount();
        }

        if (this.fromJsonHelper.parameterExists(WorkingCapitalLoanChargeConstants.dueDateParamName, element)) {
            final LocalDate dueDate = this.fromJsonHelper.extractLocalDateNamed(WorkingCapitalLoanChargeConstants.dueDateParamName,
                    element);
            baseDataValidator.reset().parameter(WorkingCapitalLoanChargeConstants.dueDateParamName).value(dueDate).notBlank();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    /**
     * Rules that depend on the charge definition and the loan, so they run after both are loaded.
     *
     * <p>
     * A specified-due-date charge needs an amount and a future due date, and only fits an active (or closed / overpaid,
     * to reopen) loan. A disbursement charge is settled when the loan is disbursed, so it is only accepted while the
     * loan has not been disbursed yet, never carries a due date of its own, and may omit the amount to use the one of
     * its definition.
     *
     * <p>
     * The WC add-charge path never checks {@code chargeAppliesTo}, so this time-type guard is also what stops a
     * term-loan charge product from being attached to a WC account; removing it requires adding that check instead.
     */
    public void validateCreateLoanChargeAgainstLoan(final WorkingCapitalLoan loan, final ChargeTimeType chargeTimeType,
            final BigDecimal amount, final LocalDate dueDate, final LocalDate businessDate) {
        if (chargeTimeType == null || !ChargeTimeType.validWorkingCapitalLoanProduct().contains(chargeTimeType)) {
            final String chargeTimeCode = chargeTimeType == null ? null : chargeTimeType.getCode();
            throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.charge.time.type.not.supported",
                    "Charge time type " + chargeTimeType + " is not supported on a Working Capital Loan.", chargeTimeCode);
        }
        final LoanStatus loanStatus = loan.getLoanStatus();
        if (chargeTimeType.isTimeOfDisbursement()) {
            if (loan.isDisbursed()) {
                throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.disbursement.charge.loan.already.disbursed",
                        "A disbursement charge can only be added before the Working Capital Loan is disbursed.", loan.getId());
            }
            if (!(loanStatus.isSubmittedAndPendingApproval() || loanStatus.isApproved())) {
                throw new PlatformApiDataValidationException("loan.should.be.pending.or.approved",
                        "Loan should be submitted and pending approval or approved", "workingCapitalLoan");
            }
            if (dueDate != null) {
                throw new PlatformApiDataValidationException("dueDate.not.allowed.for.disbursement.charge",
                        "A disbursement charge is due on the disbursement date and cannot carry a due date",
                        WorkingCapitalLoanChargeConstants.dueDateParamName);
            }
            return;
        }
        if (amount == null) {
            throw new PlatformApiDataValidationException("field.is.mandatory", "Field is mandatory",
                    WorkingCapitalLoanChargeConstants.amountParamName);
        }
        if (dueDate == null) {
            throw new PlatformApiDataValidationException("field.is.mandatory", "Field is mandatory",
                    WorkingCapitalLoanChargeConstants.dueDateParamName);
        }
        if (dueDate.isBefore(businessDate)) {
            throw new PlatformApiDataValidationException("dueDate.cannot.be.in.the.past", "DueDate cannot be in the past",
                    WorkingCapitalLoanChargeConstants.dueDateParamName);
        }
        if (!(loanStatus.isActive() || loanStatus.isClosedObligationsMet() || loanStatus.isOverpaid())) {
            throw new PlatformApiDataValidationException("loan.should.be.active", "Loan should be in active status", "workingCapitalLoan");
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
