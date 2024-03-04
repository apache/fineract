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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REPAYMENT;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.FEE;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.INTEREST;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PENALTY;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PRINCIPAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCreditAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdvancedPaymentScheduleTransactionProcessorTest {

    private final LocalDate transactionDate = LocalDate.of(2023, 7, 11);
    private static final MonetaryCurrency MONETARY_CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);
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

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void chargePaymentTransactionTestWithExactAmount() {
        final BigDecimal chargeAmount = BigDecimal.valueOf(100);
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        LocalDate transactionDate = LocalDate.of(2023, 1, 5);
        LoanTransaction loanTransaction = mock(LoanTransaction.class);
        MonetaryCurrency currency = MONETARY_CURRENCY;
        LoanCharge charge = mock(LoanCharge.class);
        LoanChargePaidBy chargePaidBy = mock(LoanChargePaidBy.class);
        Money overpaidAmount = Money.zero(currency);
        Money zero = Money.zero(currency);
        Loan loan = mock(Loan.class);
        Money chargeAmountMoney = Money.of(currency, chargeAmount);
        LoanRepaymentScheduleInstallment installment = spy(
                new LoanRepaymentScheduleInstallment(loan, 1, disbursementDate, disbursementDate.plusMonths(1), BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), chargeAmount, BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));

        when(loanTransaction.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_PAYMENT);
        when(chargePaidBy.getLoanCharge()).thenReturn(charge);
        when(loanTransaction.getLoanChargesPaid()).thenReturn(Set.of(chargePaidBy));
        when(loanTransaction.getAmount()).thenReturn(chargeAmount);
        when(loanTransaction.getAmount(currency)).thenReturn(chargeAmountMoney);
        when(loanTransaction.getTransactionDate()).thenReturn(transactionDate);
        when(charge.getAmountOutstanding(currency)).thenReturn(chargeAmountMoney);
        when(loanTransaction.getLoan()).thenReturn(loan);
        when(loan.getDisbursementDate()).thenReturn(disbursementDate);
        when(charge.isDueForCollectionFromIncludingAndUpToAndIncluding(disbursementDate, installment.getDueDate())).thenReturn(true);
        when(installment.getInstallmentNumber()).thenReturn(1);
        when(charge.updatePaidAmountBy(refEq(chargeAmountMoney), eq(1), refEq(zero))).thenReturn(chargeAmountMoney);
        when(loanTransaction.isPenaltyPayment()).thenReturn(false);

        underTest.processLatestTransaction(loanTransaction,
                new TransactionCtx(currency, List.of(installment), Set.of(charge), new MoneyHolder(overpaidAmount)));

        Mockito.verify(installment, times(1)).payFeeChargesComponent(eq(transactionDate), eq(chargeAmountMoney));
        Mockito.verify(loanTransaction, times(1)).updateComponents(refEq(zero), refEq(zero), refEq(chargeAmountMoney), refEq(zero));
        assertEquals(zero.getAmount(), loanTransaction.getAmount(currency).minus(chargeAmountMoney).getAmount());
        assertEquals(0, chargeAmount.compareTo(installment.getFeeChargesCharged(currency).getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(installment.getFeeChargesOutstanding(currency).getAmount()));
        Mockito.verify(loan, times(0)).getPaymentAllocationRules();
    }

    @Test
    public void chargePaymentTransactionTestWithLessTransactionAmount() {
        BigDecimal chargeAmount = BigDecimal.valueOf(100.00);
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        LocalDate transactionDate = LocalDate.of(2023, 1, 5);
        LoanTransaction loanTransaction = mock(LoanTransaction.class);
        MonetaryCurrency currency = MONETARY_CURRENCY;
        LoanCharge charge = mock(LoanCharge.class);
        LoanChargePaidBy chargePaidBy = mock(LoanChargePaidBy.class);
        Money overpaidAmount = Money.zero(currency);
        Money zero = Money.zero(currency);
        Loan loan = mock(Loan.class);
        Money chargeAmountMoney = Money.of(currency, chargeAmount);
        BigDecimal transactionAmount = BigDecimal.valueOf(20.00);
        Money transactionAmountMoney = Money.of(currency, transactionAmount);
        LoanRepaymentScheduleInstallment installment = spy(
                new LoanRepaymentScheduleInstallment(loan, 1, disbursementDate, disbursementDate.plusMonths(1), BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), chargeAmount, BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));

        when(loanTransaction.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_PAYMENT);
        when(chargePaidBy.getLoanCharge()).thenReturn(charge);
        when(loanTransaction.getLoanChargesPaid()).thenReturn(Set.of(chargePaidBy));
        when(loanTransaction.getAmount()).thenReturn(transactionAmount);
        when(loanTransaction.getAmount(currency)).thenReturn(transactionAmountMoney);
        when(loanTransaction.getTransactionDate()).thenReturn(transactionDate);
        when(charge.getAmountOutstanding(currency)).thenReturn(chargeAmountMoney);
        when(loanTransaction.getLoan()).thenReturn(loan);
        when(loan.getDisbursementDate()).thenReturn(disbursementDate);
        when(charge.isDueForCollectionFromIncludingAndUpToAndIncluding(disbursementDate, installment.getDueDate())).thenReturn(true);
        when(installment.getInstallmentNumber()).thenReturn(1);
        when(charge.updatePaidAmountBy(refEq(transactionAmountMoney), eq(1), refEq(zero))).thenReturn(transactionAmountMoney);
        when(loanTransaction.isPenaltyPayment()).thenReturn(false);

        underTest.processLatestTransaction(loanTransaction,
                new TransactionCtx(currency, List.of(installment), Set.of(charge), new MoneyHolder(overpaidAmount)));

        Mockito.verify(installment, times(1)).payFeeChargesComponent(eq(transactionDate), eq(transactionAmountMoney));
        Mockito.verify(loanTransaction, times(1)).updateComponents(refEq(zero), refEq(zero), refEq(transactionAmountMoney), refEq(zero));
        assertEquals(zero.getAmount(), loanTransaction.getAmount(currency).minus(transactionAmountMoney).getAmount());
        assertEquals(0, chargeAmount.compareTo(installment.getFeeChargesCharged(currency).getAmount()));
        assertEquals(0, BigDecimal.valueOf(80.00).compareTo(installment.getFeeChargesOutstanding(currency).getAmount()));
        Mockito.verify(loan, times(0)).getPaymentAllocationRules();
    }

    @Test
    public void chargePaymentTransactionTestWithMoreTransactionAmount() {
        BigDecimal chargeAmount = BigDecimal.valueOf(100.00);
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        LocalDate transactionDate = disbursementDate.plusMonths(1);
        LoanTransaction loanTransaction = mock(LoanTransaction.class);
        MonetaryCurrency currency = MONETARY_CURRENCY;
        LoanCharge charge = mock(LoanCharge.class);
        LoanChargePaidBy chargePaidBy = mock(LoanChargePaidBy.class);
        Money overpaidAmount = Money.zero(currency);
        Money zero = Money.zero(currency);
        Loan loan = mock(Loan.class);
        LoanProductRelatedDetail loanProductRelatedDetail = mock(LoanProductRelatedDetail.class);
        Money chargeAmountMoney = Money.of(currency, chargeAmount);
        BigDecimal transactionAmount = BigDecimal.valueOf(120.00);
        Money transactionAmountMoney = Money.of(currency, transactionAmount);
        LoanPaymentAllocationRule loanPaymentAllocationRule = mock(LoanPaymentAllocationRule.class);
        LoanRepaymentScheduleInstallment installment = spy(new LoanRepaymentScheduleInstallment(loan, 1, disbursementDate, transactionDate,
                BigDecimal.valueOf(100L), BigDecimal.valueOf(0L), chargeAmount, BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));

        when(loanTransaction.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_PAYMENT);
        when(chargePaidBy.getLoanCharge()).thenReturn(charge);
        when(loanTransaction.getLoanChargesPaid()).thenReturn(Set.of(chargePaidBy));
        when(loanTransaction.getAmount()).thenReturn(transactionAmount);
        when(loanTransaction.getAmount(currency)).thenReturn(transactionAmountMoney);
        when(loanTransaction.getTransactionDate()).thenReturn(transactionDate);
        when(charge.getAmountOutstanding(currency)).thenReturn(chargeAmountMoney);
        when(loanTransaction.getLoan()).thenReturn(loan);
        when(loanTransaction.getLoan().getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loanProductRelatedDetail.getLoanScheduleProcessingType()).thenReturn(LoanScheduleProcessingType.HORIZONTAL);
        when(loan.getDisbursementDate()).thenReturn(disbursementDate);
        when(charge.isDueForCollectionFromIncludingAndUpToAndIncluding(disbursementDate, installment.getDueDate())).thenReturn(true);
        when(installment.getInstallmentNumber()).thenReturn(1);
        when(charge.updatePaidAmountBy(refEq(chargeAmountMoney), eq(1), refEq(zero))).thenReturn(chargeAmountMoney);
        when(loanTransaction.isPenaltyPayment()).thenReturn(false);
        when(loan.getPaymentAllocationRules()).thenReturn(List.of(loanPaymentAllocationRule));
        when(loanPaymentAllocationRule.getTransactionType()).thenReturn(PaymentAllocationTransactionType.DEFAULT);
        when(loanPaymentAllocationRule.getAllocationTypes()).thenReturn(List.of(PaymentAllocationType.DUE_PRINCIPAL));
        when(loanTransaction.isOn(eq(transactionDate))).thenReturn(true);

        underTest.processLatestTransaction(loanTransaction,
                new TransactionCtx(currency, List.of(installment), Set.of(charge), new MoneyHolder(overpaidAmount)));

        Mockito.verify(installment, times(1)).payFeeChargesComponent(eq(transactionDate), eq(chargeAmountMoney));
        Mockito.verify(loanTransaction, times(1)).updateComponents(refEq(zero), refEq(zero), refEq(chargeAmountMoney), refEq(zero));
        assertEquals(0, BigDecimal.valueOf(20).compareTo(loanTransaction.getAmount(currency).minus(chargeAmountMoney).getAmount()));
        assertEquals(0, chargeAmount.compareTo(installment.getFeeChargesCharged(currency).getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(installment.getFeeChargesOutstanding(currency).getAmount()));
        assertEquals(0, BigDecimal.valueOf(80).compareTo(installment.getPrincipalOutstanding(currency).getAmount()));
        Mockito.verify(loan, times(1)).getPaymentAllocationRules();
    }

    @Test
    public void testProcessCreditTransactionWithAllocationRulePrincipalPenaltyFeeInterest() {
        // given
        Loan loan = mock(Loan.class);
        LoanTransaction chargeBackTransaction = createChargebackTransaction(loan, 25.0);

        LoanCreditAllocationRule mockCreditAllocationRule = createMockCreditAllocationRule(PRINCIPAL, PENALTY, FEE, INTEREST);
        when(loan.getCreditAllocationRules()).thenReturn(List.of(mockCreditAllocationRule));
        LoanTransaction repayment = createRepayment(loan, chargeBackTransaction, 10, 0, 20, 5);
        lenient().when(loan.getLoanTransactions()).thenReturn(List.of(repayment));

        MoneyHolder overpaymentHolder = new MoneyHolder(Money.zero(MONETARY_CURRENCY));
        List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        LoanRepaymentScheduleInstallment installment = createMockInstallment(LocalDate.of(2023, 1, 31), false);
        installments.add(installment);

        // when
        TransactionCtx ctx = new TransactionCtx(MONETARY_CURRENCY, installments, null, overpaymentHolder);
        underTest.processCreditTransaction(chargeBackTransaction, ctx);

        // verify principal
        Mockito.verify(installment, times(1)).addToCreditedPrincipal(new BigDecimal("10.00"));
        ArgumentCaptor<LocalDate> localDateArgumentCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(installment, times(1)).addToPrincipal(localDateArgumentCaptor.capture(), moneyCaptor.capture());
        Assertions.assertEquals(LocalDate.of(2023, 1, 1), localDateArgumentCaptor.getValue());
        assertEquals(0, moneyCaptor.getValue().getAmount().compareTo(BigDecimal.valueOf(10.0)));

        // verify charges on installment
        Mockito.verify(installment, times(1)).addToCreditedFee(new BigDecimal("10.00"));
        Mockito.verify(installment, times(1)).addToCreditedPenalty(new BigDecimal("5.00"));

        ArgumentCaptor<Money> feeCaptor = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> penaltyCaptor = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(installment, times(2)).addToChargePortion(feeCaptor.capture(), any(), any(), penaltyCaptor.capture(), any(), any());
        assertEquals(2, feeCaptor.getAllValues().size());
        assertEquals(2, penaltyCaptor.getAllValues().size());

        assertEquals(0, feeCaptor.getAllValues().get(0).getAmount().compareTo(BigDecimal.valueOf(10.0)));
        assertEquals(0, feeCaptor.getAllValues().get(1).getAmount().compareTo(BigDecimal.ZERO));

        assertEquals(0, penaltyCaptor.getAllValues().get(0).getAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, penaltyCaptor.getAllValues().get(1).getAmount().compareTo(BigDecimal.valueOf(5.0)));

        // verify transaction
        ArgumentCaptor<Money> principal = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> interest = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> fee = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> penalty = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(chargeBackTransaction, times(1)).updateComponents(principal.capture(), interest.capture(), fee.capture(),
                penalty.capture());
        assertEquals(0, principal.getValue().getAmount().compareTo(BigDecimal.valueOf(10.0)));
        assertEquals(0, fee.getValue().getAmount().compareTo(BigDecimal.valueOf(10.0)));
        assertEquals(0, penalty.getValue().getAmount().compareTo(BigDecimal.valueOf(5.0)));
        assertEquals(0, interest.getValue().getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testProcessCreditTransactionWithAllocationRulePenaltyFeePrincipalInterest() {
        // given
        Loan loan = mock(Loan.class);
        LoanTransaction chargeBackTransaction = createChargebackTransaction(loan, 25.0);

        LoanCreditAllocationRule mockCreditAllocationRule = createMockCreditAllocationRule(PENALTY, FEE, PRINCIPAL, INTEREST);
        when(loan.getCreditAllocationRules()).thenReturn(List.of(mockCreditAllocationRule));
        LoanTransaction repayment = createRepayment(loan, chargeBackTransaction, 10, 0, 20, 5);
        lenient().when(loan.getLoanTransactions()).thenReturn(List.of(repayment));

        MoneyHolder overpaymentHolder = new MoneyHolder(Money.zero(MONETARY_CURRENCY));
        List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        LoanRepaymentScheduleInstallment installment = createMockInstallment(LocalDate.of(2023, 1, 31), false);
        installments.add(installment);

        // when
        TransactionCtx ctx = new TransactionCtx(MONETARY_CURRENCY, installments, null, overpaymentHolder);
        underTest.processCreditTransaction(chargeBackTransaction, ctx);

        // verify charges on installment
        Mockito.verify(installment, times(1)).addToCreditedFee(new BigDecimal("20.00"));
        Mockito.verify(installment, times(1)).addToCreditedPenalty(new BigDecimal("5.00"));

        ArgumentCaptor<Money> feeCaptor = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> penaltyCaptor = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(installment, times(2)).addToChargePortion(feeCaptor.capture(), any(), any(), penaltyCaptor.capture(), any(), any());
        assertEquals(2, feeCaptor.getAllValues().size());
        assertEquals(2, penaltyCaptor.getAllValues().size());

        assertEquals(0, feeCaptor.getAllValues().get(0).getAmount().compareTo(BigDecimal.valueOf(20.0)));
        assertEquals(0, feeCaptor.getAllValues().get(1).getAmount().compareTo(BigDecimal.ZERO));

        assertEquals(0, penaltyCaptor.getAllValues().get(0).getAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, penaltyCaptor.getAllValues().get(1).getAmount().compareTo(BigDecimal.valueOf(5.0)));

        // verify transaction
        ArgumentCaptor<Money> principal = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> interest = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> fee = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> penalty = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(chargeBackTransaction, times(1)).updateComponents(principal.capture(), interest.capture(), fee.capture(),
                penalty.capture());
        assertEquals(0, principal.getValue().getAmount().compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, interest.getValue().getAmount().compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, fee.getValue().getAmount().compareTo(BigDecimal.valueOf(20.0)));
        assertEquals(0, penalty.getValue().getAmount().compareTo(BigDecimal.valueOf(5.0)));
    }

    @Test
    public void testProcessCreditTransactionWithAllocationRulePrincipalAndInterestWithAdditionalInstallment() {
        // given
        Loan loan = mock(Loan.class);
        LoanTransaction chargeBackTransaction = createChargebackTransaction(loan, 25.0);

        LoanCreditAllocationRule mockCreditAllocationRule = createMockCreditAllocationRule(PRINCIPAL, PENALTY, FEE, INTEREST);
        when(loan.getCreditAllocationRules()).thenReturn(List.of(mockCreditAllocationRule));
        LoanTransaction repayment = createRepayment(loan, chargeBackTransaction, 10, 0, 20, 5);
        lenient().when(loan.getLoanTransactions()).thenReturn(List.of(repayment));

        MoneyHolder overpaymentHolder = new MoneyHolder(Money.zero(MONETARY_CURRENCY));
        List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();

        LoanRepaymentScheduleInstallment installment1 = createMockInstallment(LocalDate.of(2022, 12, 20), false);
        LoanRepaymentScheduleInstallment installment2 = createMockInstallment(LocalDate.of(2022, 12, 27), true);
        installments.add(installment1);
        installments.add(installment2);

        // when
        TransactionCtx ctx = new TransactionCtx(MONETARY_CURRENCY, installments, null, overpaymentHolder);
        underTest.processCreditTransaction(chargeBackTransaction, ctx);

        // verify principal
        Mockito.verify(installment2, times(1)).addToCreditedPrincipal(new BigDecimal("10.00"));
        ArgumentCaptor<LocalDate> localDateArgumentCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(installment2, times(1)).addToPrincipal(localDateArgumentCaptor.capture(), moneyCaptor.capture());
        Assertions.assertEquals(LocalDate.of(2023, 1, 1), localDateArgumentCaptor.getValue());
        assertEquals(0, moneyCaptor.getValue().getAmount().compareTo(BigDecimal.valueOf(10.0)));

        // verify charges on installment
        Mockito.verify(installment2, times(1)).addToCreditedFee(new BigDecimal("10.00"));
        Mockito.verify(installment2, times(1)).addToCreditedPenalty(new BigDecimal("5.00"));

        ArgumentCaptor<Money> feeCaptor = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> penaltyCaptor = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(installment2, times(2)).addToChargePortion(feeCaptor.capture(), any(), any(), penaltyCaptor.capture(), any(), any());
        assertEquals(2, feeCaptor.getAllValues().size());
        assertEquals(2, penaltyCaptor.getAllValues().size());

        assertEquals(0, feeCaptor.getAllValues().get(0).getAmount().compareTo(BigDecimal.valueOf(10.0)));
        assertEquals(0, feeCaptor.getAllValues().get(1).getAmount().compareTo(BigDecimal.ZERO));

        assertEquals(0, penaltyCaptor.getAllValues().get(0).getAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, penaltyCaptor.getAllValues().get(1).getAmount().compareTo(BigDecimal.valueOf(5.0)));

        // verify transaction
        ArgumentCaptor<Money> principal = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> interest = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> fee = ArgumentCaptor.forClass(Money.class);
        ArgumentCaptor<Money> penalty = ArgumentCaptor.forClass(Money.class);
        Mockito.verify(chargeBackTransaction, times(1)).updateComponents(principal.capture(), interest.capture(), fee.capture(),
                penalty.capture());
        assertEquals(0, principal.getValue().getAmount().compareTo(BigDecimal.valueOf(10.0)));
        assertEquals(0, fee.getValue().getAmount().compareTo(BigDecimal.valueOf(10.0)));
        assertEquals(0, penalty.getValue().getAmount().compareTo(BigDecimal.valueOf(5.0)));
        assertEquals(0, interest.getValue().getAmount().compareTo(BigDecimal.ZERO));
    }

    private LoanRepaymentScheduleInstallment createMockInstallment(LocalDate localDate, boolean isAdditional) {
        LoanRepaymentScheduleInstallment installment = mock(LoanRepaymentScheduleInstallment.class);
        lenient().when(installment.isAdditional()).thenReturn(isAdditional);
        lenient().when(installment.getDueDate()).thenReturn(localDate);
        Money interestCharged = Money.of(MONETARY_CURRENCY, BigDecimal.ZERO);
        lenient().when(installment.getInterestCharged(MONETARY_CURRENCY)).thenReturn(interestCharged);
        return installment;
    }

    @NotNull
    private LoanCreditAllocationRule createMockCreditAllocationRule(AllocationType... allocationTypes) {
        LoanCreditAllocationRule mockCreditAllocationRule = mock(LoanCreditAllocationRule.class);
        lenient().when(mockCreditAllocationRule.getTransactionType()).thenReturn(CreditAllocationTransactionType.CHARGEBACK);
        lenient().when(mockCreditAllocationRule.getAllocationTypes()).thenReturn(Arrays.asList(allocationTypes));
        return mockCreditAllocationRule;
    }

    private LoanTransaction createRepayment(Loan loan, LoanTransaction toTransaction, double principalPortion, double interestPortion,
            double feePortion, double penaltyPortion) {
        LoanTransaction repayment = mock(LoanTransaction.class);
        lenient().when(repayment.getLoan()).thenReturn(loan);
        lenient().when(repayment.isRepayment()).thenReturn(true);
        lenient().when(repayment.getTypeOf()).thenReturn(REPAYMENT);
        lenient().when(repayment.getPrincipalPortion()).thenReturn(BigDecimal.valueOf(principalPortion));
        lenient().when(repayment.getInterestPortion()).thenReturn(BigDecimal.valueOf(interestPortion));
        lenient().when(repayment.getFeeChargesPortion()).thenReturn(BigDecimal.valueOf(feePortion));
        lenient().when(repayment.getPenaltyChargesPortion()).thenReturn(BigDecimal.valueOf(penaltyPortion));

        LoanTransactionRelation relation = mock(LoanTransactionRelation.class);
        lenient().when(relation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.CHARGEBACK);
        lenient().when(relation.getToTransaction()).thenReturn(toTransaction);

        lenient().when(repayment.getLoanTransactionRelations()).thenReturn(Set.of(relation));
        return repayment;
    }

    private LoanTransaction createChargebackTransaction(Loan loan, double transactionAmount) {
        LoanTransaction chargeback = mock(LoanTransaction.class);
        lenient().when(chargeback.isChargeback()).thenReturn(true);
        lenient().when(chargeback.getTypeOf()).thenReturn(LoanTransactionType.CHARGEBACK);
        lenient().when(chargeback.getLoan()).thenReturn(loan);
        lenient().when(chargeback.getAmount()).thenReturn(BigDecimal.valueOf(transactionAmount));
        Money money = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(transactionAmount));
        lenient().when(chargeback.getAmount(MONETARY_CURRENCY)).thenReturn(money);
        lenient().when(chargeback.getTransactionDate()).thenReturn(LocalDate.of(2023, 1, 1));
        return chargeback;
    }

    @Test
    public void calculateChargebackAllocationMap() {
        Map<AllocationType, Money> result;
        MonetaryCurrency currency = mock(MonetaryCurrency.class);

        result = underTest.calculateChargebackAllocationMap(allocationMap(50.0, 100.0, 200.0, 12.0, currency), BigDecimal.valueOf(50.0),
                List.of(PRINCIPAL, INTEREST, FEE, PENALTY), currency);
        verify(allocationMap(50.0, 0, 0, 0, currency), result);

        result = underTest.calculateChargebackAllocationMap(allocationMap(40.0, 100.0, 200.0, 12.0, currency), BigDecimal.valueOf(50.0),
                List.of(PRINCIPAL, INTEREST, FEE, PENALTY), currency);
        verify(allocationMap(40.0, 10, 0, 0, currency), result);

        result = underTest.calculateChargebackAllocationMap(allocationMap(40.0, 100.0, 200.0, 12.0, currency), BigDecimal.valueOf(50.0),
                List.of(PRINCIPAL, FEE, PENALTY, INTEREST), currency);
        verify(allocationMap(40.0, 0, 10, 0, currency), result);

        result = underTest.calculateChargebackAllocationMap(allocationMap(40.0, 100.0, 200.0, 12.0, currency), BigDecimal.valueOf(340.0),
                List.of(PRINCIPAL, FEE, PENALTY, INTEREST), currency);
        verify(allocationMap(40.0, 88.0, 200.0, 12.0, currency), result);

        result = underTest.calculateChargebackAllocationMap(allocationMap(40.0, 100.0, 200.0, 12.0, currency), BigDecimal.valueOf(352.0),
                List.of(PRINCIPAL, FEE, PENALTY, INTEREST), currency);
        verify(allocationMap(40.0, 100.0, 200.0, 12.0, currency), result);
    }

    private void verify(Map<AllocationType, Money> expected, Map<AllocationType, Money> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        expected.forEach((k, v) -> {
            Assertions.assertEquals(0, v.getAmount().compareTo(actual.get(k).getAmount()), "Not matching for " + k);
        });
    }

    private Map<AllocationType, Money> allocationMap(double principal, double interest, double fee, double penalty,
            MonetaryCurrency currency) {
        Map<AllocationType, Money> allocationMap = new HashMap<>();
        allocationMap.put(AllocationType.PRINCIPAL, Money.of(currency, BigDecimal.valueOf(principal)));
        allocationMap.put(AllocationType.INTEREST, Money.of(currency, BigDecimal.valueOf(interest)));
        allocationMap.put(AllocationType.FEE, Money.of(currency, BigDecimal.valueOf(fee)));
        allocationMap.put(AllocationType.PENALTY, Money.of(currency, BigDecimal.valueOf(penalty)));
        return allocationMap;
    }

    @Test
    public void testFindOriginalTransactionShouldFindOriginalInLoansTransactionWhenIdProvided() {
        // given
        LoanTransaction chargebackTransaction = mock(LoanTransaction.class);
        when(chargebackTransaction.getId()).thenReturn(123L);
        Loan loan = mock(Loan.class);
        when(chargebackTransaction.getLoan()).thenReturn(loan);
        LoanTransaction repayment1 = mock(LoanTransaction.class);
        LoanTransaction repayment2 = mock(LoanTransaction.class);
        when(loan.getLoanTransactions()).thenReturn(List.of(chargebackTransaction, repayment1, repayment2));

        LoanTransactionRelation relation = mock(LoanTransactionRelation.class);
        when(relation.getToTransaction()).thenReturn(chargebackTransaction);
        when(relation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.CHARGEBACK);
        when(repayment2.getLoanTransactionRelations()).thenReturn(Set.of(relation));
        TransactionCtx ctx = mock(TransactionCtx.class);

        // when
        LoanTransaction originalTransaction = underTest.findOriginalTransaction(chargebackTransaction, ctx);

        // then
        Assertions.assertEquals(originalTransaction, repayment2);
    }

    @Test
    public void testFindOriginalTransactionThrowsRuntimeExceptionWhenIdProvidedAndRelationsAreMissing() {
        // given
        LoanTransaction chargebackTransaction = mock(LoanTransaction.class);
        when(chargebackTransaction.getId()).thenReturn(123L);
        Loan loan = mock(Loan.class);
        when(chargebackTransaction.getLoan()).thenReturn(loan);
        LoanTransaction repayment1 = mock(LoanTransaction.class);
        LoanTransaction repayment2 = mock(LoanTransaction.class);
        when(loan.getLoanTransactions()).thenReturn(List.of(chargebackTransaction, repayment1, repayment2));

        when(repayment2.getLoanTransactionRelations()).thenReturn(Set.of());

        TransactionCtx ctx = mock(TransactionCtx.class);

        // when + then
        RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class,
                () -> underTest.findOriginalTransaction(chargebackTransaction, ctx));
        Assertions.assertEquals("Chargeback transaction must have an original transaction", runtimeException.getMessage());
    }

    @Test
    public void testFindOriginalTransactionShouldFindOriginalInLoansTransactionFromTransactionCtxWhenIdIsNotProvided() {
        // given
        LoanTransaction chargebackReplayed = mock(LoanTransaction.class);
        when(chargebackReplayed.getId()).thenReturn(null);
        LoanTransaction repayment1 = mock(LoanTransaction.class);
        LoanTransaction repayment2 = mock(LoanTransaction.class);

        LoanTransaction originalChargeback = mock(LoanTransaction.class);
        when(originalChargeback.getId()).thenReturn(123L);
        LoanTransactionRelation relation = mock(LoanTransactionRelation.class);
        when(relation.getToTransaction()).thenReturn(originalChargeback);
        when(relation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.CHARGEBACK);
        when(repayment2.getLoanTransactionRelations()).thenReturn(Set.of(relation));

        TransactionCtx ctx = mock(TransactionCtx.class);
        ChangedTransactionDetail changedTransactionDetail = mock(ChangedTransactionDetail.class);
        when(ctx.getChangedTransactionDetail()).thenReturn(changedTransactionDetail);
        when(changedTransactionDetail.getCurrentTransactionToOldId()).thenReturn(Map.of(chargebackReplayed, 123L));
        when(changedTransactionDetail.getNewTransactionMappings()).thenReturn(Map.of(122L, repayment1, 121L, repayment2));

        // when
        LoanTransaction originalTransaction = underTest.findOriginalTransaction(chargebackReplayed, ctx);

        // then
        Assertions.assertEquals(originalTransaction, repayment2);
    }

    @Test
    public void testFindOriginalTransactionShouldFindOriginalInLoansTransactionFromTransactionCtxWhenIdIsNotProvidedFallbackToPersistedTransactions() {
        // given
        LoanTransaction chargebackReplayed = mock(LoanTransaction.class);
        when(chargebackReplayed.getId()).thenReturn(null);
        LoanTransaction repayment1 = mock(LoanTransaction.class);
        LoanTransaction repayment2 = mock(LoanTransaction.class);
        Loan loan = mock(Loan.class);
        when(chargebackReplayed.getLoan()).thenReturn(loan);
        when(loan.getLoanTransactions()).thenReturn(List.of(repayment1, repayment2));

        LoanTransaction originalChargeback = mock(LoanTransaction.class);
        when(originalChargeback.getId()).thenReturn(123L);
        LoanTransactionRelation relation = mock(LoanTransactionRelation.class);
        when(relation.getToTransaction()).thenReturn(originalChargeback);
        when(relation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.CHARGEBACK);
        when(repayment2.getLoanTransactionRelations()).thenReturn(Set.of(relation));

        TransactionCtx ctx = mock(TransactionCtx.class);
        ChangedTransactionDetail changedTransactionDetail = mock(ChangedTransactionDetail.class);
        when(ctx.getChangedTransactionDetail()).thenReturn(changedTransactionDetail);
        when(changedTransactionDetail.getCurrentTransactionToOldId()).thenReturn(Map.of(chargebackReplayed, 123L));
        when(changedTransactionDetail.getNewTransactionMappings()).thenReturn(Map.of());

        // when
        LoanTransaction originalTransaction = underTest.findOriginalTransaction(chargebackReplayed, ctx);

        // then
        Assertions.assertEquals(originalTransaction, repayment2);
    }

}
