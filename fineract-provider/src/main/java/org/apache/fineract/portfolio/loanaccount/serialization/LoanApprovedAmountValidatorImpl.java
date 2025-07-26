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
package org.apache.fineract.portfolio.loanaccount.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.common.service.Validator;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class LoanApprovedAmountValidatorImpl implements LoanApprovedAmountValidator {

    private static final Set<LoanStatus> INVALID_LOAN_STATUSES_FOR_APPROVED_AMOUNT_MODIFICATION = Set.of(LoanStatus.INVALID,
            LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.REJECTED);

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepository loanRepository;
    private final LoanApplicationValidator loanApplicationValidator;

    @Override
    public void validateLoanApprovedAmountModification(JsonCommand command) {
        String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> supportedParameters = new HashSet<>(
                Arrays.asList(LoanApiConstants.amountParameterName, LoanApiConstants.localeParameterName));

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final BigDecimal newApprovedAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.amountParameterName,
                element);

        Validator.validateOrThrow("loan.approved.amount", baseDataValidator -> {
            baseDataValidator.reset().parameter(LoanApiConstants.amountParameterName).value(newApprovedAmount).notNull();
        });

        Validator.validateOrThrowDomainViolation("loan.approved.amount", baseDataValidator -> {
            baseDataValidator.reset().parameter(LoanApiConstants.amountParameterName).value(newApprovedAmount).positiveAmount();

            final Long loanId = command.getLoanId();
            Loan loan = this.loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));

            if (INVALID_LOAN_STATUSES_FOR_APPROVED_AMOUNT_MODIFICATION.contains(loan.getStatus())) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("loan.status.not.valid.for.approved.amount.modification");
            }

            BigDecimal maximumThresholdForApprovedAmount;
            if (loan.loanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
                maximumThresholdForApprovedAmount = loanApplicationValidator.getOverAppliedMax(loan);
            } else {
                maximumThresholdForApprovedAmount = loan.getProposedPrincipal();
            }

            if (MathUtil.isGreaterThan(newApprovedAmount, maximumThresholdForApprovedAmount)) {
                baseDataValidator.reset().parameter(LoanApiConstants.amountParameterName)
                        .failWithCode("can't.be.greater.than.maximum.applied.loan.amount.calculation");
            }

            BigDecimal totalPrincipalOnLoan = loan.getSummary().getTotalPrincipal();
            if (MathUtil.isLessThan(newApprovedAmount, totalPrincipalOnLoan)) {
                baseDataValidator.reset().parameter(LoanApiConstants.amountParameterName)
                        .failWithCode("less.than.disbursed.principal.and.capitalized.income");
            }
        });
    }
}
