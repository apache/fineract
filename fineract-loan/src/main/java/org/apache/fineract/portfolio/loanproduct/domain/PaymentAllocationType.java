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

    PAST_DUE_PENALTY(PAST_DUE, PENALTY, "Past due penalty"), //
    PAST_DUE_FEE(PAST_DUE, FEE, "Past due fee"), //
    PAST_DUE_PRINCIPAL(PAST_DUE, PRINCIPAL, "Past due principal"), //
    PAST_DUE_INTEREST(PAST_DUE, INTEREST, "Past due interest"), //
    DUE_PENALTY(DUE, PENALTY, "Due penalty"), //
    DUE_FEE(DUE, FEE, "Due fee"), //
    DUE_PRINCIPAL(DUE, PRINCIPAL, "Due principal"), //
    DUE_INTEREST(DUE, INTEREST, "Due interest"), //
    IN_ADVANCE_PENALTY(IN_ADVANCE, PENALTY, "In advance penalty"), //
    IN_ADVANCE_FEE(IN_ADVANCE, FEE, "In advance fee"), //
    IN_ADVANCE_PRINCIPAL(IN_ADVANCE, PRINCIPAL, "In advance principal"), //
    IN_ADVANCE_INTEREST(IN_ADVANCE, INTEREST, "In advanced interest"); //

    private final DueType dueType;
    private final AllocationType allocationType;
    private final String humanReadableName;

    public static List<EnumOptionData> getValuesAsEnumOptionDataList() {
        return Arrays.stream(values()).map(v -> new EnumOptionData((long) (v.ordinal() + 1), v.name(), v.getHumanReadableName())).toList();
    }

}
