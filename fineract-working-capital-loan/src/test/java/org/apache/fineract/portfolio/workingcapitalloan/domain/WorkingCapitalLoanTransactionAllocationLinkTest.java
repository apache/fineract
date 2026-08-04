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

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The one-allocation-per-transaction rule, which lives in code rather than in the database.
 *
 * <p>
 * The association is mapped {@code orphanRemoval = true}, so attaching a second allocation would not raise the
 * {@code uq_m_wc_loan_transaction_allocation_transaction_id} unique constraint - there would never be a second row to
 * reject. It would instead delete the existing allocation and replace it, discarding its identity and audit trail
 * without a word.
 */
class WorkingCapitalLoanTransactionAllocationLinkTest {

    @Test
    @DisplayName("creating an allocation wires both sides of the association")
    void creatingAnAllocationWiresBothSides() {
        final WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        assertNull(transaction.getAllocation(), "a fresh transaction has no allocation");

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation.forPortions(transaction, TEN,
                ZERO, ZERO, ZERO);

        assertSame(allocation, transaction.getAllocation(), "the transaction's inverse side must see the new allocation immediately");
        assertSame(transaction, allocation.getWcLoanTransaction(), "the allocation must own the link back to its transaction");
    }

    @Test
    @DisplayName("re-attaching the very same allocation is a no-op")
    void reAttachingTheSameAllocationIsAllowed() {
        final WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation.forPortions(transaction, TEN,
                ZERO, ZERO, ZERO);

        transaction.attachAllocation(allocation);

        assertSame(allocation, transaction.getAllocation(), "attaching the allocation already in place must leave it in place");
    }

    @Test
    @DisplayName("attaching a second, different allocation fails instead of silently replacing the first")
    void attachingASecondAllocationFails() {
        final WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        final WorkingCapitalLoanTransactionAllocation original = WorkingCapitalLoanTransactionAllocation.forPortions(transaction, TEN, ZERO,
                ZERO, ZERO);

        assertThrows(IllegalStateException.class,
                () -> WorkingCapitalLoanTransactionAllocation.forPortions(transaction, ONE, ZERO, ZERO, ZERO),
                "a second allocation must be rejected rather than orphan-removing the first");
        assertSame(original, transaction.getAllocation(), "the original allocation must survive the rejected attempt");
    }

    @Test
    @DisplayName("the guard covers every allocation factory, not just the repayment one")
    void guardCoversTheOtherFactories() {
        final WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        WorkingCapitalLoanTransactionAllocation.forPrincipalAllocation(transaction, TEN);

        assertThrows(IllegalStateException.class,
                () -> WorkingCapitalLoanTransactionAllocation.forCreditBalanceRefund(transaction, ONE, ZERO));
        assertThrows(IllegalStateException.class, () -> WorkingCapitalLoanTransactionAllocation.forChargeAccrual(transaction, ONE, false));
        assertThrows(IllegalStateException.class, () -> WorkingCapitalLoanTransactionAllocation.forDisbursementDiscount(transaction, ONE));
        assertThrows(IllegalStateException.class, () -> WorkingCapitalLoanTransactionAllocation.forDiscountFeeAdjustment(transaction, ONE));
    }
}
