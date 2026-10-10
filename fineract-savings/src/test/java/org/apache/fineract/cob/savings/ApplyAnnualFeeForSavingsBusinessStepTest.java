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
package org.apache.fineract.cob.savings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplyAnnualFeeForSavingsBusinessStepTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 6, 1);
    private static final Long ACCOUNT_ID = 42L;

    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private SavingsAccount savingsAccount;
    @InjectMocks
    private ApplyAnnualFeeForSavingsBusinessStep underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        lenient().when(savingsAccount.getId()).thenReturn(ACCOUNT_ID);
        // eligible by default; the non-active case overrides it
        lenient().when(savingsAccount.isActive()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldApplyAnnualFeeDueBeforeBusinessDate() {
        // due 2024-05-31, business date 2024-06-01
        givenCharges(charge(1L, true, true, true, BUSINESS_DATE.minusDays(1)));

        final SavingsAccount result = underTest.execute(savingsAccount);

        verify(savingsAccountWritePlatformService).applyAnnualFee(1L, ACCOUNT_ID);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldApplyAnnualFeeDueOnBusinessDate() {
        // due 2024-06-01, business date 2024-06-01: inclusive boundary
        givenCharges(charge(1L, true, true, true, BUSINESS_DATE));

        underTest.execute(savingsAccount);

        verify(savingsAccountWritePlatformService).applyAnnualFee(1L, ACCOUNT_ID);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
    }

    @Test
    void shouldNotApplyAnnualFeeDueAfterBusinessDate() {
        // due 2024-06-02, business date 2024-06-01
        givenCharges(charge(1L, true, true, true, BUSINESS_DATE.plusDays(1)));

        final SavingsAccount result = underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldNotApplyInactiveAnnualFee() {
        givenCharges(charge(1L, true, false, true, BUSINESS_DATE.minusDays(1)));

        underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
    }

    @Test
    void shouldNotApplyFullyPaidAnnualFee() {
        givenCharges(charge(1L, true, true, false, BUSINESS_DATE.minusDays(1)));

        underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
    }

    @Test
    void shouldNotApplyAnnualFeeWithoutDueDate() {
        givenCharges(charge(1L, true, true, true, null));

        underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
    }

    @Test
    void shouldNotApplyNonAnnualFeeCharge() {
        // e.g. a monthly fee due 2024-05-31 is handled by the pay-due-charges step, not here
        givenCharges(charge(1L, false, true, true, BUSINESS_DATE.minusDays(1)));

        underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
    }

    @Test
    void shouldApplyOnlyEligibleChargesAmongSeveral() {
        givenCharges(charge(1L, true, true, true, BUSINESS_DATE.minusDays(10)), // eligible
                charge(2L, true, true, true, BUSINESS_DATE.plusDays(10)), // not yet due
                charge(3L, false, true, true, BUSINESS_DATE.minusDays(10)), // not an annual fee
                charge(4L, true, true, true, BUSINESS_DATE)); // eligible, due today

        underTest.execute(savingsAccount);

        verify(savingsAccountWritePlatformService).applyAnnualFee(1L, ACCOUNT_ID);
        verify(savingsAccountWritePlatformService).applyAnnualFee(4L, ACCOUNT_ID);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
    }

    @Test
    void shouldNotApplyAnnualFeeOnNonActiveAccount() {
        // e.g. an account approved but not yet activated, holding an annual fee due 2024-05-31
        when(savingsAccount.isActive()).thenReturn(false);
        // built outside thenReturn(..) to avoid nested stubbing
        final SavingsAccountCharge eligibleCharge = charge(1L, true, true, true, BUSINESS_DATE.minusDays(1));
        lenient().when(savingsAccount.charges()).thenReturn(new LinkedHashSet<>(List.of(eligibleCharge)));

        final SavingsAccount result = underTest.execute(savingsAccount);

        verify(savingsAccount, never()).charges();
        verifyNoInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldDoNothingWhenAccountHasNoCharges() {
        givenCharges();

        final SavingsAccount result = underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldExposeStepNames() {
        assertThat(underTest.getEnumStyledName()).isEqualTo("APPLY_ANNUAL_FEE_FOR_SAVINGS");
        assertThat(underTest.getHumanReadableName()).isEqualTo("Apply annual fee for savings");
    }

    private void givenCharges(SavingsAccountCharge... charges) {
        when(savingsAccount.charges()).thenReturn(new LinkedHashSet<>(List.of(charges)));
    }

    // Stubs are lenient because the step short-circuits its condition, so not every property is read for every case.
    private static SavingsAccountCharge charge(Long id, boolean annualFee, boolean active, boolean notFullyPaid, LocalDate dueDate) {
        final SavingsAccountCharge charge = mock(SavingsAccountCharge.class);
        lenient().when(charge.getId()).thenReturn(id);
        lenient().when(charge.isAnnualFee()).thenReturn(annualFee);
        lenient().when(charge.isActive()).thenReturn(active);
        lenient().when(charge.isNotFullyPaid()).thenReturn(notFullyPaid);
        lenient().when(charge.getDueDate()).thenReturn(dueDate);
        return charge;
    }
}
