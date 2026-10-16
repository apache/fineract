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

/**
 * An interest waiver under the HeavensFamily strategy posted double the waived amount to the GL, because the
 * interest-waiver branch applied {@code updateComponents} and the unconditional call at the end of the method applied
 * it a second time - and {@code updateComponents} accumulates onto the existing portion.
 */
@ExtendWith(MockitoExtension.class)
public class HeavensFamilyLoanRepaymentScheduleTransactionProcessorTest {

    private static final MonetaryCurrency MONETARY_CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);

    private final LocalDate transactionDate = LocalDate.of(2023, 7, 11);
    private final LocalDate firstInstallmentToDate = LocalDate.of(2023, 7, 11);
    private final LocalDate firstInstallmentDueDate = LocalDate.of(2023, 7, 31);
    private final Money zero = Money.zero(MONETARY_CURRENCY);
    private final Money ten = Money.of(MONETARY_CURRENCY, BigDecimal.valueOf(10));

    private HeavensFamilyLoanRepaymentScheduleTransactionProcessor underTest;
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
        underTest = new HeavensFamilyLoanRepaymentScheduleTransactionProcessor(mock(ExternalIdFactory.class),
                mock(LoanChargeValidator.class), mock(LoanBalanceService.class));
        transactionMappings = new ArrayList<>();
        Mockito.lenient().when(loan.getCurrency()).thenReturn(MONETARY_CURRENCY);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, transactionDate)));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void onTimeInterestWaiverAppliesTheWaivedAmountToInterestPortionExactlyOnce() {
        final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate,
                firstInstallmentDueDate, BigDecimal.ZERO, BigDecimal.valueOf(10L), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final LoanTransaction waiver = LoanTransaction.waiver(office, loan, ten, transactionDate, ten, zero, ExternalId.empty());
        // the real pipeline clears the derived portions before handing the transaction to the strategy
        waiver.resetDerivedComponents();

        underTest.handleTransactionThatIsOnTimePaymentOfInstallment(installment, waiver, ten, transactionMappings, charges);

        // without the fix this was 20.00 - the waived amount applied twice
        assertEquals(0, BigDecimal.valueOf(10).compareTo(waiver.getInterestPortion()),
                "waived interest must be applied exactly once, got " + waiver.getInterestPortion());
    }

    @Test
    public void inAdvanceInterestWaiverAppliesTheWaivedAmountToInterestPortionExactlyOnce() {
        final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, 1, firstInstallmentToDate,
                firstInstallmentDueDate, BigDecimal.ZERO, BigDecimal.valueOf(10L), BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                BigDecimal.ZERO);

        final LoanTransaction waiver = LoanTransaction.waiver(office, loan, ten, transactionDate, ten, zero, ExternalId.empty());
        waiver.resetDerivedComponents();

        underTest.handleTransactionThatIsPaymentInAdvanceOfInstallment(installment, null, waiver, ten, transactionMappings, charges);

        // without the fix this was 20.00 - the waived amount applied twice
        assertEquals(0, BigDecimal.valueOf(10).compareTo(waiver.getInterestPortion()),
                "waived interest must be applied exactly once, got " + waiver.getInterestPortion());
    }
}
