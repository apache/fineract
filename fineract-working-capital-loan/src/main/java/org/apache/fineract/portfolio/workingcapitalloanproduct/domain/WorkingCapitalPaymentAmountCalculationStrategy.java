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
package org.apache.fineract.portfolio.workingcapitalloanproduct.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.springframework.util.StringUtils;

/**
 * Strategy for calculating the working-capital loan daily payment amount.
 */
@Getter
@RequiredArgsConstructor
public enum WorkingCapitalPaymentAmountCalculationStrategy implements ApiFacingEnum<WorkingCapitalPaymentAmountCalculationStrategy> {

    TPV(1, "TPV", "Total Payment Volume"), //
    ANNUAL_EIR(2, "ANNUAL_EIR", "Annual EIR") //
    ;

    private final Integer value;
    private final String code;
    private final String humanReadableName;

    public static WorkingCapitalPaymentAmountCalculationStrategy fromString(final String paymentAmountCalcStrategy) {
        if (!StringUtils.hasText(paymentAmountCalcStrategy)) {
            return null;
        }
        final String trimmed = paymentAmountCalcStrategy.trim();
        if (trimmed.equalsIgnoreCase(TPV.name())) {
            return TPV;
        }
        if (trimmed.equalsIgnoreCase(ANNUAL_EIR.name())) {
            return ANNUAL_EIR;
        }
        return null;
    }

    public boolean isTpv() {
        return this == TPV;
    }

    public boolean isAnnualEir() {
        return this == ANNUAL_EIR;
    }
}
