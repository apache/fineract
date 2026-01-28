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
package org.apache.fineract.portfolio.loanaccount.service.reamortization;

import static org.apache.fineract.infrastructure.core.service.DateUtils.getBusinessLocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanReAmortizationApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.request.ReAmortizationPreviewRequest;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.reamortization.LoanReAmortizationInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.domain.reamortization.LoanReAmortizationParameter;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanReAmortizationValidator {

    private final CodeValueRepository codeValueRepository;

    public void validateReAmortize(Loan loan, JsonCommand command) {
        validateReAmortizeRequest(command);
        LoanReAmortizationInterestHandlingType interestHandlingType = command.enumValueOfParameterNamed(
                LoanReAmortizationApiConstants.reAmortizationInterestHandlingParamName, LoanReAmortizationInterestHandlingType.class);
        validateReAmortizeBusinessRules(loan, interestHandlingType);
    }

    private void validateReAmortizeRequest(JsonCommand command) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.reAmortization");

        String externalId = command.stringValueOfParameterNamedAllowingNull(LoanReAmortizationApiConstants.externalIdParameterName);
        baseDataValidator.reset().parameter(LoanReAmortizationApiConstants.externalIdParameterName).ignoreIfNull().value(externalId)
                .notExceedingLengthOf(100);

        final LoanReAmortizationInterestHandlingType reAmortizationInterestHandlingType = command.enumValueOfParameterNamed(
                LoanReAmortizationApiConstants.reAmortizationInterestHandlingParamName, LoanReAmortizationInterestHandlingType.class);
        baseDataValidator.reset().parameter(LoanReAmortizationApiConstants.reAmortizationInterestHandlingParamName)
                .value(reAmortizationInterestHandlingType).ignoreIfNull();

        Long reasonCodeValueId = command.longValueOfParameterNamed(LoanReAmortizationApiConstants.reasonCodeValueIdParamName);
        baseDataValidator.reset().parameter(LoanReAmortizationApiConstants.reasonCodeValueIdParamName).value(reasonCodeValueId)
                .ignoreIfNull();
        if (reasonCodeValueId != null) {
            final CodeValue reasonCodeValue = codeValueRepository.findByCodeNameAndId(LoanApiConstants.REAMORTIZATION_REASONS,
                    reasonCodeValueId);
            if (reasonCodeValue == null) {
                dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.reamortization.reason.invalid",
                        "Reamortization Reason with ID " + reasonCodeValueId + " does not exist", LoanApiConstants.REAMORTIZATION_REASONS));
            }
        }

        throwExceptionIfValidationErrorsExist(dataValidationErrors);
    }

    private void validateReAmortizeBusinessRules(Loan loan, LoanReAmortizationInterestHandlingType interestHandlingType) {
        // validate reamortization shouldn't happen after maturity
        if (DateUtils.isAfter(getBusinessLocalDate(), loan.getMaturityDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.cannot.be.submitted.after.maturity",
                    "Loan cannot be re-amortized after maturity", loan.getId());
        }

        // validate reamortization is only available for progressive schedule & advanced payment allocation
        LoanScheduleType loanScheduleType = LoanScheduleType.valueOf(loan.getLoanProductRelatedDetail().getLoanScheduleType().name());
        boolean isProgressiveSchedule = LoanScheduleType.PROGRESSIVE.equals(loanScheduleType);

        String transactionProcessingStrategyCode = loan.getTransactionProcessingStrategyCode();
        boolean isAdvancedPaymentSchedule = AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                .equals(transactionProcessingStrategyCode);

        if (!(isProgressiveSchedule && isAdvancedPaymentSchedule)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.supported.only.for.progressive.loan.schedule.type",
                    "Loan reamortization is only available for progressive repayment schedule and Advanced payment allocation strategy",
                    loan.getId());
        }

        // validate re-amortization is only done on an active loan
        if (!loan.getStatus().isActive()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.supported.only.for.active.loans",
                    "Loan re-amortization can only be done on active loans", loan.getId());
        }

        // validate if there's already a re-amortization transaction for today
        boolean isReAmortizationTransactionForTodayPresent = loan.getLoanTransactions().stream()
                .anyMatch(tx -> tx.getTypeOf().isReAmortize() && tx.getTransactionDate().equals(getBusinessLocalDate()));
        if (isReAmortizationTransactionForTodayPresent) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.reamortize.transaction.already.present.for.today",
                    "Loan reamortization can only be done once a day. There has already been a reamortization done for today",
                    loan.getId());
        }

        // validate if there is active re-amortization transaction, it should have the same interest handling strategy
        Optional<LoanTransaction> previousReAmortizationTransaction = loan.getLoanTransactions().stream()
                .filter(LoanTransaction::isNotReversed).filter(LoanTransaction::isReAmortize).findAny();
        if (previousReAmortizationTransaction.isPresent()) {
            LoanReAmortizationInterestHandlingType previousInterestHandlingType = Optional
                    .ofNullable(previousReAmortizationTransaction.get().getLoanReAmortizationParameter())
                    .map(LoanReAmortizationParameter::getInterestHandlingType).orElse(LoanReAmortizationInterestHandlingType.DEFAULT);
            LoanReAmortizationInterestHandlingType currentInterestHandlingType = Optional.ofNullable(interestHandlingType)
                    .orElse(LoanReAmortizationInterestHandlingType.DEFAULT);
            if (!previousInterestHandlingType.equals(currentInterestHandlingType)) {
                throw new GeneralPlatformDomainRuleException(
                        "error.msg.loan.reamortize.reamortize.transaction.interest.handling.strategy.missmatch",
                        "Previous active reamortization transactiuon has different interest handling strategy.", loan.getId());
            }
        }

        // validate loan is not charged-off
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.not.allowed.on.charged.off",
                    "Loan re-amortization is not allowed on charged-off loan.", loan.getId());
        }

        // validate loan is not contract terminated
        if (loan.isContractTermination()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.not.allowed.on.contract.terminated",
                    "Loan re-amortization is not allowed on contract terminated loan.", loan.getId());
        }

        // validate there are overdue installments to re-amortize
        boolean hasOverdueInstallments = loan.getRepaymentScheduleInstallments().stream()
                .anyMatch(installment -> installment.getDueDate().isBefore(getBusinessLocalDate())
                        && installment.getPrincipalOutstanding(loan.getCurrency()).isGreaterThanZero());
        if (!hasOverdueInstallments) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.no.overdue.amount",
                    "Re-amortization cannot be executed: no overdue amount exists.", loan.getId());
        }
    }

    public LoanTransaction findAndValidateReAmortizeTransactionForUndo(Loan loan) {
        // validate if there's a non-reversed reamortization transaction already
        final Optional<LoanTransaction> optionalReAmortizationTx = loan.getLoanTransactions().stream() //
                .filter(LoanTransaction::isNotReversed) //
                .filter(tx -> tx.getTypeOf().isReAmortize()) //
                .findAny();
        if (optionalReAmortizationTx.isEmpty()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.reamortization.transaction.missing",
                    "Undoing a reamortization can only be done if there was a non-reversed reamortization already", loan.getId());
        }

        return optionalReAmortizationTx.get();
    }

    private void throwExceptionIfValidationErrorsExist(List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateReAmortize(Loan loan, ReAmortizationPreviewRequest reAmortizationPreviewRequest) {
        LoanReAmortizationInterestHandlingType interestHandlingType = LoanReAmortizationInterestHandlingType
                .valueOf(reAmortizationPreviewRequest.getReAmortizationInterestHandling());
        validateReAmortizeBusinessRules(loan, interestHandlingType);
    }
}
