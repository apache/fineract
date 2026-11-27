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
package org.apache.fineract.portfolio.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class InstallmentProcessingHelperTest {

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 11, 22);

    private MockedStatic<MoneyHelper> moneyHelper;
    private final Set<LoanInstallmentCharge> installmentChargesOfLoanCharge = new HashSet<>();
    private final LoanCharge loanCharge = mock(LoanCharge.class);

    @BeforeEach
    void setUp() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(19, RoundingMode.HALF_EVEN));
        when(loanCharge.getLoanInstallmentCharge()).thenReturn(installmentChargesOfLoanCharge);
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    @Test
    void removeInstallmentsIfRemovesTheChargesOfTheRemovedInstallmentsOnly() {
        final LoanRepaymentScheduleInstallment regular = installment(1, false);
        final LoanInstallmentCharge chargeOfRegular = charge(regular);
        final LoanRepaymentScheduleInstallment reAged = installment(2, true);
        final LoanInstallmentCharge chargeOfReAged = charge(reAged);
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>(List.of(regular, reAged));

        InstallmentProcessingHelper.removeInstallmentsIf(installments, LoanRepaymentScheduleInstallment::isReAged);

        assertThat(installments).containsExactly(regular);
        assertThat(regular.getInstallmentCharges()).containsExactly(chargeOfRegular);
        assertThat(reAged.getInstallmentCharges()).isEmpty();
        assertThat(installmentChargesOfLoanCharge).containsExactly(chargeOfRegular).doesNotContain(chargeOfReAged);
    }

    @Test
    void removeInstallmentRemovesItsCharges() {
        final LoanRepaymentScheduleInstallment regular = installment(1, false);
        final LoanInstallmentCharge chargeOfRegular = charge(regular);
        final LoanRepaymentScheduleInstallment reAged = installment(2, true);
        final LoanInstallmentCharge chargeOfReAged = charge(reAged);
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>(List.of(regular, reAged));

        InstallmentProcessingHelper.removeInstallment(installments, reAged);

        assertThat(installments).containsExactly(regular);
        assertThat(reAged.getInstallmentCharges()).isEmpty();
        assertThat(installmentChargesOfLoanCharge).containsExactly(chargeOfRegular).doesNotContain(chargeOfReAged);
    }

    @Test
    void removeInstallmentLeavesTheChargesOfAnInstallmentNotInTheList() {
        final LoanRepaymentScheduleInstallment regular = installment(1, false);
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>(List.of(regular));
        final LoanRepaymentScheduleInstallment elsewhere = installment(2, true);
        final LoanInstallmentCharge chargeOfElsewhere = charge(elsewhere);

        InstallmentProcessingHelper.removeInstallment(installments, elsewhere);

        assertThat(installments).containsExactly(regular);
        assertThat(elsewhere.getInstallmentCharges()).containsExactly(chargeOfElsewhere);
        assertThat(installmentChargesOfLoanCharge).containsExactly(chargeOfElsewhere);
    }

    private static LoanRepaymentScheduleInstallment installment(final int number, final boolean reAged) {
        return new LoanRepaymentScheduleInstallment(mock(Loan.class), number, DUE_DATE.minusMonths(1), DUE_DATE.plusMonths(number - 1L),
                BigDecimal.valueOf(100), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, reAged);
    }

    private LoanInstallmentCharge charge(final LoanRepaymentScheduleInstallment installment) {
        final LoanInstallmentCharge installmentCharge = new LoanInstallmentCharge(BigDecimal.TEN, loanCharge, installment);
        installment.getInstallmentCharges().add(installmentCharge);
        installmentChargesOfLoanCharge.add(installmentCharge);
        return installmentCharge;
    }
}
