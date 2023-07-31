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

import static org.mockito.ArgumentMatchers.refEq;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessorTest {

    private static final MonetaryCurrency MONETARY_CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);
    private final LocalDate transactionDate = LocalDate.of(2023, 7, 11);
    private final LocalDate firstInstallmentToDate = LocalDate.of(2023, 7, 11);
    private final LocalDate firstInstallmentDueDate = LocalDate.of(2023, 7, 31);
    private final LocalDate lateDate = firstInstallmentDueDate.plusDays(1);
    private final Money zero = Money.zero(MONETARY_CURRENCY);
    private final Money one = Money.of(MONETARY_CURRENCY, BigDecimal.ONE);
    private final Money two = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(2));
    private final Money three = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(3));
    private final Money four = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(4));
    private final Money five = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(5));
    private final Money six = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(6));
    private final Money ten = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(10));
    private final Money eleven = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(11));
    private DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor underTest;
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
        underTest = new DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor();
        Mockito.reset(charges, transactionMappings);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, transactionDate)));
    }

    // IN ADVANCE
    @Test
    public void inAdvancePaymentOfPrincipal() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 5, interest 0, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(zero));
    }

    @Test
    public void inAdvancePaymentOfInterest() {
        Mockito.when(charges.stream()).thenReturn(Stream.empty());
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.valueOf(5L), BigDecimal.ZERO, BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 5, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(five), refEq(zero), refEq(zero));
    }

    @Test
    public void inAdvancePaymentOfFee() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, false, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.valueOf(5L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 5, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(five), refEq(zero));
    }

    @Test
    public void inAdvancePaymentOfPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, true, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, true, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 5, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFee() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, true, six, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(2L), BigDecimal.valueOf(6L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 0, fee 1, penalty 6
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(one), refEq(six));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterest() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 1, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(one), refEq(two), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPartialPrincipal() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(2));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 2, interest 0, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(zero), refEq(zero), refEq(zero));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPrincipalAndPartialPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(4));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 0, fee 0, penalty 1
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(zero), refEq(one));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPrincipalAndPenaltyAndPartialFee() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(8));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 0, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(one), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPrincipalAndPenaltyAndFeeAndPartialInterest() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 1, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(one), refEq(two), refEq(four));
    }

    @Test
    public void duePaymentOfPenaltyAsInAdvanceTransaction() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, two, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 1, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(one), refEq(two), refEq(four));
    }

    @Test
    public void duePaymentOfPenaltyAndFeeAsInAdvanceTransaction() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, two, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate.minusDays(1), false, one, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 1, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(one), refEq(two), refEq(four));
    }

    @Test
    public void duePaymentOfHigherPenaltyAndHigherFeeAsInAdvanceTransaction() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, eleven, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate.minusDays(1), false, eleven, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(2L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 2, interest 2, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(two), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyWherePenaltyIsSameDayButLaterThanRepayment() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, one, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, true, three, OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = four;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        Mockito.when(loanTransaction.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 0, fee 0, penalty 1
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(zero), refEq(one));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyWherePenaltyIsSameDayButEarlierThanRepayment() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, one, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, true, three, OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = four;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        Mockito.when(loanTransaction.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 3, ZoneOffset.UTC)));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyWherePenaltyIsSameDayButNoChargeCreatedDate() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, one, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, true, three, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = five;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 1, interest 0, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(one), refEq(zero), refEq(zero), refEq(four));
    }

    @Test
    public void inAdvancePaymentOfPrincipalAndPenaltyWherePenaltyIsSameDayButNoTransactionCreatedDate() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, one, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, true, three, OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = five;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, transactionDate, ExternalId.empty()));
        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 1, interest 0, fee 0, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(one), refEq(zero), refEq(zero), refEq(four));
    }

    // ON TIME
    @Test
    public void onTimePaymentOfPrincipal() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 5, interest 0, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(zero));
    }

    @Test
    public void onTimePaymentOfInterest() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.valueOf(5L), BigDecimal.ZERO, BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 0, interest 5, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(five), refEq(zero), refEq(zero));
    }

    @Test
    public void onTimePaymentOfFee() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, false, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.valueOf(5L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 0, interest 0, fee 5, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(five), refEq(zero));
    }

    @Test
    public void onTimePaymentOfPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 0, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 5, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFee() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, six, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(2L), BigDecimal.valueOf(6L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 2, interest 0, fee 2, penalty 6
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(zero), refEq(two), refEq(six));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterest() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 2, interest 2, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(two), refEq(four));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPartialPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.ONE);
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 0, interest 0, fee 0, penalty 1
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(one));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndPartialFee() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(5));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 0, interest 0, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(one), refEq(four));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndFeeAndPartialInterest() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(7));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 0, interest 1, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(one), refEq(two), refEq(four));
    }

    @Test
    public void onTimePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndFeeAndInterestAndPartialPrincipal() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentDueDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 2, interest 2, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(two), refEq(four));
    }

    @Test
    public void onTimePaymentOfPrincipalAndFeeWhereFeeIsSameDayButEarlierThanRepayment() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, three,
                OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = four;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentToDate, ExternalId.empty()));
        Mockito.when(loanTransaction.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 3, ZoneOffset.UTC)));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 0, interest 0, fee 4, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(four), refEq(zero));
    }

    @Test
    public void onTimePaymentOfPrincipalAndFeeWhereFeeIsSameDayButLaterThanRepayment() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, three,
                OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = four;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentToDate, ExternalId.empty()));
        Mockito.when(loanTransaction.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 3, interest 0, fee 1, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(one), refEq(zero));
    }

    @Test
    public void onTimePaymentOfPrincipalAndFeeWhereFeeIsSameDayButNoChargeCreatedDate() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, three, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = five;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentToDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 1, interest 0, fee 4, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(one), refEq(zero), refEq(four), refEq(zero));
    }

    @Test
    public void onTimePaymentOfPrincipalAndFeeWhereFeeIsSameDayButNoTransactionCreatedDate() {
        LoanCharge loanCharge1 = createLoanCharge(firstInstallmentToDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(firstInstallmentToDate, false, three,
                OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = five;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, firstInstallmentToDate, ExternalId.empty()));
        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, loanTransaction, transactionAmount, transactionMappings,
                charges);

        // Principal 1, interest 0, fee 4, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(one), refEq(zero), refEq(four), refEq(zero));
    }

    // LATE
    @Test
    public void latePaymentOfPrincipal() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 5, interest 0, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(zero));
    }

    @Test
    public void latePaymentOfInterest() {
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.valueOf(5L), BigDecimal.ZERO, BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 5, fee 0, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(five), refEq(zero), refEq(zero));
    }

    @Test
    public void latePaymentOfFee() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, false, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.valueOf(5L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 5, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(five), refEq(zero));
    }

    @Test
    public void latePaymentOfPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void latePaymentOfPrincipalAndPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, five, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(5L),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 5, interest 0, fee 0, penalty 5
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(five), refEq(zero), refEq(zero), refEq(five));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFee() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, six, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(2L), BigDecimal.valueOf(6L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 2, interest 0, fee 2, penalty 6
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(zero), refEq(two), refEq(six));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterest() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 2, interest 2, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(two), refEq(four));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPartialPenalty() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.ONE);
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 0, penalty 1
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(zero), refEq(one));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndPartialFee() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(5));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 1, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(one), refEq(four));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndFeeAndPartialInterest() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(7));
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 1, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(one), refEq(two), refEq(four));
    }

    @Test
    public void latePaymentOfPrincipalAndPenaltyAndFeeAndInterestButNotEnoughOnlyForPenaltyAndFeeAndInterestAndPartialPrincipal() {
        LoanCharge loanCharge1 = createLoanCharge(transactionDate, true, four, null);
        LoanCharge loanCharge2 = createLoanCharge(transactionDate, false, two, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = ten;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.valueOf(2L), BigDecimal.valueOf(2L), BigDecimal.valueOf(4L), false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 2, interest 2, fee 2, penalty 4
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(two), refEq(two), refEq(two), refEq(four));
    }

    @Test
    public void latePaymentOfPrincipalAndFeeWhereFeeIsSameDayButEarlierThanRepayment() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, three, OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = four;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        Mockito.when(loanTransaction.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 3, ZoneOffset.UTC)));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 0, interest 0, fee 4, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(zero), refEq(zero), refEq(four), refEq(zero));
    }

    @Test
    public void latePaymentOfPrincipalAndFeeWhereFeeIsSameDayButLaterThanRepayment() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, three, OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = four;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        Mockito.when(loanTransaction.getCreatedDate()).thenReturn(Optional.of(OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 3, interest 0, fee 1, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(three), refEq(zero), refEq(one), refEq(zero));
    }

    @Test
    public void latePaymentOfPrincipalAndFeeWhereFeeIsSameDayButNoChargeCreatedDate() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, three, null);
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = five;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 1, interest 0, fee 4, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(one), refEq(zero), refEq(four), refEq(zero));
    }

    @Test
    public void latePaymentOfPrincipalAndFeeWhereFeeIsSameDayButNoTransactionCreatedDate() {
        LoanCharge loanCharge1 = createLoanCharge(lateDate, false, one, null);
        LoanCharge loanCharge2 = createLoanCharge(lateDate, false, three, OffsetDateTime.of(2023, 1, 1, 1, 1, 1, 2, ZoneOffset.UTC));
        Mockito.when(charges.stream()).thenReturn(Stream.of(loanCharge1, loanCharge2));
        Money transactionAmount = five;
        LoanRepaymentScheduleInstallment installment = Mockito
                .spy(new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate, firstInstallmentDueDate, BigDecimal.valueOf(3L),
                        BigDecimal.ZERO, BigDecimal.valueOf(4L), BigDecimal.ZERO, false, null, BigDecimal.ZERO));
        LoanTransaction loanTransaction = Mockito
                .spy(LoanTransaction.repayment(office, transactionAmount, null, lateDate, ExternalId.empty()));
        underTest.handleTransactionThatIsALateRepaymentOfInstallment(installment, null, loanTransaction, transactionAmount,
                transactionMappings, charges);

        // Principal 1, interest 0, fee 4, penalty 0
        Mockito.verify(loanTransaction, Mockito.times(1)).updateComponents(refEq(one), refEq(zero), refEq(four), refEq(zero));
    }

    @NotNull
    private LoanCharge createLoanCharge(LocalDate dueDate, boolean isPenalty, Money amount, OffsetDateTime createdDate) {
        LoanCharge loanCharge = Mockito.mock(LoanCharge.class);
        Mockito.lenient().when(loanCharge.isActive()).thenReturn(true);
        Mockito.lenient().when(loanCharge.isNotFullyPaid()).thenReturn(true);
        Mockito.lenient().when(loanCharge.getEffectiveDueDate()).thenReturn(dueDate);
        Mockito.lenient().when(loanCharge.isPenaltyCharge()).thenReturn(isPenalty);
        if (createdDate != null) {
            Mockito.lenient().when(loanCharge.getCreatedDate()).thenReturn(Optional.of(createdDate));
        }
        Mockito.lenient().when(loanCharge.getAmount(refEq(MONETARY_CURRENCY))).thenReturn(amount);
        return loanCharge;
    }
}
