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

public enum TransactionProcessingStrategy {

    PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER(1), HEAVENSFAMILY_UNIQUE(2), CREOCORE_UNIQUE(3), OVERDUE_DUE_FEE_INT_PRINCIPAL(
            4), PRINCIPAL_INTEREST_PENALTIES_FEES_ORDER(5), INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER(6), EARLY_REPAYMENT_STRATEGY(7);

    public final Integer value;

    TransactionProcessingStrategy(Integer value) {
        this.value = value;
    }
}
