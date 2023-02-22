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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests {@link Loan}.
 */
public class LoanTest {

    /**
     * Tests {@link Loan#getCharges()} with charges.
     */
    @Test
    public void testGetChargesWithCharges() {
        Loan loan = new Loan();
        ReflectionTestUtils.setField(loan, "charges", Collections.singleton(buildLoanCharge()));

        final Collection<LoanCharge> chargeIds = loan.getCharges();

        assertEquals(1, chargeIds.size());
    }

    /**
     * Tests {@link Loan#getCharges()} with no charges.
     */
    @Test
    public void testGetChargesWithNoCharges() {
        final Loan loan = new Loan();

        final Collection<LoanCharge> chargeIds = loan.getCharges();

        assertEquals(0, chargeIds.size());
    }

    /**
     * Tests {@link Loan#getCharges()} with null to make sure NPE is not thrown.
     */
    @Test
    public void testGetChargesWithNull() {
        final Loan loan = new Loan();
        ReflectionTestUtils.setField(loan, "charges", null);

        final Collection<LoanCharge> chargeIds = loan.getCharges();

        assertEquals(0, chargeIds.size());
    }

    /**
     * Tests {@link Loan#findChargedOffTransaction()} with empty list
     */
    @Test
    public void testFindChargedOffTransactionEmptyList() {
        final Loan loan = new Loan();
        final LoanTransaction chargedOffTransaction = loan.findChargedOffTransaction();
        assertNull(chargedOffTransaction);
    }

    /**
     * Tests {@link Loan#findChargedOffTransaction()} where there is no charge-off transaction
     */
    @Test
    public void testFindChargedOffTransactionNoChargeOffTransaction() {
        final Loan loan = new Loan();
        final LoanTransaction loanTransaction = new LoanTransaction();
        ReflectionTestUtils.setField(loan, "loanTransactions", List.of(loanTransaction));
        final LoanTransaction chargedOffTransaction = loan.findChargedOffTransaction();
        assertNull(chargedOffTransaction);
    }

    /**
     * Tests {@link Loan#findChargedOffTransaction()} where there is charge-off transaction
     */
    @Test
    public void testFindChargedOffTransactionWithChargeOffTransaction() {
        final Loan loan = new Loan();

        final LoanTransaction loanTransaction = new LoanTransaction();
        final LoanTransaction loanTransaction2 = Mockito.mock(LoanTransaction.class);
        final LoanTransaction loanTransaction3 = new LoanTransaction();
        when(loanTransaction2.isNotReversed()).thenReturn(Boolean.TRUE);
        when(loanTransaction2.isChargeOff()).thenReturn(Boolean.TRUE);
        ReflectionTestUtils.setField(loan, "loanTransactions", List.of(loanTransaction, loanTransaction2, loanTransaction3));
        final LoanTransaction chargedOffTransaction = loan.findChargedOffTransaction();
        assertNotNull(chargedOffTransaction);
        assertEquals(loanTransaction2, chargedOffTransaction);
    }

    /**
     * Tests {@link Loan#findChargedOffTransaction()} where there is charge-off transaction but reversed
     */
    @Test
    public void testFindChargedOffTransactionWithReversedChargeOffTransaction() {
        final Loan loan = new Loan();

        final LoanTransaction loanTransaction = new LoanTransaction();
        final LoanTransaction loanTransaction2 = Mockito.mock(LoanTransaction.class);
        final LoanTransaction loanTransaction3 = new LoanTransaction();
        when(loanTransaction2.isReversed()).thenReturn(Boolean.TRUE);
        when(loanTransaction2.isChargeOff()).thenReturn(Boolean.TRUE);
        ReflectionTestUtils.setField(loan, "loanTransactions", List.of(loanTransaction, loanTransaction2, loanTransaction3));
        final LoanTransaction chargedOffTransaction = loan.findChargedOffTransaction();
        assertNull(chargedOffTransaction);
    }

    /**
     * Tests {@link Loan#getLastUserTransaction()} with empty list
     */
    @Test
    public void testGetLastUserTransactionEmptyList() {
        final Loan loan = new Loan();
        final LoanTransaction userTransaction = loan.getLastUserTransaction();
        assertNull(userTransaction);
    }

    /**
     * Tests {@link Loan#getLastUserTransaction()} where there are user transactions
     */
    @Test
    public void testGetLastUserTransaction() {
        final Loan loan = new Loan();

        final LoanTransaction loanTransaction = Mockito.mock(LoanTransaction.class);
        when(loanTransaction.isNotReversed()).thenReturn(Boolean.TRUE);
        when(loanTransaction.isAccrualTransaction()).thenReturn(Boolean.FALSE);
        final LoanTransaction loanTransaction2 = Mockito.mock(LoanTransaction.class);
        when(loanTransaction2.isNotReversed()).thenReturn(Boolean.TRUE);
        when(loanTransaction2.isAccrualTransaction()).thenReturn(Boolean.FALSE);
        final LoanTransaction loanTransaction3 = Mockito.mock(LoanTransaction.class);
        when(loanTransaction3.isNotReversed()).thenReturn(Boolean.TRUE);
        when(loanTransaction3.isAccrualTransaction()).thenReturn(Boolean.TRUE);
        ReflectionTestUtils.setField(loan, "loanTransactions", List.of(loanTransaction, loanTransaction2, loanTransaction3));
        final LoanTransaction userTransaction = loan.getLastUserTransaction();
        assertNotNull(userTransaction);
        assertEquals(loanTransaction2, userTransaction);
    }

    /**
     * Builds a new loan charge.
     *
     * @return the {@link LoanCharge}
     */
    private LoanCharge buildLoanCharge() {
        return new LoanCharge(mock(Loan.class), mock(Charge.class), new BigDecimal(100), new BigDecimal(100),
                ChargeTimeType.TRANCHE_DISBURSEMENT, ChargeCalculationType.FLAT, LocalDate.of(2022, 6, 27), ChargePaymentMode.REGULAR, 1,
                new BigDecimal(100), ExternalId.generate());
    }
}
