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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan.ChargeAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest.ChargeBalance;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanPaymentAllocationProcessorTest {

    private static final LocalDate TXN_DATE = LocalDate.of(2026, 6, 19);

    private final WorkingCapitalLoanPaymentAllocationProcessor processor = new WorkingCapitalLoanPaymentAllocationProcessor();

    @Test
    @DisplayName("With no charges and principal-only allocation, the payment settles principal and the excess becomes overpayment")
    void principalOnlyFallbackAllocatesToPrincipalAndLeavesOverpayment() {
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(TXN_DATE, amount("120"),
                List.of(WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL), amount("100"), List.of());

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("100", plan.principalPortion());
        assertAmount("0", plan.feeChargesPortion());
        assertAmount("0", plan.penaltyChargesPortion());
        assertAmount("20", plan.overpaymentPortion());
        assertTrue(plan.chargeAllocations().isEmpty());
    }

    @Test
    @DisplayName("A fee-then-principal order first settles the due fee in full, then applies the remainder to principal")
    void feeThenPrincipalSplitsAmountAcrossChargeAndPrincipal() {
        final ChargeBalance fee = new ChargeBalance(10L, amount("30"), TXN_DATE, false);
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(TXN_DATE, amount("100"),
                List.of(WorkingCapitalPaymentAllocationType.DUE_FEE, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL), amount("200"),
                List.of(fee));

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("30", plan.feeChargesPortion());
        assertAmount("70", plan.principalPortion());
        assertAmount("0", plan.penaltyChargesPortion());
        assertAmount("0", plan.overpaymentPortion());
        assertEquals(1, plan.chargeAllocations().size());
        final ChargeAllocation allocation = plan.chargeAllocations().get(0);
        assertEquals(10L, allocation.chargeId());
        assertFalse(allocation.penalty(), "ChargeAllocation for a fee charge must not be flagged as penalty");
        assertAmount("30", allocation.amount());
    }

    @Test
    @DisplayName("A penalty-then-fee-then-principal order settles penalty, then fee, then principal in that sequence")
    void penaltyThenFeeThenPrincipalFollowsConfiguredOrder() {
        final ChargeBalance penalty = new ChargeBalance(1L, amount("15"), TXN_DATE, true);
        final ChargeBalance fee = new ChargeBalance(2L, amount("25"), TXN_DATE, false);
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(
                TXN_DATE, amount("100"), List.of(WorkingCapitalPaymentAllocationType.DUE_PENALTY,
                        WorkingCapitalPaymentAllocationType.DUE_FEE, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL),
                amount("200"), List.of(penalty, fee));

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("15", plan.penaltyChargesPortion());
        assertAmount("25", plan.feeChargesPortion());
        assertAmount("60", plan.principalPortion());
        assertAmount("0", plan.overpaymentPortion());
        assertEquals(2, plan.chargeAllocations().size());
    }

    @Test
    @DisplayName("The DUE_FEE bucket settles only charges already due and skips charges dated in the future")
    void dueBucketSkipsInAdvanceCharges() {
        final ChargeBalance dueFee = new ChargeBalance(1L, amount("20"), TXN_DATE.minusDays(1), false);
        final ChargeBalance inAdvanceFee = new ChargeBalance(2L, amount("20"), TXN_DATE.plusDays(5), false);
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(TXN_DATE, amount("50"),
                List.of(WorkingCapitalPaymentAllocationType.DUE_FEE, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL), amount("200"),
                List.of(dueFee, inAdvanceFee));

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("20", plan.feeChargesPortion());
        assertAmount("30", plan.principalPortion());
        assertEquals(1, plan.chargeAllocations().size());
        assertEquals(1L, plan.chargeAllocations().get(0).chargeId());
    }

    @Test
    @DisplayName("The IN_ADVANCE_FEE bucket settles only charges dated in the future and skips already-due charges")
    void inAdvanceBucketMatchesOnlyFutureCharges() {
        final ChargeBalance dueFee = new ChargeBalance(1L, amount("20"), TXN_DATE.minusDays(1), false);
        final ChargeBalance inAdvanceFee = new ChargeBalance(2L, amount("20"), TXN_DATE.plusDays(5), false);
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(TXN_DATE, amount("50"),
                List.of(WorkingCapitalPaymentAllocationType.IN_ADVANCE_FEE, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL),
                amount("200"), List.of(dueFee, inAdvanceFee));

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("20", plan.feeChargesPortion());
        assertAmount("30", plan.principalPortion());
        assertEquals(1, plan.chargeAllocations().size());
        assertEquals(2L, plan.chargeAllocations().get(0).chargeId());
    }

    @Test
    @DisplayName("A charge with a null due date is treated as due and settled by the DUE_FEE bucket")
    void chargeWithNullDueDateIsTreatedAsDue() {
        final ChargeBalance fee = new ChargeBalance(1L, amount("20"), null, false);
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(TXN_DATE, amount("20"),
                List.of(WorkingCapitalPaymentAllocationType.DUE_FEE), amount("200"), List.of(fee));

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("20", plan.feeChargesPortion());
        assertEquals(1, plan.chargeAllocations().size());
    }

    @Test
    @DisplayName("Within a bucket the oldest charge is settled first, then the remainder partially settles the next")
    void oldestChargeFirstWithinBucket() {
        final ChargeBalance older = new ChargeBalance(1L, amount("30"), TXN_DATE.minusDays(10), false);
        final ChargeBalance newer = new ChargeBalance(2L, amount("30"), TXN_DATE.minusDays(1), false);
        // amount only covers one charge in full plus part of the next
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(TXN_DATE, amount("40"),
                List.of(WorkingCapitalPaymentAllocationType.DUE_FEE), amount("200"), List.of(older, newer));

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("40", plan.feeChargesPortion());
        assertEquals(2, plan.chargeAllocations().size());
        assertEquals(1L, plan.chargeAllocations().get(0).chargeId());
        assertAmount("30", plan.chargeAllocations().get(0).amount());
        assertEquals(2L, plan.chargeAllocations().get(1).chargeId());
        assertAmount("10", plan.chargeAllocations().get(1).amount());
    }

    @Test
    @DisplayName("When fee and principal are both fully settled, the leftover payment becomes overpayment")
    void leftoverBecomesOverpaymentWhenEverythingIsSettled() {
        final ChargeBalance fee = new ChargeBalance(1L, amount("20"), TXN_DATE, false);
        final WorkingCapitalLoanAllocationRequest request = new WorkingCapitalLoanAllocationRequest(TXN_DATE, amount("100"),
                List.of(WorkingCapitalPaymentAllocationType.DUE_FEE, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL), amount("50"),
                List.of(fee));

        final WorkingCapitalLoanAllocationPlan plan = processor.plan(request);

        assertAmount("20", plan.feeChargesPortion());
        assertAmount("50", plan.principalPortion());
        assertAmount("30", plan.overpaymentPortion());
    }

    private static BigDecimal amount(final String value) {
        return new BigDecimal(value);
    }

    private static void assertAmount(final String expected, final BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }
}
