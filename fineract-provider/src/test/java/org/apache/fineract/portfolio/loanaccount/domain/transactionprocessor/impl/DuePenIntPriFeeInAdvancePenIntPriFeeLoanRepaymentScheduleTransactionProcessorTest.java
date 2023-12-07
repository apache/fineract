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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
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
public class DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessorTest {

    private static final MonetaryCurrency MONETARY_CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);
    private final LocalDate transactionDate = LocalDate.of(2023, 7, 11);
    private final LocalDate firstInstallmentToDate = LocalDate.of(2023, 7, 11);
    private final LocalDate firstInstallmentDueDate = LocalDate.of(2023, 7, 31);
    private final LocalDate lateDate = firstInstallmentDueDate.plusDays(1);
    private final Money zero = Money.zero(MONETARY_CURRENCY);
    private final Money one = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(1));
    private final Money two = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(2));
    private final Money three = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(3));
    private final Money four = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(4));
    private final Money five = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(5));
    private final Money six = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(6));
    private final Money seven = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(7));
    private final Money eight = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(8));
    private final Money nine = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(9));
    private final Money ten = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(10));

    private final Money eleven = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(11));
    private DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor underTest;
    @Mock
    private Set<LoanCharge> charges;
    @Mock
    private Office office;
    @Mock
    private Loan loan;
    @Mock
    private List<LoanTransactionToRepaymentScheduleMapping> transactionMappings;

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
        underTest = new DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor();
        Mockito.reset(charges, transactionMappings);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, transactionDate)));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    // IN ADVANCE
    @Test
    public void inAdvancePaymentOfPrincipal() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 5, but no outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 5, but no outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 5, but 0 is outstanding for this installment, so 5 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 5, but no outstanding of fee
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(five));
        // Principal 5, interest 0, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(zero));
    }

    @Test
    public void inAdvancePaymentOfInterest() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(5L), BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 10, but no outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 10, but only 5 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 5, but 0 is outstanding for this installment, so 5 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(five));
        // In advance with value of 5, but no outstanding of fee
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(five));
        // Principal 0, interest 5, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(five), refEq(zero), refEq(zero));
    }

    @Test
    public void inAdvancePaymentOfFee() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 10, but no outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 10, but no outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 10, but 0 is outstanding for this installment, so 10 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 10, but no only 5 is outstanding of fee
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(ten));
        // Principal 0, interest 0, fee 5, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(five), refEq(zero));
    }

    @Test
    public void inAdvancePaymentOfPenalty() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 10, but only 5 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 5, but no outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(five));
        // In advance with value of 5, but 0 is outstanding for this installment, so 5 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(five));
        // In advance with value of 5, but no outstanding of fee
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(five));
        // Principal 0, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenalty() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(2)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 10, and 5 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(five));
        // In advance with value of 5, but only 5 is outstanding for this installment, so 0 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(five));
        // Principal 5, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFee() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(2L), BigDecimal.valueOf(6L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 10, but only 6 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 4, but no outstanding
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(four));
        // In advance with value of 4, but only 3 is outstanding for this installment, so 1 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(four));
        // In advance with value of 1, and 2 is outstanding of fee
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(one));
        // Principal 3, interest 0, fee 1, penalty 6
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(one), refEq(six));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterest() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 10, but only 4 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 6, but only 2 is outstanding
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(six));
        // In advance with value of 4, but only 3 is outstanding for this installment, so 1 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(four));
        // In advance with value of 1, and 2 is outstanding of fee
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(one));
        // Principal 3, interest 1, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(two), refEq(one), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPartialPenalty() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(2));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 2, but 4 is outstanding for this installment
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(two));
        // Principal 0, interest 0, fee 0, penalty 2
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(two));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndPartialInterest() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(5));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 5, and 4 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(five));
        // In advance with value of 1, and 2 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(one));
        // In advance with value of 0, and 3 is outstanding for this installment, so 0 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(2)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // Principal 0, interest 1, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(one), refEq(zero), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndInterestAndPartialPrincipal() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(7));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 7, and 4 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(seven));
        // In advance with value of 3, and 2 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(three));
        // In advance with value of 1, and 3 is outstanding for this installment, so 0 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(one));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(2)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // Principal 1, interest 2, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(one), refEq(two), refEq(zero), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndInterestAndPrincipalAndPartialFee() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(zero));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(one));
        // In advance with value of 10, and 4 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // In advance with value of 6, and 2 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(six));
        // In advance with value of 4, and 3 is outstanding for this installment, so 1 is unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(four));
        // In advance with value of 1, and outstanding is 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(one));
        // Principal 3, interest 1, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(two), refEq(one), refEq(four));
    }

    @Test
    public void duePaymentOfPenaltyAsInAdvanceTransaction() {

        LoanCharge loanCharge1 = Mockito.mock(LoanCharge.class);
        Mockito.when(loanCharge1.isActive()).thenReturn(true);
        Mockito.when(loanCharge1.isChargePending()).thenReturn(true);
        Mockito.when(loanCharge1.getEffectiveDueDate()).thenReturn(transactionDate);
        Mockito.when(loanCharge1.isPenaltyCharge()).thenReturn(true);
        Mockito.when(loanCharge1.getAmount(refEq(MONETARY_CURRENCY))).thenReturn(two);
        LoanCharge loanCharge2 = Mockito.mock(LoanCharge.class);
        Mockito.when(loanCharge2.isActive()).thenReturn(true);
        Mockito.when(loanCharge2.isChargePending()).thenReturn(true);
        Mockito.when(loanCharge2.getEffectiveDueDate()).thenReturn(transactionDate.plusDays(1));

        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated two
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(two));
        // Calculated zero
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(zero));
        // In advance with value of 8, and 2 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(eight));
        // In advance with value of 6, and 2 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(six));
        // In advance with value of 4, and 3 is outstanding of principal
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(four));
        // In advance with value of 1, but only 2 is outstanding of fee
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(one));
        // Principal 3, interest 2, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(two), refEq(one), refEq(four));
    }

    @Test
    public void duePaymentOfPenaltyAndFeeAsInAdvanceTransaction() {

        LoanCharge loanCharge1 = Mockito.mock(LoanCharge.class);
        Mockito.when(loanCharge1.isActive()).thenReturn(true);
        Mockito.when(loanCharge1.isChargePending()).thenReturn(true);
        Mockito.when(loanCharge1.getEffectiveDueDate()).thenReturn(transactionDate);
        Mockito.when(loanCharge1.isPenaltyCharge()).thenReturn(true);
        Mockito.when(loanCharge1.getAmount(refEq(MONETARY_CURRENCY))).thenReturn(two);
        LoanCharge loanCharge2 = Mockito.mock(LoanCharge.class);
        Mockito.when(loanCharge2.isActive()).thenReturn(true);
        Mockito.when(loanCharge2.isChargePending()).thenReturn(true);
        Mockito.when(loanCharge2.isPenaltyCharge()).thenReturn(false);
        Mockito.when(loanCharge2.getEffectiveDueDate()).thenReturn(transactionDate.minusDays(1));
        Mockito.when(loanCharge2.getAmount(refEq(MONETARY_CURRENCY))).thenReturn(one);

        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(2L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated two
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(two));
        // Calculated one
        Mockito.verify(installment, Mockito.times(2)).payFeeChargesComponent(eq(transactionDate), refEq(one));
        // In advance with value of 7, and 2 is outstanding of penalty
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(seven));
        // In advance with value of 5, and 2 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(five));
        // In advance with value of 3, and 2 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(three));
        // Principal 2, interest 2, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(two), refEq(four));
    }

    @Test
    public void duePaymentOfHigherPenaltyAndHigherFeeAsInAdvanceTransaction() {

        LoanCharge loanCharge1 = Mockito.mock(LoanCharge.class);
        Mockito.when(loanCharge1.isActive()).thenReturn(true);
        Mockito.when(loanCharge1.isChargePending()).thenReturn(true);
        Mockito.when(loanCharge1.getEffectiveDueDate()).thenReturn(transactionDate);
        Mockito.when(loanCharge1.isPenaltyCharge()).thenReturn(true);
        Mockito.when(loanCharge1.getAmount(refEq(MONETARY_CURRENCY))).thenReturn(eleven);
        LoanCharge loanCharge2 = Mockito.mock(LoanCharge.class);
        Mockito.when(loanCharge2.isActive()).thenReturn(true);
        Mockito.when(loanCharge2.isChargePending()).thenReturn(true);
        Mockito.when(loanCharge2.isPenaltyCharge()).thenReturn(false);
        Mockito.when(loanCharge2.getEffectiveDueDate()).thenReturn(transactionDate.minusDays(1));
        Mockito.when(loanCharge2.getAmount(refEq(MONETARY_CURRENCY))).thenReturn(eleven);

        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(2L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Calculated eleven, overriden by unprocessed
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(transactionDate), refEq(ten));
        // Calculated eleven, overriden by unprocessed
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(transactionDate), refEq(six));
        // In advance with value of 4, and 2 is outstanding of interest
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(transactionDate), refEq(four));
        // In advance with value of 2, and 2 is outstanding of principal
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(transactionDate), refEq(two));

        // Principal 2, interest 2, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(two), refEq(four));
    }

    // ON TIME
    @Test
    public void onTimePaymentOfPrincipal() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(five));
        // Principal 5, interest 0, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(zero));
    }

    @Test
    public void onTimePaymentOfInterest() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(5L), BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(five));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(five));
        // Principal 0, interest 5, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(five), refEq(zero), refEq(zero));
    }

    @Test
    public void onTimePaymentOfFee() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Principal 0, interest 0, fee 5, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(five), refEq(zero));
    }

    @Test
    public void onTimePaymentOfPenalty() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(five));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(five));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(five));
        // Principal 0, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenalty() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(five));
        // Unprocessed: 5, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(five));
        // Unprocessed: 0, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(zero));
        // Principal 5, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFee() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(2L), BigDecimal.valueOf(6L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 6
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 4, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(four));
        // Unprocessed: 4, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(four));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(one));
        // Principal 3, interest 0, fee 1, penalty 6
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(one), refEq(six));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterest() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 6, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(six));
        // Unprocessed: 4, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(four));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(one));
        // Principal 3, interest 2, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(two), refEq(one), refEq(four));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPartialPenalty() {
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(1));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 1, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(one));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(zero));
        // Unprocessed: 0, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(zero));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(zero));
        // Principal 0, interest 0, fee 0, penalty 1
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(one));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndPartialInterest() {
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(5));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 5, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(five));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(one));
        // Unprocessed: 0, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(zero));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(zero));
        // Principal 0, interest 1, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(one), refEq(zero), refEq(four));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndInterestAndPartialPrincipal() {
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(8));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 8, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(eight));
        // Unprocessed: 4, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(four));
        // Unprocessed: 2, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(two));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(zero));
        // Principal 2, interest 2, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(zero), refEq(four));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndInterestAndPrincipalAndPartialFee() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Unprocessed: 10, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(firstInstallmentDueDate), refEq(ten));
        // Unprocessed: 6, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(firstInstallmentDueDate), refEq(six));
        // Unprocessed: 4, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(firstInstallmentDueDate), refEq(four));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(firstInstallmentDueDate), refEq(one));
        // Principal 3, interest 2, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(two), refEq(one), refEq(four));
    }

    // LATE
    @Test
    public void latePaymentOfPrincipal() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(five));
        // Principal 5, interest 0, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(zero));
    }

    @Test
    public void latePaymentOfInterest() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(5L), BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(five));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(five));
        // Principal 0, interest 5, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(five), refEq(zero), refEq(zero));
    }

    @Test
    public void latePaymentOfFee() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), BigDecimal.valueOf(0L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 10, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(ten));
        // Principal 0, interest 0, fee 5, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(five), refEq(zero));
    }

    @Test
    public void latePaymentOfPenalty() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(0L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(five));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(five));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(five));
        // Principal 0, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void latePaymentOfPrincipalAndPenalty() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(0L), BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 5, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(five));
        // Unprocessed: 5, outstanding: 5
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(five));
        // Unprocessed: 0, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(zero));
        // Principal 5, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFee() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(0L), BigDecimal.valueOf(2L), BigDecimal.valueOf(6L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 6
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 4, outstanding: 0
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(four));
        // Unprocessed: 4, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(four));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(one));
        // Principal 3, interest 0, fee 1, penalty 6
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(one), refEq(six));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterest() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 6, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(six));
        // Unprocessed: 4, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(four));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(one));
        // Principal 3, interest 2, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(two), refEq(one), refEq(four));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPartialPenalty() {
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(1));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 1, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(one));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(zero));
        // Unprocessed: 0, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(zero));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(zero));
        // Principal 0, interest 0, fee 0, penalty 1
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(one));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndPartialInterest() {
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(5));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 5, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(five));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(one));
        // Unprocessed: 0, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(zero));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(zero));
        // Principal 0, interest 1, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(one), refEq(zero), refEq(four));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndInterestAndPartialPrincipal() {
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(8));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 8, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(eight));
        // Unprocessed: 4, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(four));
        // Unprocessed: 2, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(two));
        // Unprocessed: 0, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(zero));
        // Principal 2, interest 2, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(zero), refEq(four));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndInterestAndPrincipalAndPartialFee() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Unprocessed: 10, outstanding: 4
        Mockito.verify(installment, Mockito.times(1)).payPenaltyChargesComponent(eq(lateDate), refEq(ten));
        // Unprocessed: 6, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payInterestComponent(eq(lateDate), refEq(six));
        // Unprocessed: 4, outstanding: 3
        Mockito.verify(installment, Mockito.times(1)).payPrincipalComponent(eq(lateDate), refEq(four));
        // Unprocessed: 1, outstanding: 2
        Mockito.verify(installment, Mockito.times(1)).payFeeChargesComponent(eq(lateDate), refEq(one));
        // Principal 3, interest 2, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(two), refEq(one), refEq(four));
    }
}
