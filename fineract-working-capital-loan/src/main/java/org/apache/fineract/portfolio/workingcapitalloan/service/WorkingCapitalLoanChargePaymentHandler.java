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

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.springframework.stereotype.Component;

/**
 * Single owner of working capital charge settlement ("charge payment handling"). Settling a charge accumulates the
 * charge's {@code amountPaid} and flags it paid once fully settled. Used by the repayment allocation flow and by charge
 * adjustments so the two paths share one implementation.
 *
 * <p>
 * The handler mutates only the charge in place and <strong>accumulates</strong> (never overwrites). The matching
 * fee/penalty bucket on the {@link org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance}
 * is refreshed separately by {@link WorkingCapitalLoanBalanceUpdater}; persistence is the caller's responsibility.
 */
@Component
public class WorkingCapitalLoanChargePaymentHandler {

    public void applyChargePayment(final WorkingCapitalLoanCharge charge, final BigDecimal amount) {
        if (!MathUtil.isGreaterThanZero(amount)) {
            return;
        }
        final BigDecimal newPaid = MathUtil.nullToZero(charge.getAmountPaid()).add(amount);
        charge.setAmountPaid(newPaid);
        if (newPaid.compareTo(MathUtil.nullToZero(charge.getAmount())) >= 0) {
            charge.setPaid(true);
        }
    }
}
