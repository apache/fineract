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
import lombok.Getter;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;

/**
 * One repayment's still-unallocated remainder, offered up to settle a charge when an overpayment is consumed.
 *
 * <p>
 * Holds live JPA entities and mutates them, so it is service machinery rather than a DTO, and deliberately carries no
 * generated {@code equals}/{@code hashCode}/{@code toString}: identity would be derived from mutable entities, and a
 * {@code toString} would drag lazy associations into being just by logging it.
 */
@Getter
class OverpaymentFunder {

    private final WorkingCapitalLoanTransaction transaction;
    private final WorkingCapitalLoanTransactionAllocation allocation;

    OverpaymentFunder(final WorkingCapitalLoanTransaction transaction, final WorkingCapitalLoanTransactionAllocation allocation) {
        this.transaction = transaction;
        this.allocation = allocation;
    }

    /**
     * What this transaction can still offer, read straight off the allocation rather than cached alongside it. The
     * headroom and the overpayment portion are one quantity in two places if kept separately, and {@link #fund} moves
     * the portion - a cached copy could drift and let funding drive the column negative while still reporting room.
     */
    BigDecimal getAvailable() {
        return MathUtil.nullToZero(allocation.getOverpaymentPortion());
    }

    /**
     * Moves {@code amount} out of this transaction's still-unallocated remainder and into its fee or penalty portion.
     * The remainder is exactly what the allocation records as its overpayment portion, so that portion shrinks by the
     * same amount: the portions must keep summing to the transaction amount, and the overpayment portion is read back
     * on its own (by the undo balance unwind and the overpayment journal leg) rather than being re-derived.
     */
    void fund(final BigDecimal amount, final boolean penalty) {
        if (penalty) {
            allocation.setPenaltyChargesPortion(MathUtil.nullToZero(allocation.getPenaltyChargesPortion()).add(amount));
        } else {
            allocation.setFeeChargesPortion(MathUtil.nullToZero(allocation.getFeeChargesPortion()).add(amount));
        }
        allocation.setOverpaymentPortion(MathUtil.subtract(MathUtil.nullToZero(allocation.getOverpaymentPortion()), amount));
    }
}
