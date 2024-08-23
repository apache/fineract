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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import static java.math.BigDecimal.ZERO;
import static org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE;
import static org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

class ProgressiveLoanScheduleGeneratorTest {

    static class TestRow {

        LocalDate fromDate;
        LocalDate dueDate;
        BigDecimal balance;
        BigDecimal principal;
        BigDecimal interest;
        BigDecimal fee;
        BigDecimal penalty;
        boolean paid;

        TestRow(LocalDate fromDate, LocalDate dueDate, BigDecimal balance, BigDecimal principal, BigDecimal interest, BigDecimal fee,
                BigDecimal penalty, boolean paid) {
            this.fromDate = fromDate;
            this.dueDate = dueDate;
            this.balance = balance;
            this.principal = principal;
            this.interest = interest;
            this.fee = fee;
            this.penalty = penalty;
            this.paid = paid;
        }
    }

    private ProgressiveLoanScheduleGenerator generator = new ProgressiveLoanScheduleGenerator(null, null);
    private MonetaryCurrency usd = new MonetaryCurrency("USD", 2, null);
    private HolidayDetailDTO holidays = new HolidayDetailDTO(false, null, null);
    LoanRepaymentScheduleTransactionProcessor processor = mock(LoanRepaymentScheduleTransactionProcessor.class);

    static {
        ConfigurationDomainService domainService = mock(ConfigurationDomainService.class);
        when(domainService.getRoundingMode()).thenReturn(RoundingMode.HALF_UP.ordinal());
        ReflectionTestUtils.setField(MoneyHelper.class, "staticConfigurationDomainService", domainService);
    }

    @BeforeAll
    public static void beforeAll() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Logger.ROOT_LOGGER_NAME).setLevel(ch.qos.logback.classic.Level.DEBUG);
    }

    @AfterAll
    public static void afterAll() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
    }

    public List<TestRow> testRows() {
        return List.of(
                new TestRow(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), BigDecimal.valueOf(83.57), BigDecimal.valueOf(16.43),
                        BigDecimal.valueOf(0.58), ZERO, ZERO, true),
                new TestRow(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), BigDecimal.valueOf(67.05), BigDecimal.valueOf(16.52),
                        BigDecimal.valueOf(0.49), ZERO, ZERO, false),
                new TestRow(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1), BigDecimal.valueOf(50.43), BigDecimal.valueOf(16.62),
                        BigDecimal.valueOf(0.39), ZERO, ZERO, false),
                new TestRow(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), BigDecimal.valueOf(33.71), BigDecimal.valueOf(16.72),
                        BigDecimal.valueOf(0.29), ZERO, ZERO, false),
                new TestRow(LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1), BigDecimal.valueOf(16.90), BigDecimal.valueOf(16.81),
                        BigDecimal.valueOf(0.20), ZERO, ZERO, false),
                new TestRow(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1), BigDecimal.valueOf(00.90), BigDecimal.valueOf(16.90),
                        BigDecimal.valueOf(0.10), ZERO, ZERO, false));
    }

    @Test
    public void calculatePrepaymentAmount_TILL_PRE_CLOSURE_DATE() {
        LoanApplicationTerms terms = mock(LoanApplicationTerms.class);
        when(terms.getPreClosureInterestCalculationStrategy()).thenReturn(TILL_PRE_CLOSURE_DATE);
        Loan loan = prepareLoanWithInstallments(testRows());

        OutstandingAmountsDTO amounts = generator.calculatePrepaymentAmount(usd, LocalDate.of(2024, 2, 15), terms, MathContext.DECIMAL32,
                loan, holidays, processor);
        assertEquals(BigDecimal.valueOf(83.84), amounts.getTotalOutstanding().getAmount());
    }

    @Test
    public void calculatePrepaymentAmount_TILL_REST_FREQUENCY_DATE() {
        LoanApplicationTerms terms = mock(LoanApplicationTerms.class);
        when(terms.getPreClosureInterestCalculationStrategy()).thenReturn(TILL_REST_FREQUENCY_DATE);
        Loan loan = prepareLoanWithInstallments(testRows());

        OutstandingAmountsDTO amounts = generator.calculatePrepaymentAmount(usd, LocalDate.of(2024, 2, 15), terms, MathContext.DECIMAL32,
                loan, holidays, processor);
        assertEquals(BigDecimal.valueOf(84.06), amounts.getTotalOutstanding().getAmount());
    }

    @Test
    public void calculateSameDayPayoff_TILL_PRE_CLOSURE_DATE() {
        LoanApplicationTerms terms = mock(LoanApplicationTerms.class);
        when(terms.getPreClosureInterestCalculationStrategy()).thenReturn(TILL_PRE_CLOSURE_DATE);

        Loan loan = prepareLoanWithInstallments(List.of(
                new TestRow(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), BigDecimal.valueOf(102), BigDecimal.valueOf(100),
                        BigDecimal.valueOf(2), ZERO, ZERO, false),
                new TestRow(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), BigDecimal.valueOf(102), BigDecimal.valueOf(100),
                        BigDecimal.valueOf(2), ZERO, ZERO, false)));

        OutstandingAmountsDTO amounts = generator.calculatePrepaymentAmount(usd, LocalDate.of(2024, 1, 1), terms, MathContext.DECIMAL32,
                loan, holidays, processor);
        assertEquals(BigDecimal.valueOf(200.0).longValue(), amounts.getTotalOutstanding().getAmount().longValue());
    }

    @Test
    public void calculateSameDayPayoff_TILL_REST_FREQUENCY_DATE() {
        LoanApplicationTerms terms = mock(LoanApplicationTerms.class);
        when(terms.getPreClosureInterestCalculationStrategy()).thenReturn(TILL_REST_FREQUENCY_DATE);

        Loan loan = prepareLoanWithInstallments(List.of(
                new TestRow(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), BigDecimal.valueOf(102), BigDecimal.valueOf(100),
                        BigDecimal.valueOf(2), ZERO, ZERO, false),
                new TestRow(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), BigDecimal.valueOf(102), BigDecimal.valueOf(100),
                        BigDecimal.valueOf(2), ZERO, ZERO, false)));

        OutstandingAmountsDTO amounts = generator.calculatePrepaymentAmount(usd, LocalDate.of(2024, 1, 1), terms, MathContext.DECIMAL32,
                loan, holidays, processor);
        assertEquals(BigDecimal.valueOf(200.0).longValue(), amounts.getTotalOutstanding().getAmount().longValue());
    }

    @NotNull
    private Loan prepareLoanWithInstallments(List<TestRow> rows) {
        Loan loan = mock(Loan.class);
        List<LoanRepaymentScheduleInstallment> installments = createInstallments(rows, loan, usd);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(installments);
        when(loan.getCurrency()).thenReturn(usd);
        return loan;
    }

    private List<LoanRepaymentScheduleInstallment> createInstallments(List<TestRow> rows, Loan loan, MonetaryCurrency usd) {
        AtomicInteger count = new AtomicInteger(1);
        return rows.stream().map(row -> {
            LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, count.incrementAndGet(), row.fromDate,
                    row.dueDate, row.principal, row.interest, row.fee, row.penalty, true, null, null, row.paid);
            if (row.paid) {
                installment.payPrincipalComponent(row.fromDate, Money.of(usd, row.principal));
                installment.payInterestComponent(row.fromDate, Money.of(usd, row.interest));
                installment.updateObligationMet(true);
            }
            return installment;
        }).toList();
    }
}
