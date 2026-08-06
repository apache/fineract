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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanChargePaymentHandlerTest {

    private final WorkingCapitalLoanChargePaymentHandler handler = new WorkingCapitalLoanChargePaymentHandler();

    private static WorkingCapitalLoanCharge charge(final String amount) {
        final WorkingCapitalLoanCharge charge = new WorkingCapitalLoanCharge();
        charge.setAmount(new BigDecimal(amount));
        return charge;
    }

    @Test
    @DisplayName("A partial payment accumulates amountPaid, leaves the charge unpaid, and records a matching paid-by row")
    void partialPaymentAccumulatesAndRecordsPaidBy() {
        final WorkingCapitalLoanTransaction transaction = mock(WorkingCapitalLoanTransaction.class);
        final WorkingCapitalLoanCharge charge = charge("100");

        final WorkingCapitalLoanChargePaidBy paidBy = handler.applyChargePayment(transaction, charge, new BigDecimal("30"));

        assertEquals(0, new BigDecimal("30").compareTo(charge.getAmountPaid()));
        assertFalse(charge.isPaid());
        assertSame(transaction, paidBy.getWcLoanTransaction());
        assertSame(charge, paidBy.getWcLoanCharge());
        assertEquals(0, new BigDecimal("30").compareTo(paidBy.getAmount()));
    }

    @Test
    @DisplayName("A second payment that fully settles the charge accumulates and flags it paid, recording only its own amount")
    void fullSettlementFlagsPaidAndRecordsIncrement() {
        final WorkingCapitalLoanTransaction transaction = mock(WorkingCapitalLoanTransaction.class);
        final WorkingCapitalLoanCharge charge = charge("100");
        handler.applyChargePayment(transaction, charge, new BigDecimal("30"));

        final WorkingCapitalLoanChargePaidBy paidBy = handler.applyChargePayment(transaction, charge, new BigDecimal("70"));

        assertEquals(0, new BigDecimal("100").compareTo(charge.getAmountPaid()));
        assertTrue(charge.isPaid());
        assertEquals(0, new BigDecimal("70").compareTo(paidBy.getAmount()), "paid-by records only this increment, not the running total");
    }

    @Test
    @DisplayName("A non-positive amount settles nothing and returns no paid-by row")
    void zeroAmountIsNoOp() {
        final WorkingCapitalLoanTransaction transaction = mock(WorkingCapitalLoanTransaction.class);
        final WorkingCapitalLoanCharge charge = charge("100");

        final WorkingCapitalLoanChargePaidBy paidBy = handler.applyChargePayment(transaction, charge, BigDecimal.ZERO);

        assertNull(paidBy);
        assertNull(charge.getAmountPaid());
        assertFalse(charge.isPaid());
    }
}
