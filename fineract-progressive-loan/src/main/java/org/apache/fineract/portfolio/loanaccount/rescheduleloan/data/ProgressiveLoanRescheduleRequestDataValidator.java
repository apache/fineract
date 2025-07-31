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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.data;

import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.APPROVE_REQUEST_DATA_PARAMETERS;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.CREATE_REQUEST_DATA_PARAMETERS;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateAndRetrieveAdjustedDate;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateAndRetrieveRescheduleFromDate;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateApprovalDate;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateForOverdueCharges;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateLoanIsActive;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateLoanStatusIsActiveOrClosed;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateRescheduleReasonComment;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateRescheduleReasonId;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateRescheduleRequestStatus;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateSubmittedOnDate;
import static org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidatorImpl.validateSupportedParameters;

import com.google.gson.JsonElement;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRescheduleRequestToTermVariationMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.springframework.stereotype.Component;

@Component("progressiveLoanRescheduleRequestDataValidatorImpl")
@AllArgsConstructor
public class ProgressiveLoanRescheduleRequestDataValidator implements LoanRescheduleRequestDataValidator {

    private final FromJsonHelper fromJsonHelper;
    private final LoanRescheduleRequestRepository loanRescheduleRequestRepository;

    @Override
    public void validateForCreateAction(JsonCommand jsonCommand, Loan loan) {
        validateSupportedParameters(jsonCommand, CREATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors)
                .resource(StringUtils.lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));

        final JsonElement jsonElement = jsonCommand.parsedJson();

        validateSubmittedOnDate(fromJsonHelper, loan, jsonElement, dataValidatorBuilder);
        final LocalDate rescheduleFromDate = validateAndRetrieveRescheduleFromDate(fromJsonHelper, jsonElement, dataValidatorBuilder);
        validateRescheduleReasonId(fromJsonHelper, jsonElement, dataValidatorBuilder);
        validateRescheduleReasonComment(fromJsonHelper, jsonElement, dataValidatorBuilder);
        LocalDate adjustedDueDate = validateAndRetrieveAdjustedDate(fromJsonHelper, jsonElement, rescheduleFromDate, dataValidatorBuilder);
        BigDecimal interestRate = validateInterestRate(fromJsonHelper, jsonElement, dataValidatorBuilder, loan);
        validateUnsupportedParams(jsonElement, dataValidatorBuilder);

        boolean hasInterestRateChange = interestRate != null;
        boolean hasAdjustDueDateChange = adjustedDueDate != null;
        if (hasInterestRateChange) {
            validateLoanStatusIsActiveOrClosed(loan, dataValidatorBuilder);
        } else {
            validateLoanIsActive(loan, dataValidatorBuilder);
        }

        if (hasInterestRateChange && hasAdjustDueDateChange) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.adjustedDueDateParamName).failWithCode(
                    RescheduleLoansApiConstants.rescheduleMultipleOperationsNotSupportedErrorCode,
                    "Only one operation is supported at a time during Loan Rescheduling");
        }

        if (rescheduleFromDate != null && hasInterestRateChange) {
            validateInterestRateChangeRescheduleFromDate(loan, rescheduleFromDate);
        }

        LoanRepaymentScheduleInstallment installment;
        if (hasInterestRateChange) {
            installment = loan.getRelatedRepaymentScheduleInstallment(rescheduleFromDate);
        } else {
            installment = loan.fetchLoanRepaymentScheduleInstallmentByDueDate(rescheduleFromDate);
        }

        validateReschedulingInstallment(dataValidatorBuilder, installment);
        validateForOverdueCharges(dataValidatorBuilder, loan, installment);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    @Override
    public void validateReschedulingInstallment(DataValidatorBuilder dataValidatorBuilder, LoanRepaymentScheduleInstallment installment) {
        if (installment == null) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)
                    .failWithCode("repayment.schedule.installment.does.not.exist", "Repayment schedule installment does not exist");
        }
    }

    private void validateUnsupportedParams(JsonElement jsonElement, DataValidatorBuilder dataValidatorBuilder) {
        final var unsupportedFields = List.of(RescheduleLoansApiConstants.graceOnPrincipalParamName, //
                RescheduleLoansApiConstants.graceOnInterestParamName, //
                RescheduleLoansApiConstants.extraTermsParamName, //
                RescheduleLoansApiConstants.emiParamName//
        );

        for (var unsupportedField : unsupportedFields) {
            if (this.fromJsonHelper.parameterHasValue(unsupportedField, jsonElement)) {
                dataValidatorBuilder.reset().parameter(unsupportedField).failWithCode(
                        RescheduleLoansApiConstants.rescheduleSelectedOperationNotSupportedErrorCode,
                        "Selected operation is not supported by Progressive Loan at a time during Loan Rescheduling");
            }
        }
    }

    @Override
    public void validateForApproveAction(JsonCommand jsonCommand, LoanRescheduleRequest loanRescheduleRequest) {
        validateSupportedParameters(jsonCommand, APPROVE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors)
                .resource(StringUtils.lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));
        final JsonElement jsonElement = jsonCommand.parsedJson();
        validateApprovalDate(fromJsonHelper, loanRescheduleRequest, jsonElement, dataValidatorBuilder);
        validateRescheduleRequestStatus(loanRescheduleRequest, dataValidatorBuilder);
        LocalDate rescheduleFromDate = loanRescheduleRequest.getRescheduleFromDate();
        final Loan loan = loanRescheduleRequest.getLoan();
        LoanRepaymentScheduleInstallment installment;

        boolean hasInterestRateChange = false;
        for (LoanRescheduleRequestToTermVariationMapping mapping : loanRescheduleRequest
                .getLoanRescheduleRequestToTermVariationMappings()) {
            LoanTermVariationType termType = mapping.getLoanTermVariations().getTermType();
            if (termType.isInterestRateVariation() || termType.isInterestRateFromInstallment()) {
                hasInterestRateChange = true;
            }
        }
        if (hasInterestRateChange) {
            validateLoanStatusIsActiveOrClosed(loan, dataValidatorBuilder);
        } else {
            validateLoanIsActive(loan, dataValidatorBuilder);
        }

        if (loanRescheduleRequest.getInterestRateFromInstallmentTermVariationIfExists() != null) {
            installment = loan.getRelatedRepaymentScheduleInstallment(rescheduleFromDate);
        } else {
            installment = loan.fetchLoanRepaymentScheduleInstallmentByDueDate(rescheduleFromDate);
        }
        validateReschedulingInstallment(dataValidatorBuilder, installment);
        validateForOverdueCharges(dataValidatorBuilder, loan, installment);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    @Override
    public void validateForRejectAction(JsonCommand jsonCommand, LoanRescheduleRequest loanRescheduleRequest) {
        throw new UnsupportedOperationException("Nothing to override here");
    }

    private void validateInterestRateChangeRescheduleFromDate(Loan loan, LocalDate rescheduleFromDate) {
        boolean alreadyExistInterestRateChange = loanRescheduleRequestRepository.exists((root, query, criteriaBuilder) -> {
            Predicate loanPredicate = criteriaBuilder.equal(root.get("loan"), loan);
            Predicate statusPredicate = root.get("statusEnum")
                    .in(List.of(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(), LoanStatus.APPROVED.getValue()));
            Predicate datePredicate = criteriaBuilder.equal(root.get("rescheduleFromDate"), rescheduleFromDate);
            return criteriaBuilder.and(loanPredicate, statusPredicate, datePredicate);
        });
        if (alreadyExistInterestRateChange) {
            throw new GeneralPlatformDomainRuleException("loan.reschedule.interest.rate.change.already.exists",
                    "Interest rate change for the provided date is already exists.", rescheduleFromDate);
        }
    }

    private BigDecimal validateInterestRate(final FromJsonHelper fromJsonHelper, final JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder, Loan loan) {
        final BigDecimal interestRate = fromJsonHelper
                .extractBigDecimalWithLocaleNamed(RescheduleLoansApiConstants.newInterestRateParamName, jsonElement);
        DataValidatorBuilder interestRateDataValidatorBuilder = dataValidatorBuilder.reset()
                .parameter(RescheduleLoansApiConstants.newInterestRateParamName).value(interestRate).ignoreIfNull().zeroOrPositiveAmount();

        BigDecimal minNominalInterestRatePerPeriod = loan.getLoanProduct().getMinNominalInterestRatePerPeriod();
        if (minNominalInterestRatePerPeriod != null) {
            interestRateDataValidatorBuilder.notLessThanMin(minNominalInterestRatePerPeriod);
        }

        BigDecimal maxNominalInterestRatePerPeriod = loan.getLoanProduct().getMaxNominalInterestRatePerPeriod();
        if (maxNominalInterestRatePerPeriod != null) {
            interestRateDataValidatorBuilder.notGreaterThanMax(maxNominalInterestRatePerPeriod);
        }
        return interestRate;
    }
}
