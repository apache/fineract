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
package org.apache.fineract.portfolio.savings.jobs.payduesavingscharges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.savings.data.SavingsAccountAnnualFeeData;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

class PayDueSavingsChargesTaskletTest {

    private static final LocalDate INSTALLMENT_DUE_DATE = LocalDate.of(2026, 9, 1);

    private SavingsAccountChargeReadPlatformService readPlatformService;
    private SavingsAccountWritePlatformService writePlatformService;
    private PayDueSavingsChargesTasklet tasklet;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        readPlatformService = mock(SavingsAccountChargeReadPlatformService.class);
        writePlatformService = mock(SavingsAccountWritePlatformService.class);
        tasklet = new PayDueSavingsChargesTasklet(readPlatformService, writePlatformService);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldContinueProcessingAndReportChargeContextWhenOneChargeFailsUnexpectedly() {
        SavingsAccountAnnualFeeData failingCharge = charge(11L, 101L, "SAV-101");
        SavingsAccountAnnualFeeData successfulCharge = charge(22L, 202L, "SAV-202");
        IllegalStateException failure = new IllegalStateException("Unexpected persistence failure");
        when(readPlatformService.retrieveChargesWithDue()).thenReturn(List.of(failingCharge, successfulCharge));
        when(writePlatformService.applyChargeDue(11L, 101L)).thenThrow(failure);

        JobExecutionException exception = catchThrowableOfType(JobExecutionException.class,
                () -> tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class)));

        assertThat(exception).hasMessageContaining("charge 11").hasMessageContaining("account 101").hasMessageContaining("SAV-101")
                .hasMessageContaining(INSTALLMENT_DUE_DATE.toString());
        assertThat(exception.getCauses()).singleElement().satisfies(contextualFailure -> assertThat(contextualFailure).hasCause(failure));

        InOrder processingOrder = inOrder(writePlatformService);
        processingOrder.verify(writePlatformService).applyChargeDue(11L, 101L);
        processingOrder.verify(writePlatformService).applyChargeDue(22L, 202L);
    }

    @Test
    void shouldFinishWhenAllDueChargesAreCollected() throws Exception {
        when(readPlatformService.retrieveChargesWithDue()).thenReturn(List.of(charge(11L, 101L, "SAV-101"), charge(22L, 202L, "SAV-202")));

        RepeatStatus result = tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));

        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void shouldCountUnpaidRecurringInstallmentAndFinishWhenLaterInstallmentHasInsufficientBalance() throws Exception {
        SavingsAccountAnnualFeeData recurringCharge = charge(11L, 101L, "SAV-101");
        SavingsAccountAnnualFeeData otherCharge = charge(22L, 202L, "SAV-202");
        InsufficientAccountBalanceException failure = new InsufficientAccountBalanceException("transactionAmount", BigDecimal.ZERO, null,
                BigDecimal.TEN);
        when(readPlatformService.retrieveChargesWithDue()).thenReturn(List.of(recurringCharge, otherCharge));
        when(writePlatformService.applyChargeDue(11L, 101L)).thenReturn(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 8))
                .thenThrow(failure);
        StepContribution contribution = mock(StepContribution.class);

        RepeatStatus result = tasklet.execute(contribution, mock(ChunkContext.class));

        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
        verify(contribution).incrementWriteSkipCount();
        InOrder processingOrder = inOrder(writePlatformService);
        processingOrder.verify(writePlatformService, times(3)).applyChargeDue(11L, 101L);
        processingOrder.verify(writePlatformService).applyChargeDue(22L, 202L);
    }

    private SavingsAccountAnnualFeeData charge(Long chargeId, Long accountId, String accountNumber) {
        return SavingsAccountAnnualFeeData.instance(chargeId, accountId, accountNumber, INSTALLMENT_DUE_DATE);
    }
}
