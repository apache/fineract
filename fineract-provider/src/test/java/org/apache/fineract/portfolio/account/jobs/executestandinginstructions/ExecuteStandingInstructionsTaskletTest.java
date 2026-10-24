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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.jdbc.core.JdbcTemplate;

class ExecuteStandingInstructionsTaskletTest {

    private final StandingInstructionReadPlatformService readPlatformService = mock(StandingInstructionReadPlatformService.class);
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService = mock(
            AccountTransfersWritePlatformService.class);
    private final ExecuteStandingInstructionsTasklet tasklet = new ExecuteStandingInstructionsTasklet(readPlatformService,
            mock(JdbcTemplate.class), mock(DatabaseSpecificSQLGenerator.class), accountTransfersWritePlatformService);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void monthlyInstructionStartingInAShorterMonthRunsOnItsLastDay() throws Exception {
        givenBusinessDate(LocalDate.of(2026, 4, 30));
        givenActiveInstruction(PeriodFrequencyType.MONTHS, 1, LocalDate.of(2026, 4, 10), 31, 1);

        tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));

        assertThat(transferDate()).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    @Test
    void monthlyRecurrenceOnThe31stFallsOnTheLastDayOfEveryShorterMonth() {
        assertThat(dueDates(PeriodFrequencyType.MONTHS, 1, LocalDate.of(2026, 1, 5), 31, 1, LocalDate.of(2026, 12, 31))).containsExactly(
                LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 31), LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 31));
    }

    @Test
    void monthlyRecurrenceKeepsTheIntervalAcrossShorterMonths() {
        assertThat(dueDates(PeriodFrequencyType.MONTHS, 2, LocalDate.of(2026, 1, 15), 31, 1, LocalDate.of(2026, 12, 31))).containsExactly(
                LocalDate.of(2026, 1, 31), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 11, 30));
    }

    @Test
    void monthlyRecurrenceUsesTheLeapDayInFebruary() {
        assertThat(dueDates(PeriodFrequencyType.MONTHS, 1, LocalDate.of(2028, 2, 1), 30, 1, LocalDate.of(2028, 3, 31)))
                .containsExactly(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 30));
    }

    @Test
    void monthlyRecurrenceStartsNextMonthWhenTheDayIsBeforeValidFrom() {
        assertThat(dueDates(PeriodFrequencyType.MONTHS, 1, LocalDate.of(2026, 1, 20), 15, 1, LocalDate.of(2026, 3, 31)))
                .containsExactly(LocalDate.of(2026, 2, 15), LocalDate.of(2026, 3, 15));
    }

    @Test
    void yearlyRecurrenceOn29FebruaryFallsOn28FebruaryOutsideLeapYears() {
        assertThat(dueDates(PeriodFrequencyType.YEARS, 1, LocalDate.of(2028, 1, 1), 29, 2, LocalDate.of(2032, 12, 31))).containsExactly(
                LocalDate.of(2028, 2, 29), LocalDate.of(2029, 2, 28), LocalDate.of(2030, 2, 28), LocalDate.of(2031, 2, 28),
                LocalDate.of(2032, 2, 29));
    }

    @Test
    void yearlyRecurrenceDoesNotFailWhenValidFromIsInAShorterMonth() {
        assertThat(dueDates(PeriodFrequencyType.YEARS, 1, LocalDate.of(2026, 2, 10), 30, 3, LocalDate.of(2027, 12, 31)))
                .containsExactly(LocalDate.of(2026, 3, 30), LocalDate.of(2027, 3, 30));
    }

    @Test
    void weeklyRecurrenceIsUnchanged() {
        assertThat(dueDates(PeriodFrequencyType.WEEKS, 2, LocalDate.of(2026, 1, 1), 0, 0, LocalDate.of(2026, 2, 15)))
                .containsExactly(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 29), LocalDate.of(2026, 2, 12));
    }

    private List<LocalDate> dueDates(PeriodFrequencyType frequencyType, int interval, LocalDate validFrom, int recurrenceOnDay,
            int recurrenceOnMonth, LocalDate until) {
        List<LocalDate> dueDates = new ArrayList<>();
        for (LocalDate date = validFrom; !date.isAfter(until); date = date.plusDays(1)) {
            if (ExecuteStandingInstructionsTasklet.isDueOnDate(frequencyType, interval, validFrom, recurrenceOnDay, recurrenceOnMonth,
                    date)) {
                dueDates.add(date);
            }
        }
        return dueDates;
    }

    private void givenBusinessDate(LocalDate businessDate) {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));
    }

    private void givenActiveInstruction(PeriodFrequencyType frequencyType, int interval, LocalDate validFrom, int recurrenceOnDay,
            int recurrenceOnMonth) {
        StandingInstructionData data = mock(StandingInstructionData.class);
        PortfolioAccountData fromAccount = mock(PortfolioAccountData.class);
        PortfolioAccountData toAccount = mock(PortfolioAccountData.class);
        when(fromAccount.getId()).thenReturn(10L);
        when(toAccount.getId()).thenReturn(20L);
        when(data.getId()).thenReturn(1L);
        when(data.getName()).thenReturn("Monthly savings");
        when(data.getRecurrenceTypeEnum()).thenReturn(AccountTransferRecurrenceType.PERIODIC);
        when(data.getInstructionTypeEnum()).thenReturn(StandingInstructionType.FIXED);
        when(data.getRecurrenceFrequencyEnum()).thenReturn(frequencyType);
        when(data.getRecurrenceInterval()).thenReturn(interval);
        when(data.getValidFrom()).thenReturn(validFrom);
        when(data.getRecurrenceOnDay()).thenReturn(recurrenceOnDay);
        when(data.getRecurrenceOnMonth()).thenReturn(recurrenceOnMonth);
        when(data.getAmount()).thenReturn(new BigDecimal("100"));
        when(data.getFromAccountTypeEnum()).thenReturn(PortfolioAccountType.SAVINGS);
        when(data.getToAccountTypeEnum()).thenReturn(PortfolioAccountType.SAVINGS);
        when(data.getFromAccount()).thenReturn(fromAccount);
        when(data.getToAccount()).thenReturn(toAccount);
        when(data.getTransferTypeEnum()).thenReturn(AccountTransferType.ACCOUNT_TRANSFER);
        when(readPlatformService.retrieveAll(anyInt())).thenReturn(List.of(data));
    }

    private LocalDate transferDate() {
        ArgumentCaptor<AccountTransferDTO> transfer = ArgumentCaptor.forClass(AccountTransferDTO.class);
        verify(accountTransfersWritePlatformService).transferFunds(transfer.capture());
        return transfer.getValue().getTransactionDate();
    }
}
