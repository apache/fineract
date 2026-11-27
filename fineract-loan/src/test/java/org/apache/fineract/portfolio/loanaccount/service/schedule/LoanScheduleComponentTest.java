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
package org.apache.fineract.portfolio.loanaccount.service.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * An installment charge belongs to its installment and to its loan charge. When the schedule is rebuilt, the charges of
 * the installments it drops or recreates must leave their loan charge as well.
 */
class LoanScheduleComponentTest {

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 5, 14);

    private MockedStatic<MoneyHelper> moneyHelper;
    private final LoanScheduleComponent component = new LoanScheduleComponent(mock(LoanBalanceService.class));
    private final Loan loan = mock(Loan.class);
    private final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
    private final Set<LoanInstallmentCharge> installmentChargesOfLoanCharge = new HashSet<>();
    private final LoanCharge loanCharge = mock(LoanCharge.class);

    @BeforeEach
    void setUp() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(19, RoundingMode.HALF_EVEN));
        when(loan.getRepaymentScheduleInstallments()).thenReturn(installments);
        doAnswer(invocation -> installments.add(invocation.getArgument(0))).when(loan).addLoanRepaymentScheduleInstallment(any());
        when(loanCharge.getLoanInstallmentCharge()).thenReturn(installmentChargesOfLoanCharge);
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    @Test
    void scheduleModelRemovesTheChargesOfDroppedAndRecreatedInstallments() {
        final LoanRepaymentScheduleInstallment kept = installment(1);
        final LoanInstallmentCharge chargeOfKept = charge(kept);
        final LoanRepaymentScheduleInstallment dropped = installment(2);
        final LoanInstallmentCharge chargeOfDropped = charge(dropped);
        installments.addAll(List.of(kept, dropped));

        final LoanScheduleModelPeriod period = mock(LoanScheduleModelPeriod.class);
        when(period.isRepaymentPeriod()).thenReturn(true);
        when(period.periodNumber()).thenReturn(1);
        when(period.periodDueDate()).thenReturn(DUE_DATE);
        final LoanScheduleModel model = mock(LoanScheduleModel.class);
        when(model.getPeriods()).thenReturn(List.of(period));

        component.updateLoanSchedule(loan, model);

        assertThat(installments).containsExactly(kept);
        assertThat(kept.getInstallmentCharges()).isEmpty();
        assertThat(dropped.getInstallmentCharges()).isEmpty();
        assertThat(installmentChargesOfLoanCharge).doesNotContain(chargeOfKept, chargeOfDropped).isEmpty();
    }

    @Test
    void installmentsRemoveTheChargesOfDroppedAndReplacedInstallments() {
        final LoanRepaymentScheduleInstallment kept = installment(1);
        final LoanInstallmentCharge replaced = charge(kept);
        final LoanRepaymentScheduleInstallment dropped = installment(2);
        final LoanInstallmentCharge chargeOfDropped = charge(dropped);
        installments.addAll(List.of(kept, dropped));

        final LoanRepaymentScheduleInstallment regenerated = installment(1);
        final LoanInstallmentCharge replacement = charge(regenerated);

        component.updateLoanSchedule(loan, List.of(regenerated));

        assertThat(installments).containsExactly(kept);
        assertThat(kept.getInstallmentCharges()).containsExactly(replacement);
        assertThat(dropped.getInstallmentCharges()).isEmpty();
        assertThat(installmentChargesOfLoanCharge).containsExactly(replacement).doesNotContain(replaced, chargeOfDropped);
    }

    @Test
    void installmentsKeepTheChargesOfInstallmentsTheyAdd() {
        final LoanRepaymentScheduleInstallment kept = installment(1);
        installments.add(kept);
        final LoanRepaymentScheduleInstallment added = installment(2);
        final LoanInstallmentCharge chargeOfAdded = charge(added);

        component.updateLoanSchedule(loan, List.of(installment(1), added));

        assertThat(installments).containsExactly(kept, added);
        assertThat(added.getInstallmentCharges()).containsExactly(chargeOfAdded);
        assertThat(installmentChargesOfLoanCharge).containsExactly(chargeOfAdded);
    }

    private LoanRepaymentScheduleInstallment installment(final int number) {
        return new LoanRepaymentScheduleInstallment(loan, number, DUE_DATE.minusMonths(1), DUE_DATE.plusMonths(number - 1L),
                BigDecimal.valueOf(100), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false);
    }

    private LoanInstallmentCharge charge(final LoanRepaymentScheduleInstallment installment) {
        final LoanInstallmentCharge installmentCharge = new LoanInstallmentCharge(BigDecimal.TEN, loanCharge, installment);
        installment.getInstallmentCharges().add(installmentCharge);
        installmentChargesOfLoanCharge.add(installmentCharge);
        return installmentCharge;
    }
}
