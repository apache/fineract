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
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductFloatingRates;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductFloatingRatesUpdateUtil {

    public Map<? extends String, ?> update(LoanProductFloatingRates loanProductFloatingRates, JsonCommand command,
            FloatingRate floatingRate) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);
        if (floatingRate != null) {
            final String floatingRatesId = "floatingRatesId";
            if (loanProductFloatingRates.getFloatingRate() == null
                    || command.isChangeInLongParameterNamed(floatingRatesId, loanProductFloatingRates.getFloatingRate().getId())) {
                final long newValue = command.longValueOfParameterNamed(floatingRatesId);
                actualChanges.put(floatingRatesId, newValue);
                loanProductFloatingRates.setFloatingRate(floatingRate);
            }
        }

        final String interestRateDifferential = "interestRateDifferential";
        if (command.isChangeInBigDecimalParameterNamed(interestRateDifferential, loanProductFloatingRates.getInterestRateDifferential())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestRateDifferential);
            actualChanges.put(interestRateDifferential, newValue);
            loanProductFloatingRates.setInterestRateDifferential(newValue);
        }
        final String minDifferentialLendingRate = "minDifferentialLendingRate";
        if (command.isChangeInBigDecimalParameterNamed(minDifferentialLendingRate,
                loanProductFloatingRates.getMinDifferentialLendingRate())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minDifferentialLendingRate);
            actualChanges.put(minDifferentialLendingRate, newValue);
            loanProductFloatingRates.setMinDifferentialLendingRate(newValue);
        }
        final String defaultDifferentialLendingRate = "defaultDifferentialLendingRate";
        if (command.isChangeInBigDecimalParameterNamed(defaultDifferentialLendingRate,
                loanProductFloatingRates.getDefaultDifferentialLendingRate())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(defaultDifferentialLendingRate);
            actualChanges.put(defaultDifferentialLendingRate, newValue);
            loanProductFloatingRates.setDefaultDifferentialLendingRate(newValue);
        }
        final String maxDifferentialLendingRate = "maxDifferentialLendingRate";
        if (command.isChangeInBigDecimalParameterNamed(maxDifferentialLendingRate,
                loanProductFloatingRates.getMaxDifferentialLendingRate())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxDifferentialLendingRate);
            actualChanges.put(maxDifferentialLendingRate, newValue);
            loanProductFloatingRates.setMaxDifferentialLendingRate(newValue);
        }
        final String isFloatingInterestRateCalculationAllowed = "isFloatingInterestRateCalculationAllowed";
        if (command.isChangeInBooleanParameterNamed(isFloatingInterestRateCalculationAllowed,
                loanProductFloatingRates.isFloatingInterestRateCalculationAllowed())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isFloatingInterestRateCalculationAllowed);
            actualChanges.put(isFloatingInterestRateCalculationAllowed, newValue);
            loanProductFloatingRates.setFloatingInterestRateCalculationAllowed(newValue);
        }

        return actualChanges;
    }
}
