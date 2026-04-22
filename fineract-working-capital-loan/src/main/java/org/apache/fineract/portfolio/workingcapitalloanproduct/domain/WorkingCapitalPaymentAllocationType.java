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

/**
 * Payment allocation types for Working Capital Loan Product. Only PRINCIPAL, FEE, and PENALTY (no INTEREST).
 */
@Getter
@RequiredArgsConstructor
public enum WorkingCapitalPaymentAllocationType implements ApiFacingEnum<WorkingCapitalPaymentAllocationType> {

    PENALTY("PENALTY", "Penalty"), //
    FEE("FEE", "Fee"), //
    PRINCIPAL("PRINCIPAL", "Principal"); //

    private final String code;
    private final String humanReadableName;

}
