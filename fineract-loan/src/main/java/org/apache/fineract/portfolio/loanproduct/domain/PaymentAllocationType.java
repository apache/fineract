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
package org.apache.fineract.portfolio.loanproduct.domain;

import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.FEE;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.INTEREST;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PENALTY;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PRINCIPAL;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.DUE;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.IN_ADVANCE;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.PAST_DUE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentAllocationType {

    PAST_DUE_PENALTY(PAST_DUE, PENALTY), //
    PAST_DUE_FEE(PAST_DUE, FEE), //
    PAST_DUE_PRINCIPAL(PAST_DUE, PRINCIPAL), //
    PAST_DUE_INTEREST(PAST_DUE, INTEREST), //
    DUE_PENALTY(DUE, PENALTY), //
    DUE_FEE(DUE, FEE), //
    DUE_PRINCIPAL(DUE, PRINCIPAL), //
    DUE_INTEREST(DUE, INTEREST), //
    IN_ADVANCE_PENALTY(IN_ADVANCE, PENALTY), //
    IN_ADVANCE_FEE(IN_ADVANCE, FEE), //
    IN_ADVANCE_PRINCIPAL(IN_ADVANCE, PRINCIPAL), //
    IN_ADVANCE_INTEREST(IN_ADVANCE, INTEREST); //

    private final DueType dueType;
    private final AllocationType allocationType;

}
