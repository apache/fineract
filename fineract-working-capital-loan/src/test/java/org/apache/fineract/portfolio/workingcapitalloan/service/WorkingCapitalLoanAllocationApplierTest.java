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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan.ChargeAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanChargeAllocationMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class WorkingCapitalLoanAllocationApplierTest {

    private final WorkingCapitalLoanAllocationApplier applier = new WorkingCapitalLoanAllocationApplier(
            new WorkingCapitalLoanChargePaymentHandler());

    private static WorkingCapitalLoanCharge charge(final long id, final String amount, final boolean penalty) {
        final WorkingCapitalLoanCharge charge = new WorkingCapitalLoanCharge();
        ReflectionTestUtils.setField(charge, "id", id);
        charge.setAmount(new BigDecimal(amount));
        charge.setPenaltyCharge(penalty);
        return charge;
    }

    @Test
    @DisplayName("Applying a plan settles each decided charge and returns a paid-by row per charge allocation")
    void returnsPaidByRowPerChargeAllocation() {
        final WorkingCapitalLoanTransaction transaction = mock(WorkingCapitalLoanTransaction.class);
        final WorkingCapitalLoanCharge fee = charge(10L, "30", false);
        final WorkingCapitalLoanCharge penalty = charge(20L, "50", true);
        final WorkingCapitalLoanAllocationPlan plan = new WorkingCapitalLoanAllocationPlan(new BigDecimal("20"), new BigDecimal("30"),
                new BigDecimal("50"), BigDecimal.ZERO,
                List.of(new ChargeAllocation(10L, false, new BigDecimal("30")), new ChargeAllocation(20L, true, new BigDecimal("50"))));

        final WorkingCapitalLoanAllocationApplier.Result result = applier.apply(transaction, plan, List.of(fee, penalty));

        // Charges settled in place.
        assertEquals(0, new BigDecimal("30").compareTo(fee.getAmountPaid()));
        assertEquals(0, new BigDecimal("50").compareTo(penalty.getAmountPaid()));
        assertTrue(fee.isPaid());
        assertTrue(penalty.isPaid());

        // Aggregate allocation portions carried onto the allocation entity.
        assertEquals(0, new BigDecimal("20").compareTo(result.allocation().getPrincipalPortion()));
        assertEquals(0, new BigDecimal("30").compareTo(result.allocation().getFeeChargesPortion()));
        assertEquals(0, new BigDecimal("50").compareTo(result.allocation().getPenaltyChargesPortion()));

        // One paid-by row per charge allocation, attributed to the transaction and the exact charge/amount.
        final Map<Long, WorkingCapitalLoanChargePaidBy> byChargeId = result.chargesPaidBy().stream()
                .collect(Collectors.toMap(paidBy -> paidBy.getWcLoanCharge().getId(), Function.identity()));
        assertEquals(2, byChargeId.size());
        assertSame(transaction, byChargeId.get(10L).getWcLoanTransaction());
        assertEquals(0, new BigDecimal("30").compareTo(byChargeId.get(10L).getAmount()));
        assertSame(penalty, byChargeId.get(20L).getWcLoanCharge());
        assertEquals(0, new BigDecimal("50").compareTo(byChargeId.get(20L).getAmount()));
    }

    @Test
    @DisplayName("A principal-only plan produces no paid-by rows but still builds the allocation")
    void principalOnlyPlanProducesNoPaidByRows() {
        final WorkingCapitalLoanTransaction transaction = mock(WorkingCapitalLoanTransaction.class);
        final WorkingCapitalLoanAllocationPlan plan = new WorkingCapitalLoanAllocationPlan(new BigDecimal("100"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        final WorkingCapitalLoanAllocationApplier.Result result = applier.apply(transaction, plan, List.of());

        assertTrue(result.chargesPaidBy().isEmpty());
        assertEquals(0, new BigDecimal("100").compareTo(result.allocation().getPrincipalPortion()));
    }

    @Test
    @DisplayName("A plan referencing a charge absent from the loan's charges fails loudly as a data inconsistency")
    void unknownChargeInPlanFailsFast() {
        final WorkingCapitalLoanTransaction transaction = mock(WorkingCapitalLoanTransaction.class);
        final WorkingCapitalLoanCharge fee = charge(10L, "30", false);
        final WorkingCapitalLoanAllocationPlan plan = new WorkingCapitalLoanAllocationPlan(BigDecimal.ZERO, new BigDecimal("30"),
                BigDecimal.ZERO, BigDecimal.ZERO, List.of(new ChargeAllocation(99L, false, new BigDecimal("30"))));

        assertThrows(WorkingCapitalLoanChargeAllocationMismatchException.class, () -> applier.apply(transaction, plan, List.of(fee)));
    }
}
