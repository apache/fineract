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
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.springframework.util.StringUtils;

/**
 * Delinquency start types for Working Capital Loan Product.
 */
@Getter
@RequiredArgsConstructor
public enum WorkingCapitalLoanDelinquencyStartType implements ApiFacingEnum<WorkingCapitalLoanDelinquencyStartType> {

    LOAN_CREATION(1, "LOAN_CREATION", "Loan Creation"), //
    DISBURSEMENT(2, "DISBURSEMENT", "Disbursement") //
    ;

    private final Integer value;
    private final String code;
    private final String humanReadableName;

    /**
     * Resolve enum from its string name/code (e.g. "LOAN_CREATION", "DISBURSEMENT").
     */
    public static WorkingCapitalLoanDelinquencyStartType fromString(final String delinquencyStartTypeValue) {
        if (!StringUtils.hasText(delinquencyStartTypeValue)) {
            return null;
        }

        if (delinquencyStartTypeValue.trim().equalsIgnoreCase(LOAN_CREATION.name())) {
            return LOAN_CREATION;
        }
        if (delinquencyStartTypeValue.trim().equalsIgnoreCase(DISBURSEMENT.name())) {
            return DISBURSEMENT;
        }

        return null;
    }

    public StringEnumOptionData toStringEnumOptionData() {
        return new StringEnumOptionData(getValue().toString(), getCode(), getHumanReadableName());
    }
}
