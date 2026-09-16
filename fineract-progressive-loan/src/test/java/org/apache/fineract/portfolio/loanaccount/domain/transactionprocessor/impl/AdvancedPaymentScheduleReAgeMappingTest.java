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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Both sides of the transaction to installment mapping association are mapped with orphan removal, so a mapping that is
 * left out of both owning collections is deleted when the changes are flushed. Re-aging lifts the mappings of the
 * installments it replaces, and these tests cover that they are always handed over to an installment afterwards.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdvancedPaymentScheduleReAgeMappingTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);

    private static final BigDecimal PAID_AMOUNT = BigDecimal.valueOf(2.80);
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 5, 21);
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 7, 10);

    private AdvancedPaymentScheduleTransactionProcessor underTest;
    private Loan loan;
    private LoanTransaction reAgeTransaction;
    private LoanTransaction paymentTransaction;
    private Set<LoanTransactionToRepaymentScheduleMapping> paymentTransactionMappings;

    @BeforeAll
    static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    static void destruct() {
        MONEY_HELPER.close();
    }

    @BeforeEach
    void setUp() {
        underTest = new AdvancedPaymentScheduleTransactionProcessor(mock(EMICalculator.class), null, null,
                mock(LoanScheduleComponent.class), null, null, null, null, null);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, TRANSACTION_DATE)));

        loan = mock(Loan.class);
        lenient().when(loan.loanCurrency()).thenReturn(CURRENCY);
        lenient().when(loan.getDisbursementDate()).thenReturn(DISBURSEMENT_DATE);
        lenient().when(loan.getActiveLoanTermVariations()).thenReturn(List.of());

        reAgeTransaction = mock(LoanTransaction.class);
        lenient().when(reAgeTransaction.getLoan()).thenReturn(loan);
        lenient().when(reAgeTransaction.getTransactionDate()).thenReturn(TRANSACTION_DATE);

        paymentTransaction = mock(LoanTransaction.class);
        paymentTransactionMappings = new HashSet<>();
        lenient().when(paymentTransaction.getLoanTransactionToRepaymentScheduleMappings()).thenReturn(paymentTransactionMappings);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    /**
     * The schedule left to match ends with a down payment installment, so the installment holding the early repaid
     * amounts is added to the schedule and has to take the lifted mappings with it.
     */
    @Test
    void liftedMappingIsHandedOverToTheInstallmentHoldingTheEarlyRepaidAmounts() {
        LoanRepaymentScheduleInstallment firstDownPayment = downPaymentInstallment(1, DISBURSEMENT_DATE);
        LoanRepaymentScheduleInstallment pastInstallment = regularInstallment(2, DISBURSEMENT_DATE, LocalDate.of(2026, 6, 21));
        LoanRepaymentScheduleInstallment secondDownPayment = downPaymentInstallment(3, LocalDate.of(2026, 6, 25));
        LoanRepaymentScheduleInstallment earlyPaidInstallment = regularInstallment(4, LocalDate.of(2026, 6, 21), LocalDate.of(2026, 8, 21));

        LoanTransactionToRepaymentScheduleMapping mapping = mappingBetween(paymentTransaction, earlyPaidInstallment);

        List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>(
                List.of(firstDownPayment, pastInstallment, secondDownPayment, earlyPaidInstallment));

        underTest.updateInstallmentsByModelForReAging(reAgeTransaction,
                contextFor(installments,
                        List.of(regularPeriod(DISBURSEMENT_DATE, LocalDate.of(2026, 6, 21)), earlyRepaymentHolderPeriod())),
                TRANSACTION_DATE);

        LoanRepaymentScheduleInstallment holder = installments.stream().filter(i -> i.getDueDate().isEqual(TRANSACTION_DATE)).findFirst()
                .orElseThrow(() -> new AssertionError("The installment holding the early repaid amounts was not added to the schedule"));

        assertSame(holder, mapping.getLoanRepaymentScheduleInstallment(), "Mapping should point at the installment that took the amounts");
        assertTrue(holder.getLoanTransactionToRepaymentScheduleMappings().contains(mapping),
                "Mapping is missing from the installment side, orphan removal would delete it");
        assertTrue(paymentTransactionMappings.contains(mapping),
                "Mapping is missing from the transaction side, orphan removal would delete it");
    }

    /**
     * A regular installment takes the slot of the installment holding the early repaid amounts, so that installment is
     * discarded and the lifted mappings have to go back where they came from.
     */
    @Test
    void liftedMappingIsGivenBackWhenTheHoldingInstallmentIsDiscarded() {
        LoanRepaymentScheduleInstallment downPayment = downPaymentInstallment(1, DISBURSEMENT_DATE);
        LoanRepaymentScheduleInstallment pastInstallment = regularInstallment(2, DISBURSEMENT_DATE, LocalDate.of(2026, 6, 21));
        LoanRepaymentScheduleInstallment earlyPaidInstallment = regularInstallment(3, LocalDate.of(2026, 6, 21), LocalDate.of(2026, 8, 21));

        LoanTransactionToRepaymentScheduleMapping mapping = mappingBetween(paymentTransaction, earlyPaidInstallment);

        List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>(List.of(downPayment, pastInstallment, earlyPaidInstallment));

        underTest.updateInstallmentsByModelForReAging(reAgeTransaction, contextFor(installments, List.of(earlyRepaymentHolderPeriod())),
                TRANSACTION_DATE);

        LoanRepaymentScheduleInstallment owner = mapping.getLoanRepaymentScheduleInstallment();
        assertNotNull(owner, "Mapping was left without an installment");
        assertTrue(owner.getLoanTransactionToRepaymentScheduleMappings().contains(mapping),
                "Mapping is missing from the installment side, orphan removal would delete it");
        assertTrue(paymentTransactionMappings.contains(mapping),
                "Mapping is missing from the transaction side, orphan removal would delete it");
    }

    private ProgressiveTransactionCtx contextFor(List<LoanRepaymentScheduleInstallment> installments, List<RepaymentPeriod> periods) {
        ProgressiveLoanInterestScheduleModel model = mock(ProgressiveLoanInterestScheduleModel.class);
        lenient().when(model.repaymentPeriods()).thenReturn(new ArrayList<>(periods));
        return new ProgressiveTransactionCtx(CURRENCY, installments, Set.of(), null, null, model, List.of());
    }

    private RepaymentPeriod regularPeriod(LocalDate fromDate, LocalDate dueDate) {
        RepaymentPeriod period = basePeriod(fromDate, dueDate);
        lenient().when(period.isReAgedEarlyRepaymentHolder()).thenReturn(false);
        return period;
    }

    private RepaymentPeriod earlyRepaymentHolderPeriod() {
        RepaymentPeriod period = basePeriod(LocalDate.of(2026, 6, 21), TRANSACTION_DATE);
        lenient().when(period.isReAgedEarlyRepaymentHolder()).thenReturn(true);
        return period;
    }

    private RepaymentPeriod basePeriod(LocalDate fromDate, LocalDate dueDate) {
        RepaymentPeriod period = mock(RepaymentPeriod.class);
        Money zero = Money.zero(CURRENCY);
        Money amount = Money.of(CURRENCY, BigDecimal.valueOf(50));
        Money paidAmount = Money.of(CURRENCY, PAID_AMOUNT);
        lenient().when(period.getFromDate()).thenReturn(fromDate);
        lenient().when(period.getDueDate()).thenReturn(dueDate);
        lenient().when(period.getEmi()).thenReturn(amount);
        lenient().when(period.getDuePrincipal()).thenReturn(amount);
        lenient().when(period.getDueInterest()).thenReturn(zero);
        lenient().when(period.getPaidPrincipal()).thenReturn(paidAmount);
        lenient().when(period.getPaidInterest()).thenReturn(zero);
        lenient().when(period.getCreditedPrincipal()).thenReturn(zero);
        return period;
    }

    private LoanRepaymentScheduleInstallment downPaymentInstallment(int number, LocalDate date) {
        return new LoanRepaymentScheduleInstallment(loan, number, date, date, BigDecimal.valueOf(150), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, false, null, BigDecimal.ZERO, true);
    }

    private LoanRepaymentScheduleInstallment regularInstallment(int number, LocalDate fromDate, LocalDate dueDate) {
        return new LoanRepaymentScheduleInstallment(loan, number, fromDate, dueDate, BigDecimal.valueOf(150), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, null, BigDecimal.ZERO, false);
    }

    private LoanTransactionToRepaymentScheduleMapping mappingBetween(LoanTransaction transaction,
            LoanRepaymentScheduleInstallment installment) {
        Money paid = Money.of(CURRENCY, PAID_AMOUNT);
        installment.payPrincipalComponent(LocalDate.of(2026, 6, 10), paid);
        LoanTransactionToRepaymentScheduleMapping mapping = LoanTransactionToRepaymentScheduleMapping.createFrom(transaction, installment,
                paid, Money.zero(CURRENCY), Money.zero(CURRENCY), Money.zero(CURRENCY));
        installment.getLoanTransactionToRepaymentScheduleMappings().add(mapping);
        paymentTransactionMappings.add(mapping);
        return mapping;
    }
}
