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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.apache.fineract.portfolio.account.jobs.executestandinginstructions.StandingInstructionTestData.dueFixedSavingsToSavings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StandingInstructionItemProcessorTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 5, 1);

    @Mock
    private StandingInstructionReadPlatformService readService;

    private StandingInstructionItemProcessor processor;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(
                Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE, BusinessDateType.COB_DATE, BUSINESS_DATE.minusDays(1))));
        processor = new StandingInstructionItemProcessor(readService);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void aDueFixedInstructionCarriesItsMandatedAmount() {
        final DueStandingInstruction due = processor.process(dueFixedSavingsToSavings(1L));

        assertThat(due).isNotNull();
        assertThat(due.transactionAmount()).isEqualByComparingTo("100");
        assertThat(due.transactionDate()).isEqualTo(BUSINESS_DATE);
    }

    @Test
    void anInstructionWhoseRecurrenceDoesNotFallTodayIsFilteredOut() {
        final StandingInstructionData data = dueFixedSavingsToSavings(1L);
        // Yearly on 1 June, so 1 May is not a scheduled date.
        when(data.getRecurrenceFrequencyEnum()).thenReturn(PeriodFrequencyType.YEARS);
        when(data.getRecurrenceOnDay()).thenReturn(1);
        when(data.getRecurrenceOnMonth()).thenReturn(6);

        assertThat(processor.process(data)).isNull();
    }

    @Test
    void anInstructionWithNothingToTransferIsFilteredOut() {
        final StandingInstructionData data = dueFixedSavingsToSavings(1L);
        when(data.getAmount()).thenReturn(BigDecimal.ZERO);

        assertThat(processor.process(data)).isNull();
    }

    @Test
    void aDuesInstructionTakesItsAmountFromTheLoanItPays() {
        final StandingInstructionData data = dueFixedSavingsToSavings(1L);
        when(data.getToAccountTypeEnum()).thenReturn(PortfolioAccountType.LOAN);
        when(data.getInstructionTypeEnum()).thenReturn(StandingInstructionType.DUES);
        when(readService.retriveLoanDuesData(data.getToAccount().getId()))
                .thenReturn(new StandingInstructionDuesData(BUSINESS_DATE, new BigDecimal("42.50")));

        final DueStandingInstruction due = processor.process(data);

        assertThat(due).isNotNull();
        assertThat(due.transactionAmount()).isEqualByComparingTo("42.50");
    }

    /**
     * Whether a loan instalment is due is judged against the tenant's own date rather than the business date, which is
     * why these cases are stated relative to {@link DateUtils#getLocalDateOfTenant()}.
     */
    @Test
    void aDuesRecurrenceIsPayableOnceTheLoanInstalmentIsDue() {
        final StandingInstructionData data = dueFixedSavingsToSavings(1L);
        when(data.getRecurrenceTypeEnum()).thenReturn(AccountTransferRecurrenceType.AS_PER_DUES);
        when(data.getToAccountTypeEnum()).thenReturn(PortfolioAccountType.LOAN);
        when(readService.retriveLoanDuesData(data.getToAccount().getId()))
                .thenReturn(new StandingInstructionDuesData(DateUtils.getLocalDateOfTenant().minusDays(2), new BigDecimal("10")));

        assertThat(processor.process(data)).isNotNull();
    }

    @Test
    void aDuesRecurrenceIsNotPayableBeforeTheLoanInstalmentIsDue() {
        final StandingInstructionData data = dueFixedSavingsToSavings(1L);
        when(data.getRecurrenceTypeEnum()).thenReturn(AccountTransferRecurrenceType.AS_PER_DUES);
        when(data.getToAccountTypeEnum()).thenReturn(PortfolioAccountType.LOAN);
        when(readService.retriveLoanDuesData(data.getToAccount().getId()))
                .thenReturn(new StandingInstructionDuesData(DateUtils.getLocalDateOfTenant().plusDays(2), new BigDecimal("10")));

        assertThat(processor.process(data)).isNull();
    }

    @Test
    void anInstalmentDueEarlierIsStillAccepted() {
        assertThat(
                processor.isDueForTransfer(new StandingInstructionDuesData(DateUtils.getLocalDateOfTenant().minusDays(2), BigDecimal.ONE)))
                .isTrue();
    }

    @Test
    void anInstalmentDueTodayIsAccepted() {
        assertThat(processor.isDueForTransfer(new StandingInstructionDuesData(DateUtils.getLocalDateOfTenant(), BigDecimal.ONE))).isTrue();
    }
}
