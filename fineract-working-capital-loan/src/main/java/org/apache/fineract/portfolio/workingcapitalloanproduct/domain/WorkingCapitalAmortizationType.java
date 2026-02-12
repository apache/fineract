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
 * Amortization types for Working Capital Loan Product.
 */
@Getter
@RequiredArgsConstructor
public enum WorkingCapitalAmortizationType implements ApiFacingEnum<WorkingCapitalAmortizationType> {

    EIR(1, "EIR", "Effective Interest Rate"), //
    FLAT(2, "FLAT", "Flat") //
    ;

    private final Integer value;
    private final String code;
    private final String humanReadableName;

    /**
     * Resolve enum from its string name (e.g. "EIR", "FLAT").
     */
    public static WorkingCapitalAmortizationType fromString(final String amortizationTypeValue) {
        if (!StringUtils.hasText(amortizationTypeValue)) {
            return null;
        }

        if (amortizationTypeValue.trim().equalsIgnoreCase(EIR.name())) {
            return EIR;
        }
        if (amortizationTypeValue.trim().equalsIgnoreCase(FLAT.name())) {
            return FLAT;
        }

        return null;
    }

    public boolean isFLAT() {
        return this.equals(WorkingCapitalAmortizationType.FLAT);
    }

}
