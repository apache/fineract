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
package org.apache.fineract.cob.loan;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplyChargeToOverdueLoansBusinessStepTest {

    private LoanReadPlatformService loanReadPlatformService;
    private LoanChargeWritePlatformService loanChargeWritePlatformService;
    private ApplyChargeToOverdueLoansBusinessStep applyChargeToOverdueLoansBusinessStep;

    @BeforeEach
    public void setUp() {
        loanReadPlatformService = mock(LoanReadPlatformService.class);
        loanChargeWritePlatformService = mock(LoanChargeWritePlatformService.class);
        applyChargeToOverdueLoansBusinessStep = new ApplyChargeToOverdueLoansBusinessStep(loanReadPlatformService,
                loanChargeWritePlatformService);
    }

    @Test
    public void testExecute_WhenNoOverdueInstallments_ShouldNotApplyCharges() {
        Loan loan = mock(Loan.class);

        when(loanReadPlatformService.retrieveAllOverdueInstallmentsForLoan(loan)).thenReturn(Collections.emptyList());

        Loan result = applyChargeToOverdueLoansBusinessStep.execute(loan);

        assertNotNull(result);
        verify(loanChargeWritePlatformService, never()).applyOverdueChargesForLoan(anyLong(), anyCollection());
    }

    @Test
    public void testExecute_WhenOverdueInstallmentsExist_ShouldApplyCharges() {
        Long testId = 1L;
        Loan loan = mock(Loan.class);
        OverdueLoanScheduleData overdueData = mock(OverdueLoanScheduleData.class);
        List<OverdueLoanScheduleData> overdueList = Collections.singletonList(overdueData);

        when(loan.getId()).thenReturn(testId);
        when(loanReadPlatformService.retrieveAllOverdueInstallmentsForLoan(loan)).thenReturn(overdueList);

        Loan result = applyChargeToOverdueLoansBusinessStep.execute(loan);

        assertNotNull(result);
        verify(loanChargeWritePlatformService, times(1)).applyOverdueChargesForLoan(testId, overdueList);
    }
}
