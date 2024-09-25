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
package org.apache.fineract.portfolio.loanaccount.jobs.applychargetooverdueloaninstallment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class ApplyChargeToOverdueLoanInstallmentTaskletTest {

    private LoanReadPlatformService loanReadPlatformService;
    private LoanChargeWritePlatformService loanChargeWritePlatformService;
    private StepContribution contribution;
    private ChunkContext chunkContext;
    private ApplyChargeToOverdueLoanInstallmentTasklet tasklet;

    @BeforeEach
    public void setUp() {
        loanReadPlatformService = mock(LoanReadPlatformService.class);
        loanChargeWritePlatformService = mock(LoanChargeWritePlatformService.class);
        ConfigurationDomainService configurationDomainService = mock(ConfigurationDomainService.class);
        contribution = mock(StepContribution.class);
        chunkContext = mock(ChunkContext.class);

        tasklet = new ApplyChargeToOverdueLoanInstallmentTasklet(configurationDomainService, loanReadPlatformService,
                loanChargeWritePlatformService);
    }

    @Test
    public void testExecute_WhenNoOverdueInstallments_ShouldNotApplyCharges() throws Exception {
        when(loanReadPlatformService.retrieveAllLoansWithOverdueInstallments(anyLong(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);

        verify(loanChargeWritePlatformService, never()).applyOverdueChargesForLoan(anyLong(), anyCollection());
    }

    @Test
    public void testExecute_WhenOverdueInstallmentsExist_ShouldApplyCharges() throws Exception {
        OverdueLoanScheduleData overdueData = mock(OverdueLoanScheduleData.class);
        when(loanReadPlatformService.retrieveAllLoansWithOverdueInstallments(anyLong(), anyBoolean()))
                .thenReturn(Collections.singletonList(overdueData));

        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);

        verify(loanChargeWritePlatformService, times(1)).applyOverdueChargesForLoan(anyLong(), anyCollection());
    }
}
