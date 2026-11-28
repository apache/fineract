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
package org.apache.fineract.portfolio.charge.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;

/**
 * Over which period the income of an amortized loan charge is recognized. {@link #NONE} means the charge is not
 * amortized.
 */
@Getter
@RequiredArgsConstructor
public enum ChargeAmortizationPeriodType implements ApiFacingEnum<ChargeAmortizationPeriodType> {

    NONE("chargeAmortizationPeriodType.none", "None"), //
    FULL_LOAN_TERM("chargeAmortizationPeriodType.fullLoanTerm", "Full loan term"), //
    NUMBER_OF_DAYS("chargeAmortizationPeriodType.numberOfDays", "Number of days"); //

    private final String code;
    private final String humanReadableName;

    public boolean isAmortized() {
        return this != NONE;
    }
}
