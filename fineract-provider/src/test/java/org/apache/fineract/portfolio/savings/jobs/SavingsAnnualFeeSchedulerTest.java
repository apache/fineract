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
package org.apache.fineract.portfolio.savings.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.data.SavingsAccountAnnualFeeData;
import org.apache.fineract.portfolio.savings.jobs.applyannualfeeforsavings.ApplyAnnualFeeForSavingsTasklet;
import org.apache.fineract.portfolio.savings.jobs.payduesavingscharges.PayDueSavingsChargesTasklet;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

class SavingsAnnualFeeSchedulerTest {

    private final SavingsAccountChargeReadPlatformService readService = mock(SavingsAccountChargeReadPlatformService.class);
    private final SavingsAccountWritePlatformService writeService = mock(SavingsAccountWritePlatformService.class);
    private final SavingsAccountAnnualFeeData charge = SavingsAccountAnnualFeeData.instance(7L, 42L, "account", LocalDate.of(2025, 1, 1));

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "UTC", null));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void annualFeeJobStillCollectsExistingDueChargeThroughWriteService() throws Exception {
        when(readService.retrieveChargesWithAnnualFeeDue()).thenReturn(List.of(charge));
        assertThat(new ApplyAnnualFeeForSavingsTasklet(readService, writeService).execute(null, null)).isEqualTo(RepeatStatus.FINISHED);
        verify(writeService).applyAnnualFee(7L, 42L);
        verifyNoMoreInteractions(writeService);
    }

    @Test
    void dueChargesJobStillUsesExistingCollectionOperation() throws Exception {
        when(readService.retrieveChargesWithDue()).thenReturn(List.of(charge));
        assertThat(new PayDueSavingsChargesTasklet(readService, writeService).execute(null, null)).isEqualTo(RepeatStatus.FINISHED);
        verify(writeService).applyChargeDue(7L, 42L);
        verifyNoMoreInteractions(writeService);
    }
}
