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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Result of a Merchant Issued Refund / Payout Refund: the refund transaction itself plus the optional transactions
 * derived from it (interest refund and fee refund).
 */
public record RefundTransactionResult(LoanTransaction refund, @Nullable LoanTransaction interestRefund,
        @Nullable LoanTransaction feeRefund) {

    /**
     * Derived transactions that were created together with the refund.
     */
    public List<LoanTransaction> derivedTransactions() {
        return Stream.of(interestRefund, feeRefund).filter(Objects::nonNull).toList();
    }
}
