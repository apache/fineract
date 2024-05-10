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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.api.LoanReAmortizationApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.ChargeOrTransaction;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.springframework.stereotype.Component;

@Component
public class LoanReAmortizationValidator {

    public void validateReAmortize(Loan loan, JsonCommand command) {
        validateReAmortizeRequest(command);
        validateReAmortizeBusinessRules(loan);
    }

    private void validateReAmortizeRequest(JsonCommand command) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.reAmortization");

        String externalId = command.stringValueOfParameterNamedAllowingNull(LoanReAmortizationApiConstants.externalIdParameterName);
        baseDataValidator.reset().parameter(LoanReAmortizationApiConstants.externalIdParameterName).ignoreIfNull().value(externalId)
                .notExceedingLengthOf(100);

        throwExceptionIfValidationErrorsExist(dataValidationErrors);
    }

    private void validateReAmortizeBusinessRules(Loan loan) {
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

        // validate reamortization is only available for non-interest bearing loans
        if (loan.isInterestBearing()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.supported.only.for.non.interest.loans",
                    "Loan reamortization is only available for non-interest bearing loans", loan.getId());
        }

        // validate reamortization is only done on an active loan
        if (!loan.getStatus().isActive()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.supported.only.for.active.loans",
                    "Loan reamortization can only be done on active loans", loan.getId());
        }

        // validate if there's already a re-amortization transaction for today
        boolean isReAmortizationTransactionForTodayPresent = loan.getLoanTransactions().stream()
                .anyMatch(tx -> tx.getTypeOf().isReAmortize() && tx.getTransactionDate().equals(getBusinessLocalDate()));
        if (isReAmortizationTransactionForTodayPresent) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.reamortize.transaction.already.present.for.today",
                    "Loan reamortization can only be done once a day. There has already been a reamortization done for today",
                    loan.getId());
        }
    }

    public void validateUndoReAmortize(Loan loan, JsonCommand command) {
        validateUndoReAmortizeBusinessRules(loan);
    }

    private void validateUndoReAmortizeBusinessRules(Loan loan) {
        // validate if there's a reamortization transaction already
        Optional<LoanTransaction> optionalReAmortizationTx = loan.getLoanTransactions().stream().filter(tx -> tx.getTypeOf().isReAmortize())
                .min(Comparator.comparing(LoanTransaction::getTransactionDate));
        if (optionalReAmortizationTx.isEmpty()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.reamortization.transaction.missing",
                    "Undoing a reamortization can only be done if there was a reamortization already", loan.getId());
        }

        // validate if there's no payment between the reamortization and today
        boolean repaymentExistsAfterReAmortization = loan.getLoanTransactions().stream().anyMatch(tx -> tx.getTypeOf().isRepaymentType()
                && !tx.isReversed() && transactionHappenedAfterOther(tx, optionalReAmortizationTx.get()));
        if (repaymentExistsAfterReAmortization) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.reamortize.repayment.exists.after.reamortization",
                    "Undoing a reamortization can only be done if there hasn't been any repayment afterwards", loan.getId());
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
