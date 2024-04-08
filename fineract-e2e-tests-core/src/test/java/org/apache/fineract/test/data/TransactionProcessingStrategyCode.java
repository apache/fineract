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
package org.apache.fineract.test.data;

public enum TransactionProcessingStrategyCode {

    PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER("mifos-standard-strategy"), HEAVENSFAMILY_UNIQUE("heavensfamily-strategy"), CREOCORE_UNIQUE(
            "creocore-strategy"), OVERDUE_DUE_FEE_INT_PRINCIPAL("rbi-india-strategy"), PRINCIPAL_INTEREST_PENALTIES_FEES_ORDER(
                    "principal-interest-penalties-fees-order-strategy"), INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER(
                            "interest-principal-penalties-fees-order-strategy"), EARLY_REPAYMENT_STRATEGY(
                                    "early-repayment-strategy"), DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST(
                                            "due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy"), DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE(
                                                    "due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy"), ADVANCED_PAYMENT_ALLOCATION(
                                                            "advanced-payment-allocation-strategy");

    public final String value;

    TransactionProcessingStrategyCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
