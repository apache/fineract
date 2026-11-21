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

import java.util.List;

/**
 * One parsed entry of the {@code creditAllocation} request parameter, passed from {@link CreditAllocationsJsonParser}
 * to {@link CreditAllocationsValidator} before it becomes a {@link LoanProductCreditAllocationRule}.
 *
 * @param transactionType
 *            the requested transaction type, or null when it is not a valid one
 * @param allocationStrategy
 *            the requested strategy ({@link CreditAllocationStrategy#RULE_BASED} when none was sent), or null when it
 *            is not a valid one
 * @param allocationTypes
 *            the parsed allocation order; empty unless the strategy is rule based and an order was sent
 * @param creditAllocationOrderProvided
 *            whether the request contained a non-empty {@code creditAllocationOrder}
 */
public record CreditAllocationRequest(CreditAllocationTransactionType transactionType, CreditAllocationStrategy allocationStrategy,
        List<AllocationType> allocationTypes, boolean creditAllocationOrderProvided) {

    public LoanProductCreditAllocationRule toLoanProductCreditAllocationRule() {
        return new LoanProductCreditAllocationRule(null, transactionType, allocationTypes, allocationStrategy);
    }
}
