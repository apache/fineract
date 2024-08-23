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

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

@Getter
@RequiredArgsConstructor
public enum LoanSupportedInterestRefundTypes {

    MERCHANT_ISSUED_REFUND(LoanTransactionType.MERCHANT_ISSUED_REFUND, "loanRefundType.merchant_issued_refund", "Merchant issued refund"), //
    PAYOUT_REFUND(LoanTransactionType.PAYOUT_REFUND, "loanRefundType.payout_refund", "Payout refund"), //
    ;

    private final LoanTransactionType transactionType;
    private final String code;
    private final String humanReadableName;

    public static List<StringEnumOptionData> getValuesAsStringEnumOptionDataList() {
        return Arrays.stream(values()).map(v -> new StringEnumOptionData(v.name(), v.getCode(), v.getHumanReadableName())).toList();
    }

    public StringEnumOptionData getValueAsStringEnumOptionData() {
        return new StringEnumOptionData(name(), getCode(), getHumanReadableName());
    }
}
