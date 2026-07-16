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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.springframework.stereotype.Component;

/**
 * Single owner of the running-paid buckets on the {@link WorkingCapitalLoanBalance}. The balance refresh is the last
 * step of payment processing, kept out of the allocation decision (the processor) and out of the materialization (the
 * applier / charge payment handler) so the orchestrators - the forward repayment flow and the reprocessing replay -
 * drive it explicitly.
 *
 * <p>
 * All mutations <strong>accumulate</strong> (never overwrite); the reprocessing replay resets the buckets to zero
 * before replaying, so accumulating per transaction rebuilds the correct running state. Persistence stays with the
 * caller.
 */
@Component
public class WorkingCapitalLoanBalanceUpdater {

    /**
     * Refreshes the balance from an allocation plan. The plan's fee/penalty portions are by construction the sum of the
     * decided fee/penalty charge allocations, so applying the portions here is equivalent to accumulating them charge
     * by charge.
     */
    public void apply(final WorkingCapitalLoanBalance balance, final WorkingCapitalLoanAllocationPlan plan) {
        balance.setPrincipalPaid(MathUtil.nullToZero(balance.getPrincipalPaid()).add(plan.principalPortion()));
        balance.setFeePaid(MathUtil.nullToZero(balance.getFeePaid()).add(plan.feeChargesPortion()));
        balance.setPenaltyPaid(MathUtil.nullToZero(balance.getPenaltyPaid()).add(plan.penaltyChargesPortion()));
        balance.setOverpaymentAmount(MathUtil.nullToZero(balance.getOverpaymentAmount()).add(plan.overpaymentPortion()));
    }
}
