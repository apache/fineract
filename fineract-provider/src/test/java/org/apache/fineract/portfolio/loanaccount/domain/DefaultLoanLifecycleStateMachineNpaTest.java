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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * A loan that is settled while NPA leaves NPA in the same transaction.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultLoanLifecycleStateMachineNpaTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 9, 12);

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanBalanceService loanBalanceService;

    private DefaultLoanLifecycleStateMachine underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        underTest = new DefaultLoanLifecycleStateMachine(businessEventNotifierService, loanBalanceService);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void repaidInFullClearsNpa() {
        final Loan loan = loan(LoanStatus.ACTIVE, true);

        underTest.transition(LoanEvent.REPAID_IN_FULL, loan);

        verify(loan).setLoanStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        verify(loan).clearNpa();
    }

    @Test
    void writeOffClearsNpa() {
        final Loan loan = loan(LoanStatus.ACTIVE, true);

        underTest.transition(LoanEvent.WRITE_OFF_OUTSTANDING, loan);

        verify(loan).setLoanStatus(LoanStatus.CLOSED_WRITTEN_OFF);
        verify(loan).clearNpa();
    }

    @Test
    void overpaymentClearsNpa() {
        final Loan loan = loan(LoanStatus.ACTIVE, true);

        underTest.transition(LoanEvent.LOAN_OVERPAYMENT, loan);

        verify(loan).setLoanStatus(LoanStatus.OVERPAID);
        verify(loan).clearNpa();
    }

    @Test
    void closingALoanThatIsNotNpaLeavesTheFlagAlone() {
        final Loan loan = loan(LoanStatus.ACTIVE, false);

        underTest.transition(LoanEvent.REPAID_IN_FULL, loan);

        verify(loan, never()).clearNpa();
    }

    @Test
    void reopeningALoanKeepsNpaUntouched() {
        final Loan loan = loan(LoanStatus.CLOSED_OBLIGATIONS_MET, true);

        underTest.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, loan);

        verify(loan).setLoanStatus(LoanStatus.ACTIVE);
        verify(loan, never()).clearNpa();
    }

    private Loan loan(final LoanStatus status, final boolean npa) {
        final Loan loan = mock(Loan.class);
        when(loan.getStatus()).thenReturn(status);
        when(loan.isNpa()).thenReturn(npa);
        return loan;
    }
}
