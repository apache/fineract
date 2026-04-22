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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinMaxConstraints;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductMinMaxConstraintsUpdateUtil {

    public Map<String, Object> update(final LoanProductMinMaxConstraints loanProductMinMaxConstraints, final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);

        final String localeAsInput = command.locale();

        final String minPrincipalParamName = "minPrincipal";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(minPrincipalParamName,
                loanProductMinMaxConstraints.getMinPrincipal())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minPrincipalParamName);
            actualChanges.put(minPrincipalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductMinMaxConstraints.setMinPrincipal(newValue);
        }

        final String maxPrincipalParamName = "maxPrincipal";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(maxPrincipalParamName,
                loanProductMinMaxConstraints.getMaxPrincipal())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxPrincipalParamName);
            actualChanges.put(maxPrincipalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductMinMaxConstraints.setMaxPrincipal(newValue);
        }

        final String minNumberOfRepaymentsParamName = "minNumberOfRepayments";
        if (command.isChangeInIntegerParameterNamed(minNumberOfRepaymentsParamName,
                loanProductMinMaxConstraints.getMinNumberOfRepayments())) {
            final Integer newValue = command.integerValueOfParameterNamed(minNumberOfRepaymentsParamName);
            actualChanges.put(minNumberOfRepaymentsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductMinMaxConstraints.setMinNumberOfRepayments(newValue);
        }

        final String maxNumberOfRepaymentsParamName = "maxNumberOfRepayments";
        if (command.isChangeInIntegerParameterNamed(maxNumberOfRepaymentsParamName,
                loanProductMinMaxConstraints.getMaxNumberOfRepayments())) {
            final Integer newValue = command.integerValueOfParameterNamed(maxNumberOfRepaymentsParamName);
            actualChanges.put(maxNumberOfRepaymentsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductMinMaxConstraints.setMaxNumberOfRepayments(newValue);
        }

        final String minInterestRatePerPeriodParamName = "minInterestRatePerPeriod";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(minInterestRatePerPeriodParamName,
                loanProductMinMaxConstraints.getMinNominalInterestRatePerPeriod())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minInterestRatePerPeriodParamName);
            actualChanges.put(minInterestRatePerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductMinMaxConstraints.setMinNominalInterestRatePerPeriod(newValue);
        }

        final String maxInterestRatePerPeriodParamName = "maxInterestRatePerPeriod";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(maxInterestRatePerPeriodParamName,
                loanProductMinMaxConstraints.getMaxNominalInterestRatePerPeriod())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxInterestRatePerPeriodParamName);
            actualChanges.put(maxInterestRatePerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductMinMaxConstraints.setMaxNominalInterestRatePerPeriod(newValue);
        }

        return actualChanges;
    }
}
