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
package org.apache.fineract.portfolio.loanaccount.service.reaging;

import static org.apache.fineract.infrastructure.core.service.DateUtils.getBusinessLocalDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanReAgingApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.ChangeOperation;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanReAgingValidator {

    private final LoanTransactionRepository loanTransactionRepository;
    private final CodeValueRepository codeValueRepository;

    public void validateReAge(Loan loan, JsonCommand command) {
        validateReAgeRequest(loan, command);
        validateReAgeBusinessRules(loan);
        validateReAgeOutstandingBalance(loan, command);
    }

    private void validateReAgeRequest(Loan loan, JsonCommand command) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.reAge");

        String externalId = command.stringValueOfParameterNamedAllowingNull(LoanReAgingApiConstants.externalIdParameterName);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.externalIdParameterName).ignoreIfNull().value(externalId)
                .notExceedingLengthOf(100);

        LocalDate startDate = command.localDateValueOfParameterNamed(LoanReAgingApiConstants.startDate);
        if (loan.isProgressiveSchedule()) {
            baseDataValidator.reset().parameter(LoanReAgingApiConstants.startDate).value(startDate).notNull()
                    .validateDateAfterOrEqual(loan.getDisbursementDate());
        } else {
            baseDataValidator.reset().parameter(LoanReAgingApiConstants.startDate).value(startDate).notNull()
                    .validateDateAfter(loan.getMaturityDate());
        }
        String frequencyType = command.stringValueOfParameterNamedAllowingNull(LoanReAgingApiConstants.frequencyType);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.frequencyType).value(frequencyType).notNull();

        Integer frequencyNumber = command.integerValueOfParameterNamed(LoanReAgingApiConstants.frequencyNumber);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.frequencyNumber).value(frequencyNumber).notNull()
                .integerGreaterThanZero();

        Integer numberOfInstallments = command.integerValueOfParameterNamed(LoanReAgingApiConstants.numberOfInstallments);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.numberOfInstallments).value(numberOfInstallments).notNull()
                .integerGreaterThanZero();

        final LoanReAgeInterestHandlingType reAgeInterestHandlingType = command
                .enumValueOfParameterNamed(LoanReAgingApiConstants.reAgeInterestHandlingParamName, LoanReAgeInterestHandlingType.class);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.reAgeInterestHandlingParamName).value(reAgeInterestHandlingType)
                .ignoreIfNull();

        Long reasonCodeValueId = command.longValueOfParameterNamed(LoanReAgingApiConstants.reasonCodeValueIdParamName);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.reasonCodeValueIdParamName).value(reasonCodeValueId).ignoreIfNull();
        if (reasonCodeValueId != null) {
            final CodeValue reasonCodeValue = codeValueRepository.findByCodeNameAndId(LoanApiConstants.REAGE_REASONS, reasonCodeValueId);
            if (reasonCodeValue == null) {
                dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.reage.reason.invalid",
                        "Reage Reason with ID " + reasonCodeValueId + " does not exist", LoanApiConstants.REAGE_REASONS));
            }
        }

        throwExceptionIfValidationErrorsExist(dataValidationErrors);
    }

    private void validateReAgeBusinessRules(Loan loan) {
        // validate reaging is only available for progressive schedule & advanced payment allocation
        LoanScheduleType loanScheduleType = LoanScheduleType.valueOf(loan.getLoanProductRelatedDetail().getLoanScheduleType().name());
        boolean isProgressiveSchedule = LoanScheduleType.PROGRESSIVE.equals(loanScheduleType);

        String transactionProcessingStrategyCode = loan.getTransactionProcessingStrategyCode();
        boolean isAdvancedPaymentSchedule = AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                .equals(transactionProcessingStrategyCode);

        if (!(isProgressiveSchedule && isAdvancedPaymentSchedule)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.supported.only.for.progressive.loan.schedule.type",
                    "Loan reaging is only available for progressive repayment schedule and Advanced payment allocation strategy",
                    loan.getId());
        }

        // validate reaging is only done on an active loan
        if (!loan.getStatus().isActive()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.supported.only.for.active.loans",
                    "Loan reaging can only be done on active loans", loan.getId());
        }

        // validate if there's already a re-aging transaction for today
        final boolean isReAgingTransactionForTodayPresent = loanTransactionRepository.existsNonReversedByLoanAndTypeAndDate(loan,
                LoanTransactionType.REAGE, getBusinessLocalDate());

        if (isReAgingTransactionForTodayPresent) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.reage.transaction.already.present.for.today",
                    "Loan reaging can only be done once a day. There has already been a reaging done for today", loan.getId());
        }
    }

    public void validateUndoReAge(Loan loan, JsonCommand command) {
        validateUndoReAgeBusinessRules(loan);
    }

    private void validateUndoReAgeBusinessRules(Loan loan) {
        // validate if there's a reaging transaction already
        Optional<LoanTransaction> optionalReAgingTx = loan.getLoanTransactions().stream().filter(tx -> tx.getTypeOf().isReAge())
                .min(Comparator.comparing(LoanTransaction::getTransactionDate));
        if (optionalReAgingTx.isEmpty()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.reaging.transaction.missing",
                    "Undoing a reaging can only be done if there was a reaging already", loan.getId());
        }

        // validate if there's no payment between the reaging and today
        boolean repaymentExistsAfterReAging = loan.getLoanTransactions().stream()
                .anyMatch(tx -> tx.getTypeOf().isRepaymentType() && transactionHappenedAfterOther(tx, optionalReAgingTx.get()));
        if (repaymentExistsAfterReAging) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.repayment.exists.after.reaging",
                    "Undoing a reaging can only be done if there hasn't been any repayment afterwards", loan.getId());
        }
    }

    private void throwExceptionIfValidationErrorsExist(List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private boolean transactionHappenedAfterOther(LoanTransaction transaction, LoanTransaction otherTransaction) {
        return new ChangeOperation(transaction).compareTo(new ChangeOperation(otherTransaction)) > 0;
    }

    private void validateReAgeOutstandingBalance(final Loan loan, final JsonCommand command) {
        final LocalDate businessDate = getBusinessLocalDate();
        final LocalDate startDate = command.dateValueOfParameterNamed(LoanReAgingApiConstants.startDate);

        final boolean isBackdated = businessDate.isAfter(startDate);
        if (isBackdated) {
            return;
        }

        if (loan.getSummary().getTotalPrincipalOutstanding().compareTo(java.math.BigDecimal.ZERO) == 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.no.outstanding.balance.to.reage",
                    "Loan cannot be re-aged as there are no outstanding balances to be re-aged", loan.getId());
        }
    }

}
