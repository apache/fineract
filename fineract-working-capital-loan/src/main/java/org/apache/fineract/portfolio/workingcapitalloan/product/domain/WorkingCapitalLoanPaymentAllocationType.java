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
package org.apache.fineract.portfolio.workingcapitalloan.product.domain;

import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.FEE;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PENALTY;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PRINCIPAL;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.DUE;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.IN_ADVANCE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.DueType;

/**
 * Payment allocation types for Working Capital Loan Product. Only PRINCIPAL, FEE, and PENALTY (no INTEREST).
 */
@Getter
@RequiredArgsConstructor
public enum WorkingCapitalLoanPaymentAllocationType implements ApiFacingEnum<WorkingCapitalLoanPaymentAllocationType> {

    DUE_PENALTY(DUE, PENALTY, "DUE_PENALTY", "Due Penalty"), //
    DUE_FEE(DUE, FEE, "DUE_FEE", "Due Fee"), //
    DUE_PRINCIPAL(DUE, PRINCIPAL, "DUE_PRINCIPAL", "Due Principal"), //
    IN_ADVANCE_PENALTY(IN_ADVANCE, PENALTY, "IN_ADVANCE_PENALTY", "In Advance Penalty"), //
    IN_ADVANCE_FEE(IN_ADVANCE, FEE, "IN_ADVANCE_FEE", "In Advance Fee"), //
    IN_ADVANCE_PRINCIPAL(IN_ADVANCE, PRINCIPAL, "IN_ADVANCE_PRINCIPAL", "In Advance Principal"); //

    private final DueType dueType;
    private final AllocationType allocationType;
    private final String code;
    private final String humanReadableName;

}
