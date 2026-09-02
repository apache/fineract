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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.springframework.stereotype.Component;

/**
 * Keeps the waiver behaviour off {@link WorkingCapitalLoanCharge} / {@link WorkingCapitalLoanBalance}, which stay data
 * holders. Unlike a write-off the buckets are adjusted by amount rather than zeroed on undo: a loan can carry several
 * waivers, each reversible on its own.
 */
@Component
public class WorkingCapitalLoanChargeWaiverDomainService {

    /**
     * No cap on {@code amount}: the caller captures the charge's outstanding remainder before this call and stores the
     * same figure on the waiver transaction.
     */
    public void applyWaived(final WorkingCapitalLoanCharge charge, final WorkingCapitalLoanBalance balance, final BigDecimal amount) {
        charge.setAmountWaived(MathUtil.add(charge.getAmountWaived(), amount));
        if (charge.isPenaltyCharge()) {
            balance.setPenaltyWaived(MathUtil.add(balance.getPenaltyWaived(), amount));
        } else {
            balance.setFeeWaived(MathUtil.add(balance.getFeeWaived(), amount));
        }
    }

    /**
     * The reversed transaction's amount can be taken back out as it stands: a waiver never takes part in reprocessing,
     * so what was recorded and what was applied cannot have drifted apart.
     */
    public void undoWaive(final WorkingCapitalLoanCharge charge, final WorkingCapitalLoanBalance balance, final BigDecimal amount) {
        charge.setAmountWaived(MathUtil.subtractToZero(charge.getAmountWaived(), amount));
        if (charge.isPenaltyCharge()) {
            balance.setPenaltyWaived(MathUtil.subtractToZero(balance.getPenaltyWaived(), amount));
        } else {
            balance.setFeeWaived(MathUtil.subtractToZero(balance.getFeeWaived(), amount));
        }
    }
}
