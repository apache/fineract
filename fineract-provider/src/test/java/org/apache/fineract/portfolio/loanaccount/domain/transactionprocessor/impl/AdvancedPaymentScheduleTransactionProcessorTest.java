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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdvancedPaymentScheduleTransactionProcessorTest {

    private final LocalDate transactionDate = LocalDate.of(2023, 7, 11);
    private static final MonetaryCurrency MONETARY_CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);
    private AdvancedPaymentScheduleTransactionProcessor underTest;

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @BeforeEach
    public void setUp() {
        underTest = new AdvancedPaymentScheduleTransactionProcessor();

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, transactionDate)));
    }

    @Test
    public void chargePaymentTransactionTestWithExactAmount() {
        final BigDecimal chargeAmount = BigDecimal.valueOf(100);
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        LocalDate transactionDate = LocalDate.of(2023, 1, 5);
        LoanTransaction loanTransaction = Mockito.mock(LoanTransaction.class);
        MonetaryCurrency currency = MONETARY_CURRENCY;
        LoanCharge charge = Mockito.mock(LoanCharge.class);
        LoanChargePaidBy chargePaidBy = Mockito.mock(LoanChargePaidBy.class);
        Money overpaidAmount = Money.zero(currency);
        Money zero = Money.zero(currency);
        Loan loan = Mockito.mock(Loan.class);
        Money chargeAmountMoney = Money.of(currency, chargeAmount);
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, disbursementDate, disbursementDate.plusMonths(1), BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), chargeAmount, BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));

        Mockito.when(loanTransaction.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_PAYMENT);
        Mockito.when(chargePaidBy.getLoanCharge()).thenReturn(charge);
        Mockito.when(loanTransaction.getLoanChargesPaid()).thenReturn(Set.of(chargePaidBy));
        Mockito.when(loanTransaction.getAmount()).thenReturn(chargeAmount);
        Mockito.when(loanTransaction.getAmount(currency)).thenReturn(chargeAmountMoney);
        Mockito.when(loanTransaction.getTransactionDate()).thenReturn(transactionDate);
        Mockito.when(charge.getAmountOutstanding(currency)).thenReturn(chargeAmountMoney);
        Mockito.when(loanTransaction.getLoan()).thenReturn(loan);
        Mockito.when(loan.getDisbursementDate()).thenReturn(disbursementDate);
        Mockito.when(charge.isDueForCollectionFromIncludingAndUpToAndIncluding(disbursementDate, installment.getDueDate()))
                .thenReturn(true);
        Mockito.when(installment.getInstallmentNumber()).thenReturn(1);
        Mockito.when(charge.updatePaidAmountBy(refEq(chargeAmountMoney), eq(1), refEq(zero))).thenReturn(chargeAmountMoney);
        Mockito.when(loanTransaction.isPenaltyPayment()).thenReturn(false);

        underTest.processLatestTransaction(loanTransaction, currency, List.of(installment), Set.of(charge), overpaidAmount);

        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), eq(chargeAmountMoney));
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(chargeAmountMoney), refEq(zero));
        assertEquals(zero.getAmount(), loanTransaction.getAmount(currency).minus(chargeAmountMoney).getAmount());
        assertEquals(0, chargeAmount.compareTo(installment.getFeeChargesCharged(currency).getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(installment.getFeeChargesOutstanding(currency).getAmount()));
        Mockito.verify(loan, Mockito.times(0)).getPaymentAllocationRules();
    }

    @Test
    public void chargePaymentTransactionTestWithLessTransactionAmount() {
        BigDecimal chargeAmount = BigDecimal.valueOf(100.00);
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        LocalDate transactionDate = LocalDate.of(2023, 1, 5);
        LoanTransaction loanTransaction = Mockito.mock(LoanTransaction.class);
        MonetaryCurrency currency = MONETARY_CURRENCY;
        LoanCharge charge = Mockito.mock(LoanCharge.class);
        LoanChargePaidBy chargePaidBy = Mockito.mock(LoanChargePaidBy.class);
        Money overpaidAmount = Money.zero(currency);
        Money zero = Money.zero(currency);
        Loan loan = Mockito.mock(Loan.class);
        Money chargeAmountMoney = Money.of(currency, chargeAmount);
        BigDecimal transactionAmount = BigDecimal.valueOf(20.00);
        Money transactionAmountMoney = Money.of(currency, transactionAmount);
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, disbursementDate, disbursementDate.plusMonths(1), BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), chargeAmount, BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));

        Mockito.when(loanTransaction.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_PAYMENT);
        Mockito.when(chargePaidBy.getLoanCharge()).thenReturn(charge);
        Mockito.when(loanTransaction.getLoanChargesPaid()).thenReturn(Set.of(chargePaidBy));
        Mockito.when(loanTransaction.getAmount()).thenReturn(transactionAmount);
        Mockito.when(loanTransaction.getAmount(currency)).thenReturn(transactionAmountMoney);
        Mockito.when(loanTransaction.getTransactionDate()).thenReturn(transactionDate);
        Mockito.when(charge.getAmountOutstanding(currency)).thenReturn(chargeAmountMoney);
        Mockito.when(loanTransaction.getLoan()).thenReturn(loan);
        Mockito.when(loan.getDisbursementDate()).thenReturn(disbursementDate);
        Mockito.when(charge.isDueForCollectionFromIncludingAndUpToAndIncluding(disbursementDate, installment.getDueDate()))
                .thenReturn(true);
        Mockito.when(installment.getInstallmentNumber()).thenReturn(1);
        Mockito.when(charge.updatePaidAmountBy(refEq(transactionAmountMoney), eq(1), refEq(zero))).thenReturn(transactionAmountMoney);
        Mockito.when(loanTransaction.isPenaltyPayment()).thenReturn(false);

        underTest.processLatestTransaction(loanTransaction, currency, List.of(installment), Set.of(charge), overpaidAmount);

        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), eq(transactionAmountMoney));
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(transactionAmountMoney),
                refEq(zero));
        assertEquals(zero.getAmount(), loanTransaction.getAmount(currency).minus(transactionAmountMoney).getAmount());
        assertEquals(0, chargeAmount.compareTo(installment.getFeeChargesCharged(currency).getAmount()));
        assertEquals(0, BigDecimal.valueOf(80.00).compareTo(installment.getFeeChargesOutstanding(currency).getAmount()));
        Mockito.verify(loan, Mockito.times(0)).getPaymentAllocationRules();
    }

    @Test
    public void chargePaymentTransactionTestWithMoreTransactionAmount() {
        BigDecimal chargeAmount = BigDecimal.valueOf(100.00);
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        LocalDate transactionDate = disbursementDate.plusMonths(1);
        LoanTransaction loanTransaction = Mockito.mock(LoanTransaction.class);
        MonetaryCurrency currency = MONETARY_CURRENCY;
        LoanCharge charge = Mockito.mock(LoanCharge.class);
        LoanChargePaidBy chargePaidBy = Mockito.mock(LoanChargePaidBy.class);
        Money overpaidAmount = Money.zero(currency);
        Money zero = Money.zero(currency);
        Loan loan = Mockito.mock(Loan.class);
        LoanProductRelatedDetail loanProductRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);
        Money chargeAmountMoney = Money.of(currency, chargeAmount);
        BigDecimal transactionAmount = BigDecimal.valueOf(120.00);
        Money transactionAmountMoney = Money.of(currency, transactionAmount);
        LoanPaymentAllocationRule loanPaymentAllocationRule = Mockito.mock(LoanPaymentAllocationRule.class);
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, disbursementDate, transactionDate, BigDecimal.valueOf(100L),
                        BigDecimal.valueOf(0L), chargeAmount, BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));

        Mockito.when(loanTransaction.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_PAYMENT);
        Mockito.when(chargePaidBy.getLoanCharge()).thenReturn(charge);
        Mockito.when(loanTransaction.getLoanChargesPaid()).thenReturn(Set.of(chargePaidBy));
        Mockito.when(loanTransaction.getAmount()).thenReturn(transactionAmount);
        Mockito.when(loanTransaction.getAmount(currency)).thenReturn(transactionAmountMoney);
        Mockito.when(loanTransaction.getTransactionDate()).thenReturn(transactionDate);
        Mockito.when(charge.getAmountOutstanding(currency)).thenReturn(chargeAmountMoney);
        Mockito.when(loanTransaction.getLoan()).thenReturn(loan);
        Mockito.when(loanTransaction.getLoan().getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        Mockito.when(loanProductRelatedDetail.getLoanScheduleProcessingType()).thenReturn(LoanScheduleProcessingType.HORIZONTAL);
        Mockito.when(loan.getDisbursementDate()).thenReturn(disbursementDate);
        Mockito.when(charge.isDueForCollectionFromIncludingAndUpToAndIncluding(disbursementDate, installment.getDueDate()))
                .thenReturn(true);
        Mockito.when(installment.getInstallmentNumber()).thenReturn(1);
        Mockito.when(charge.updatePaidAmountBy(refEq(chargeAmountMoney), eq(1), refEq(zero))).thenReturn(chargeAmountMoney);
        Mockito.when(loanTransaction.isPenaltyPayment()).thenReturn(false);
        Mockito.when(loan.getPaymentAllocationRules()).thenReturn(List.of(loanPaymentAllocationRule));
        Mockito.when(loanPaymentAllocationRule.getTransactionType()).thenReturn(PaymentAllocationTransactionType.DEFAULT);
        Mockito.when(loanPaymentAllocationRule.getAllocationTypes()).thenReturn(List.of(PaymentAllocationType.DUE_PRINCIPAL));
        Mockito.when(loanTransaction.isOn(eq(transactionDate))).thenReturn(true);

        underTest.processLatestTransaction(loanTransaction, currency, List.of(installment), Set.of(charge), overpaidAmount);

        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), eq(chargeAmountMoney));
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(chargeAmountMoney), refEq(zero));
        assertEquals(0, BigDecimal.valueOf(20).compareTo(loanTransaction.getAmount(currency).minus(chargeAmountMoney).getAmount()));
        assertEquals(0, chargeAmount.compareTo(installment.getFeeChargesCharged(currency).getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(installment.getFeeChargesOutstanding(currency).getAmount()));
        assertEquals(0, BigDecimal.valueOf(80).compareTo(installment.getPrincipalOutstanding(currency).getAmount()));
        Mockito.verify(loan, Mockito.times(1)).getPaymentAllocationRules();
    }
}
