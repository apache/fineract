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
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.api.LoanReAgingApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.ChargeOrTransaction;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.springframework.stereotype.Component;

@Component
public class LoanReAgingValidator {

    public void validateReAge(Loan loan, JsonCommand command) {
        validateReAgeRequest(loan, command);
        validateReAgeBusinessRules(loan);
    }

    private void validateReAgeRequest(Loan loan, JsonCommand command) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.reAge");

        String externalId = command.stringValueOfParameterNamedAllowingNull(LoanReAgingApiConstants.externalIdParameterName);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.externalIdParameterName).ignoreIfNull().value(externalId)
                .notExceedingLengthOf(100);

        LocalDate startDate = command.localDateValueOfParameterNamed(LoanReAgingApiConstants.startDate);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.startDate).value(startDate).notNull()
                .validateDateAfter(loan.getMaturityDate());

        String frequencyType = command.stringValueOfParameterNamedAllowingNull(LoanReAgingApiConstants.frequencyType);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.frequencyType).value(frequencyType).notNull();

        Integer frequencyNumber = command.integerValueOfParameterNamed(LoanReAgingApiConstants.frequencyNumber);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.frequencyNumber).value(frequencyNumber).notNull()
                .integerGreaterThanZero();

        Integer numberOfInstallments = command.integerValueOfParameterNamed(LoanReAgingApiConstants.numberOfInstallments);
        baseDataValidator.reset().parameter(LoanReAgingApiConstants.numberOfInstallments).value(numberOfInstallments).notNull()
                .integerGreaterThanZero();

        throwExceptionIfValidationErrorsExist(dataValidationErrors);
    }

    private void validateReAgeBusinessRules(Loan loan) {
        // validate reaging shouldn't happen before maturity
        if (DateUtils.isBefore(getBusinessLocalDate(), loan.getMaturityDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.cannot.be.submitted.before.maturity",
                    "Loan cannot be re-aged before maturity", loan.getId());
        }

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

        // validate reaging is only available for non-interest bearing loans
        if (loan.isInterestBearing()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.supported.only.for.non.interest.loans",
                    "Loan reaging is only available for non-interest bearing loans", loan.getId());
        }

        // validate reaging is only done on an active loan
        if (!loan.getStatus().isActive()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reage.supported.only.for.active.loans",
                    "Loan reaging can only be done on active loans", loan.getId());
        }

        // validate if there's already a re-aging transaction for today
        boolean isReAgingTransactionForTodayPresent = loan.getLoanTransactions().stream()
                .anyMatch(tx -> tx.getTypeOf().isReAge() && tx.getTransactionDate().equals(getBusinessLocalDate()));
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
        return new ChargeOrTransaction(transaction).compareTo(new ChargeOrTransaction(otherTransaction)) > 0;
    }
}
