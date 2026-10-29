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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RBILoanRepaymentScheduleTransactionProcessorTest {

    private static final MonetaryCurrency MONETARY_CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);

    private final LocalDate transactionDate = LocalDate.of(2023, 8, 15);
    private final LocalDate installment1DueDate = LocalDate.of(2023, 7, 31);
    private final LocalDate installment2DueDate = LocalDate.of(2023, 8, 31);

    private RBILoanRepaymentScheduleTransactionProcessor underTest;
    private List<LoanTransactionToRepaymentScheduleMapping> transactionMappings;

    @Mock
    private Set<LoanCharge> charges;
    @Mock
    private Office office;
    @Mock
    private Loan loan;

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @BeforeEach
    public void setUp() {
        underTest = new RBILoanRepaymentScheduleTransactionProcessor(mock(ExternalIdFactory.class), mock(LoanChargeValidator.class),
                mock(LoanBalanceService.class));
        transactionMappings = new ArrayList<>();
        lenient().when(loan.getCurrency()).thenReturn(MONETARY_CURRENCY);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, transactionDate)));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void lateRepaymentDoesNotCorruptSubsequentInstallmentMappingWithPreviousFees() {
        // Installment 1: Overdue on 2023-08-15
        final LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2023, 7, 1),
                installment1DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(15), BigDecimal.valueOf(5), false,
                null, BigDecimal.ZERO);

        // Installment 2: Future due date
        final LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment(loan, 2, installment1DueDate,
                installment2DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final List<LoanRepaymentScheduleInstallment> installments = List.of(installment1, installment2);

        // Late repayment of 150:
        // Installment 1: 5 penalty + 15 fee + 20 interest + 100 principal = 140
        // Remaining 10 goes to Installment 2 principal
        final Money amount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(150));
        final LoanTransaction lateRepayment = LoanTransaction.repayment(office, amount, null, transactionDate, ExternalId.empty());
        lateRepayment.resetDerivedComponents();

        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment1, installments, lateRepayment, amount, transactionMappings,
                charges);

        assertEquals(2, transactionMappings.size(), "Should have mappings for both installments");

        LoanTransactionToRepaymentScheduleMapping mapping1 = transactionMappings.get(0);
        assertEquals(installment1, mapping1.getLoanRepaymentScheduleInstallment());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(mapping1.getPrincipalPortion()));
        assertEquals(0, BigDecimal.valueOf(20).compareTo(mapping1.getInterestPortion()));
        assertEquals(0, BigDecimal.valueOf(15).compareTo(mapping1.getFeeChargesPortion()));
        assertEquals(0, BigDecimal.valueOf(5).compareTo(mapping1.getPenaltyChargesPortion()));

        LoanTransactionToRepaymentScheduleMapping mapping2 = transactionMappings.get(1);
        assertEquals(installment2, mapping2.getLoanRepaymentScheduleInstallment());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(mapping2.getPrincipalPortion()), "Installment 2 principal portion should be 10");
        assertEquals(true, mapping2.getFeeChargesPortion(MONETARY_CURRENCY).isZero(),
                "Installment 2 fee portion must be zero, but was: " + mapping2.getFeeChargesPortion());
        assertEquals(true, mapping2.getPenaltyChargesPortion(MONETARY_CURRENCY).isZero(),
                "Installment 2 penalty portion must be zero, but was: " + mapping2.getPenaltyChargesPortion());

        // Verify total mapped amount equals the transaction amount
        Money totalMapped = mapping1.getPrincipalPortion(MONETARY_CURRENCY).plus(mapping1.getInterestPortion(MONETARY_CURRENCY))
                .plus(mapping1.getFeeChargesPortion(MONETARY_CURRENCY)).plus(mapping1.getPenaltyChargesPortion(MONETARY_CURRENCY))
                .plus(mapping2.getPrincipalPortion(MONETARY_CURRENCY)).plus(mapping2.getInterestPortion(MONETARY_CURRENCY))
                .plus(mapping2.getFeeChargesPortion(MONETARY_CURRENCY)).plus(mapping2.getPenaltyChargesPortion(MONETARY_CURRENCY));
        assertEquals(0, amount.compareTo(totalMapped), "Sum of mapping portions must equal transaction amount");
    }

    @Test
    public void lateRepaymentSpanningMultipleSubsequentInstallmentsZeroesFeesOnAll() {
        final LocalDate installment3DueDate = LocalDate.of(2023, 9, 30);

        // Installment 1: Overdue, owes 100 principal + 20 interest + 15 fee + 5 penalty = 140
        final LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2023, 7, 1),
                installment1DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(15), BigDecimal.valueOf(5), false,
                null, BigDecimal.ZERO);

        // Installment 2: Future, owes 100 principal + 20 interest
        final LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment(loan, 2, installment1DueDate,
                installment2DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        // Installment 3: Future, owes 100 principal + 20 interest
        final LoanRepaymentScheduleInstallment installment3 = new LoanRepaymentScheduleInstallment(loan, 3, installment2DueDate,
                installment3DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final List<LoanRepaymentScheduleInstallment> installments = List.of(installment1, installment2, installment3);

        // Repayment of 260:
        // - 140 pays Installment 1 in full (5 penalty + 15 fee + 20 interest + 100 principal)
        // - 100 pays Installment 2 principal in full
        // - 20 pays Installment 3 principal partially
        final Money amount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(260));
        final LoanTransaction lateRepayment = LoanTransaction.repayment(office, amount, null, transactionDate, ExternalId.empty());
        lateRepayment.resetDerivedComponents();

        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment1, installments, lateRepayment, amount, transactionMappings,
                charges);

        assertEquals(3, transactionMappings.size(), "Should have mappings for all 3 installments");

        LoanTransactionToRepaymentScheduleMapping mapping2 = transactionMappings.get(1);
        assertEquals(installment2, mapping2.getLoanRepaymentScheduleInstallment());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(mapping2.getPrincipalPortion()));
        assertEquals(true, mapping2.getFeeChargesPortion(MONETARY_CURRENCY).isZero(), "Installment 2 fee must be zero");
        assertEquals(true, mapping2.getPenaltyChargesPortion(MONETARY_CURRENCY).isZero(), "Installment 2 penalty must be zero");

        LoanTransactionToRepaymentScheduleMapping mapping3 = transactionMappings.get(2);
        assertEquals(installment3, mapping3.getLoanRepaymentScheduleInstallment());
        assertEquals(0, BigDecimal.valueOf(20).compareTo(mapping3.getPrincipalPortion()));
        assertEquals(true, mapping3.getFeeChargesPortion(MONETARY_CURRENCY).isZero(), "Installment 3 fee must be zero");
        assertEquals(true, mapping3.getPenaltyChargesPortion(MONETARY_CURRENCY).isZero(), "Installment 3 penalty must be zero");

        Money totalMapped = transactionMappings.stream()
                .map(m -> m.getPrincipalPortion(MONETARY_CURRENCY).plus(m.getInterestPortion(MONETARY_CURRENCY))
                        .plus(m.getFeeChargesPortion(MONETARY_CURRENCY)).plus(m.getPenaltyChargesPortion(MONETARY_CURRENCY)))
                .reduce(Money.zero(MONETARY_CURRENCY), Money::plus);
        assertEquals(0, amount.compareTo(totalMapped), "Sum of all mapping amounts must match transaction amount exactly");
    }

    @Test
    public void lateRepaymentPayingExactOverdueAmountDoesNotCreateMappingForSubsequentInstallment() {
        // Installment 1: Overdue, owes 100 principal + 20 interest + 15 fee + 5 penalty = 140
        final LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2023, 7, 1),
                installment1DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(15), BigDecimal.valueOf(5), false,
                null, BigDecimal.ZERO);

        // Installment 2: Future, owes 100 principal + 20 interest
        final LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment(loan, 2, installment1DueDate,
                installment2DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final List<LoanRepaymentScheduleInstallment> installments = List.of(installment1, installment2);

        // Exact payment of 140 - nothing left for Installment 2
        final Money amount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(140));
        final LoanTransaction lateRepayment = LoanTransaction.repayment(office, amount, null, transactionDate, ExternalId.empty());
        lateRepayment.resetDerivedComponents();

        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment1, installments, lateRepayment, amount, transactionMappings,
                charges);

        assertEquals(1, transactionMappings.size(), "Only Installment 1 should have a mapping");
        assertEquals(installment1, transactionMappings.get(0).getLoanRepaymentScheduleInstallment());
    }

    @Test
    public void lateRepaymentWithOverpaymentReturnsRemainingAmountAndDoesNotCorruptMappings() {
        // Installment 1: Overdue, owes 100 principal + 20 interest + 15 fee + 5 penalty = 140
        final LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2023, 7, 1),
                installment1DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(15), BigDecimal.valueOf(5), false,
                null, BigDecimal.ZERO);

        // Installment 2: Future, owes 100 principal + 20 interest (interest not due yet)
        final LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment(loan, 2, installment1DueDate,
                installment2DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final List<LoanRepaymentScheduleInstallment> installments = List.of(installment1, installment2);

        // Overpayment of 300:
        // - Installment 1: 140 total (5 pen + 15 fee + 20 int + 100 principal)
        // - Installment 2: 100 principal
        // - Remaining: 60 overpayment
        final Money amount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(300));
        final LoanTransaction lateRepayment = LoanTransaction.repayment(office, amount, null, transactionDate, ExternalId.empty());
        lateRepayment.resetDerivedComponents();

        final Money remainingAmount = underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment1, installments,
                lateRepayment, amount, transactionMappings, charges);

        assertEquals(2, transactionMappings.size(), "Should have mappings for both installments");
        assertEquals(0, BigDecimal.valueOf(60).compareTo(remainingAmount.getAmount()), "Remaining overpayment should be 60");

        LoanTransactionToRepaymentScheduleMapping mapping2 = transactionMappings.get(1);
        assertEquals(installment2, mapping2.getLoanRepaymentScheduleInstallment());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(mapping2.getPrincipalPortion()), "Installment 2 principal should be fully paid");
        assertEquals(true, mapping2.getFeeChargesPortion(MONETARY_CURRENCY).isZero(), "Installment 2 fee must be zero");
        assertEquals(true, mapping2.getPenaltyChargesPortion(MONETARY_CURRENCY).isZero(), "Installment 2 penalty must be zero");

        Money totalMapped = transactionMappings.stream()
                .map(m -> m.getPrincipalPortion(MONETARY_CURRENCY).plus(m.getInterestPortion(MONETARY_CURRENCY))
                        .plus(m.getFeeChargesPortion(MONETARY_CURRENCY)).plus(m.getPenaltyChargesPortion(MONETARY_CURRENCY)))
                .reduce(Money.zero(MONETARY_CURRENCY), Money::plus);
        assertEquals(0, BigDecimal.valueOf(240).compareTo(totalMapped.getAmount()), "Total mapped must equal 240");
        assertEquals(0, amount.compareTo(totalMapped.plus(remainingAmount)), "Total mapped + overpayment must equal transaction amount");
    }

    @Test
    public void lateRepaymentCoveringOnlyPartialOverdueChargesDoesNotProcessSubsequentInstallments() {
        // Installment 1: Overdue, owes 100 principal + 20 interest + 15 fee + 5 penalty = 140
        final LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2023, 7, 1),
                installment1DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(15), BigDecimal.valueOf(5), false,
                null, BigDecimal.ZERO);

        // Installment 2: Future, owes 100 principal + 20 interest
        final LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment(loan, 2, installment1DueDate,
                installment2DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final List<LoanRepaymentScheduleInstallment> installments = List.of(installment1, installment2);

        // Small payment of 10: pays 5 penalty + 5 partial fee on Installment 1
        final Money amount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(10));
        final LoanTransaction lateRepayment = LoanTransaction.repayment(office, amount, null, transactionDate, ExternalId.empty());
        lateRepayment.resetDerivedComponents();

        final Money remainingAmount = underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment1, installments,
                lateRepayment, amount, transactionMappings, charges);

        assertEquals(0, BigDecimal.ZERO.compareTo(remainingAmount.getAmount()), "Remaining amount should be 0");
        assertEquals(1, transactionMappings.size(), "Only Installment 1 should have a mapping");

        LoanTransactionToRepaymentScheduleMapping mapping1 = transactionMappings.get(0);
        assertEquals(installment1, mapping1.getLoanRepaymentScheduleInstallment());
        assertEquals(true, mapping1.getPrincipalPortion(MONETARY_CURRENCY).isZero(), "Principal portion should be zero");
        assertEquals(true, mapping1.getInterestPortion(MONETARY_CURRENCY).isZero(), "Interest portion should be zero");
        assertEquals(0, BigDecimal.valueOf(5).compareTo(mapping1.getFeeChargesPortion()), "Fee portion should be 5");
        assertEquals(0, BigDecimal.valueOf(5).compareTo(mapping1.getPenaltyChargesPortion()), "Penalty portion should be 5");
    }

    @Test
    public void lateRepaymentWhenSubsequentInstallmentIsDueTodayPreservesItsChargesWithoutLeak() {
        final LocalDate todayDate = installment2DueDate;

        // Installment 1: Overdue, owes 100 principal + 20 interest + 15 fee + 5 penalty
        final LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2023, 7, 1),
                installment1DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(15), BigDecimal.valueOf(5), false,
                null, BigDecimal.ZERO);

        // Installment 2: Due today (transaction date), owes 100 principal + 25 interest + 10 fee + 0 penalty
        final LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment(loan, 2, installment1DueDate,
                installment2DueDate, BigDecimal.valueOf(100), BigDecimal.valueOf(25), BigDecimal.valueOf(10), BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final List<LoanRepaymentScheduleInstallment> installments = List.of(installment1, installment2);

        // Payment of 200 on installment 2 due date:
        // - Loop 1:
        // - Inst 1: 5 penalty + 15 fee + 20 interest = 40 (remaining: 160)
        // - Inst 2 (due today): 0 penalty + 10 fee + 25 interest = 35 (remaining: 125)
        // - Loop 2:
        // - Inst 1 principal: 100 (remaining: 25)
        // - Inst 2 principal: 25 (remaining: 0)
        final Money amount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(200));
        final LoanTransaction lateRepayment = LoanTransaction.repayment(office, amount, null, todayDate, ExternalId.empty());
        lateRepayment.resetDerivedComponents();

        final Money remainingAmount = underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment1, installments,
                lateRepayment, amount, transactionMappings, charges);

        assertEquals(0, BigDecimal.ZERO.compareTo(remainingAmount.getAmount()), "Remaining amount should be 0");
        assertEquals(2, transactionMappings.size(), "Should have mappings for both installments");

        LoanTransactionToRepaymentScheduleMapping mapping1 = transactionMappings.get(0);
        assertEquals(installment1, mapping1.getLoanRepaymentScheduleInstallment());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(mapping1.getPrincipalPortion()));
        assertEquals(0, BigDecimal.valueOf(20).compareTo(mapping1.getInterestPortion()));
        assertEquals(0, BigDecimal.valueOf(15).compareTo(mapping1.getFeeChargesPortion()));
        assertEquals(0, BigDecimal.valueOf(5).compareTo(mapping1.getPenaltyChargesPortion()));

        LoanTransactionToRepaymentScheduleMapping mapping2 = transactionMappings.get(1);
        assertEquals(installment2, mapping2.getLoanRepaymentScheduleInstallment());
        assertEquals(0, BigDecimal.valueOf(25).compareTo(mapping2.getPrincipalPortion()), "Installment 2 principal should be 25");
        assertEquals(0, BigDecimal.valueOf(25).compareTo(mapping2.getInterestPortion()), "Installment 2 interest should be 25");
        assertEquals(0, BigDecimal.valueOf(10).compareTo(mapping2.getFeeChargesPortion()),
                "Installment 2 fee should be exactly its own fee (10)");
        assertEquals(true, mapping2.getPenaltyChargesPortion(MONETARY_CURRENCY).isZero(),
                "Installment 2 penalty must be zero (not corrupted with installment 1 penalty)");

        Money totalMapped = transactionMappings.stream()
                .map(m -> m.getPrincipalPortion(MONETARY_CURRENCY).plus(m.getInterestPortion(MONETARY_CURRENCY))
                        .plus(m.getFeeChargesPortion(MONETARY_CURRENCY)).plus(m.getPenaltyChargesPortion(MONETARY_CURRENCY)))
                .reduce(Money.zero(MONETARY_CURRENCY), Money::plus);
        assertEquals(0, amount.compareTo(totalMapped), "Sum of mapping portions must equal transaction amount");
    }
}
