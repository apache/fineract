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
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

@Getter
@RequiredArgsConstructor
public enum PaymentAllocationTransactionType {

    DEFAULT(null, "Default"), //
    REPAYMENT(LoanTransactionType.REPAYMENT, "Repayment"), //
    DOWN_PAYMENT(LoanTransactionType.DOWN_PAYMENT, "Down payment"), //
    MERCHANT_ISSUED_REFUND(LoanTransactionType.MERCHANT_ISSUED_REFUND, "Merchant issued refund"), //
    PAYOUT_REFUND(LoanTransactionType.PAYOUT_REFUND, "Payout refund"), //
    GOODWILL_CREDIT(LoanTransactionType.GOODWILL_CREDIT, "Goodwill credit"), //
    CHARGE_REFUND(LoanTransactionType.CHARGE_REFUND, "Charge refund"), //
    CHARGE_ADJUSTMENT(LoanTransactionType.CHARGE_ADJUSTMENT, "Charge adjustment"), //
    WAIVE_INTEREST(LoanTransactionType.WAIVE_INTEREST, "Waive interest"), //
    CHARGE_PAYMENT(LoanTransactionType.CHARGE_PAYMENT, "Charge payment"), //
    REFUND_FOR_ACTIVE_LOAN(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN, "Refund for active loan"), //
    INTEREST_PAYMENT_WAIVER(LoanTransactionType.INTEREST_PAYMENT_WAIVER, "Interest payment waiver"), //
    INTEREST_REFUND(LoanTransactionType.INTEREST_REFUND, "Interest refund");

    private final LoanTransactionType loanTransactionType;
    private final String humanReadableName;

    public static List<EnumOptionData> getValuesAsEnumOptionDataList() {
        return Arrays.stream(values()).map(v -> new EnumOptionData((long) (v.ordinal() + 1), v.name(), v.getHumanReadableName())).toList();
    }
}
