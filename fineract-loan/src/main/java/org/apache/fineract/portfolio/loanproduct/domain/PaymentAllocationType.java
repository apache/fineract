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

// TODO FINERACT-1932-Fineract modularization: Move to fineract-progressive-loan module after refactor of Loan and LoanProduct classes
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.FEE;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.INTEREST;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PENALTY;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PRINCIPAL;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.DUE;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.IN_ADVANCE;
import static org.apache.fineract.portfolio.loanproduct.domain.DueType.PAST_DUE;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Getter
@RequiredArgsConstructor
public enum PaymentAllocationType {

    PAST_DUE_PENALTY(PAST_DUE, PENALTY, "Past due penalty", 1), //
    PAST_DUE_FEE(PAST_DUE, FEE, "Past due fee", 2), //
    PAST_DUE_PRINCIPAL(PAST_DUE, PRINCIPAL, "Past due principal", 3), //
    PAST_DUE_INTEREST(PAST_DUE, INTEREST, "Past due interest", 4), //
    DUE_PENALTY(DUE, PENALTY, "Due penalty", 5), //
    DUE_FEE(DUE, FEE, "Due fee", 6), //
    DUE_PRINCIPAL(DUE, PRINCIPAL, "Due principal", 7), //
    DUE_INTEREST(DUE, INTEREST, "Due interest", 8), //
    IN_ADVANCE_PENALTY(IN_ADVANCE, PENALTY, "In advance penalty", 9), //
    IN_ADVANCE_FEE(IN_ADVANCE, FEE, "In advance fee", 10), //
    IN_ADVANCE_PRINCIPAL(IN_ADVANCE, PRINCIPAL, "In advance principal", 11), //
    IN_ADVANCE_INTEREST(IN_ADVANCE, INTEREST, "In advanced interest", 12); //

    private final DueType dueType;
    private final AllocationType allocationType;
    private final String humanReadableName;
    private final long id;

    public static List<EnumOptionData> getValuesAsEnumOptionDataList() {
        return Arrays.stream(values()).map(v -> new EnumOptionData(v.getId(), v.name(), v.getHumanReadableName())).toList();
    }

}
