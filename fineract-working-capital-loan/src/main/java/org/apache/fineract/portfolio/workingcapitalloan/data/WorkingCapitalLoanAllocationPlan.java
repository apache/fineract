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

package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Immutable output of the working capital allocation processor: the decided split of a repayment-like amount. The
 * aggregate portions feed the transaction allocation entity and the balance buckets; the per-charge
 * {@link ChargeAllocation} breakdown lets the materializer settle exact charges without re-deriving the match.
 */
public record WorkingCapitalLoanAllocationPlan(BigDecimal principalPortion, BigDecimal feeChargesPortion, BigDecimal penaltyChargesPortion,
        BigDecimal overpaymentPortion, List<ChargeAllocation> chargeAllocations) {

    public record ChargeAllocation(Long chargeId, boolean penalty, BigDecimal amount) {
    }
}
