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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Verifies that a write-off zeroes every outstanding view of the loan -- the balance buckets AND each active charge's
 * remainder -- and that the undo restores both. The charge marking matters because the charge read API derives its
 * outstanding independently of the balance: without it, a written-off loan would report a zero balance while its
 * charges still showed the fee as owed.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanWriteOffDomainServiceTest {

    private static final LocalDate WRITE_OFF_DATE = LocalDate.of(2026, 1, 15);

    @Mock
    private WorkingCapitalLoanLifecycleStateMachine loanLifecycleStateMachine;

    @InjectMocks
    private WorkingCapitalLoanWriteOffDomainService domainService;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private AppUser user;

    private WorkingCapitalLoanBalance balance;
    private WorkingCapitalLoanCharge charge;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, WRITE_OFF_DATE)));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());

        balance = WorkingCapitalLoanBalance.createFor(loan);
        balance.setPrincipal(new BigDecimal("100"));
        balance.setFee(new BigDecimal("10"));
        balance.setFeePaid(new BigDecimal("4"));
        when(loan.getBalance()).thenReturn(balance);

        charge = new WorkingCapitalLoanCharge();
        charge.setAmount(new BigDecimal("10"));
        charge.setAmountPaid(new BigDecimal("4"));
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void writeOffMarksEachChargeRemainderAndZeroesTheBalance() {
        domainService.writeOff(loan, WRITE_OFF_DATE, user, null, List.of(charge));

        verify(loanLifecycleStateMachine).transition(WorkingCapitalLoanEvent.LOAN_WRITTEN_OFF, loan, WRITE_OFF_DATE);
        assertThat(balance.getPrincipalOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(balance.getFeeOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        // Only the unpaid remainder moves to the written-off bucket; what was paid stays paid.
        assertThat(charge.getAmountWrittenOff()).isEqualByComparingTo(new BigDecimal("6"));
        assertThat(charge.getAmountOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void undoWriteOffRestoresTheChargeAndBalanceOutstanding() {
        domainService.writeOff(loan, WRITE_OFF_DATE, user, null, List.of(charge));

        domainService.undoWriteOff(loan, List.of(charge));

        verify(loanLifecycleStateMachine).transition(WorkingCapitalLoanEvent.LOAN_WRITTEN_OFF_UNDO, loan, WRITE_OFF_DATE);
        assertThat(balance.getPrincipalOutstanding()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(balance.getFeeOutstanding()).isEqualByComparingTo(new BigDecimal("6"));
        assertThat(charge.getAmountWrittenOff()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(charge.getAmountOutstanding()).isEqualByComparingTo(new BigDecimal("6"));
    }
}
