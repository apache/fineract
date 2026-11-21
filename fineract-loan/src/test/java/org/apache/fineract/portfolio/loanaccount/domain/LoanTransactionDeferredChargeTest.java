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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class LoanTransactionDeferredChargeTest {

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 1, 15);
    private static final MonetaryCurrency CURRENCY = MonetaryCurrency.fromCurrencyData(new CurrencyData("EUR"));

    private final Loan loan = mock(Loan.class);
    private final Office office = mock(Office.class);

    @BeforeAll
    static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    static void tearDown() {
        MONEY_HELPER.close();
    }

    @BeforeEach
    void setBusinessDate() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(new EnumMap<>(Map.of(BusinessDateType.BUSINESS_DATE, TRANSACTION_DATE))));
    }

    @AfterEach
    void resetContext() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void fromIntAndEnumerationsRoundTripForDeferredChargeTypes() {
        assertEquals(LoanTransactionType.DEFERRED_CHARGE, LoanTransactionType.fromInt(50));
        assertEquals(LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION, LoanTransactionType.fromInt(51));
        assertEquals(LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT, LoanTransactionType.fromInt(52));

        assertEquals(50, LoanTransactionType.DEFERRED_CHARGE.getValue());
        assertEquals(51, LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION.getValue());
        assertEquals(52, LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT.getValue());

        final LoanTransactionEnumData deferredCharge = LoanEnumerations.transactionType(LoanTransactionType.DEFERRED_CHARGE);
        assertTrue(deferredCharge.isDeferredCharge());
        assertFalse(deferredCharge.isDeferredChargeAmortization());
        assertFalse(deferredCharge.isDeferredChargeAmortizationAdjustment());
        assertEquals("Deferred Charge", deferredCharge.getValue());

        final LoanTransactionEnumData amortization = LoanEnumerations.transactionType(LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION);
        assertTrue(amortization.isDeferredChargeAmortization());
        assertFalse(amortization.isDeferredCharge());

        final LoanTransactionEnumData adjustment = LoanEnumerations
                .transactionType(LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT);
        assertTrue(adjustment.isDeferredChargeAmortizationAdjustment());
        assertFalse(adjustment.isDeferredChargeAmortization());
    }

    @Test
    void deferredChargeFactoryBooksFeePortionAndIsNonMonetaryButNotAmortizationRelated() {
        when(loan.getOffice()).thenReturn(office);
        final Money amount = Money.of(CURRENCY, new BigDecimal("181.00"));

        final LoanTransaction transaction = LoanTransaction.deferredCharge(loan, amount, TRANSACTION_DATE, ExternalId.empty());

        assertEquals(LoanTransactionType.DEFERRED_CHARGE, transaction.getTypeOf());
        assertEquals(0, new BigDecimal("181.00").compareTo(transaction.getAmount()));
        assertEquals(0, new BigDecimal("181.00").compareTo(transaction.getFeeChargesPortion()));
        assertNull(transaction.getInterestPortion());
        assertTrue(transaction.isDeferredCharge());
        assertTrue(transaction.isNonMonetaryTransaction());
        assertFalse(transaction.isPaymentTransaction());
        assertFalse(transaction.isAmortizationRelated());
    }

    @Test
    void deferredChargeAmortizationFactorySupportsFeeInterestAndMixedPortions() {
        when(loan.getOffice()).thenReturn(office);

        final LoanTransaction feeOnly = LoanTransaction.deferredChargeAmortization(loan, TRANSACTION_DATE, new BigDecimal("1.00"),
                BigDecimal.ZERO, ExternalId.empty());
        assertEquals(LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION, feeOnly.getTypeOf());
        assertEquals(0, new BigDecimal("1.00").compareTo(feeOnly.getAmount()));
        assertEquals(0, new BigDecimal("1.00").compareTo(feeOnly.getFeeChargesPortion()));
        assertNull(feeOnly.getInterestPortion());
        assertTrue(feeOnly.isDeferredChargeAmortization());
        assertTrue(feeOnly.isNonMonetaryTransaction());
        assertTrue(feeOnly.isAmortizationRelated());
        assertFalse(feeOnly.isPaymentTransaction());

        final LoanTransaction interestOnly = LoanTransaction.deferredChargeAmortization(loan, TRANSACTION_DATE, BigDecimal.ZERO,
                new BigDecimal("2.00"), ExternalId.empty());
        assertEquals(0, new BigDecimal("2.00").compareTo(interestOnly.getAmount()));
        assertNull(interestOnly.getFeeChargesPortion());
        assertEquals(0, new BigDecimal("2.00").compareTo(interestOnly.getInterestPortion()));

        final LoanTransaction mixed = LoanTransaction.deferredChargeAmortization(loan, TRANSACTION_DATE, new BigDecimal("3.00"),
                new BigDecimal("4.00"), ExternalId.empty());
        assertEquals(0, new BigDecimal("7.00").compareTo(mixed.getAmount()));
        assertEquals(0, new BigDecimal("3.00").compareTo(mixed.getFeeChargesPortion()));
        assertEquals(0, new BigDecimal("4.00").compareTo(mixed.getInterestPortion()));
    }

    @Test
    void deferredChargeAmortizationAdjustmentFactoryAndReversedPredicates() {
        when(loan.getOffice()).thenReturn(office);

        final LoanTransaction adjustment = LoanTransaction.deferredChargeAmortizationAdjustment(loan, TRANSACTION_DATE,
                new BigDecimal("5.00"), new BigDecimal("1.00"), ExternalId.empty());
        assertEquals(LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT, adjustment.getTypeOf());
        assertEquals(0, new BigDecimal("6.00").compareTo(adjustment.getAmount()));
        assertTrue(adjustment.isDeferredChargeAmortizationAdjustment());
        assertTrue(adjustment.isNonMonetaryTransaction());
        assertTrue(adjustment.isAmortizationRelated());

        ReflectionTestUtils.setField(adjustment, "reversed", true);
        assertFalse(adjustment.isDeferredChargeAmortizationAdjustment());
        assertFalse(adjustment.isNonMonetaryTransaction());
        assertFalse(adjustment.isAmortizationRelated());
    }

    @Test
    void getLastUserTransactionIgnoresDeferredChargeAmortizationTypes() {
        final Loan loanEntity = new Loan();
        final LoanTransaction chargeOff = mock(LoanTransaction.class);
        when(chargeOff.isNotReversed()).thenReturn(true);
        when(chargeOff.isAccrual()).thenReturn(false);
        when(chargeOff.isAccrualAdjustment()).thenReturn(false);
        when(chargeOff.isIncomePosting()).thenReturn(false);
        when(chargeOff.isAmortizationRelated()).thenReturn(false);

        final LoanTransaction amortization = mock(LoanTransaction.class);
        when(amortization.isNotReversed()).thenReturn(true);
        when(amortization.isAccrual()).thenReturn(false);
        when(amortization.isAccrualAdjustment()).thenReturn(false);
        when(amortization.isIncomePosting()).thenReturn(false);
        when(amortization.isAmortizationRelated()).thenReturn(true);

        ReflectionTestUtils.setField(loanEntity, "loanTransactions", List.of(chargeOff, amortization));

        assertEquals(chargeOff, loanEntity.getLastUserTransaction());
    }
}
