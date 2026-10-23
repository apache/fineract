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
package org.apache.fineract.infrastructure.jobs.service.retainedearning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetainedEarningConfigurationServiceTest {

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @InjectMocks
    private RetainedEarningConfigurationService retainedEarningConfigurationService;

    @Test
    void shouldCalculateLastDayOfPreviousFiscalYear() {
        when(configurationDomainService.getLastDayOfFinancialYear()).thenReturn(31L);
        when(configurationDomainService.getLastMonthOfFinancialYear()).thenReturn(12L);

        LocalDate result = retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(LocalDate.of(2025, 1, 1));

        assertEquals(LocalDate.of(2024, 12, 31), result);
    }

    @Test
    void shouldCalculateFiscalYearEndForSameYearFiscalYearEnd() {
        when(configurationDomainService.getLastDayOfFinancialYear()).thenReturn(31L);
        when(configurationDomainService.getLastMonthOfFinancialYear()).thenReturn(3L);

        LocalDate result = retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(LocalDate.of(2025, 6, 15));

        assertEquals(LocalDate.of(2025, 3, 31), result);
    }

    @Test
    void shouldCalculateFiscalYearEndForNonDecemberFiscalYear() {
        when(configurationDomainService.getLastDayOfFinancialYear()).thenReturn(31L);
        when(configurationDomainService.getLastMonthOfFinancialYear()).thenReturn(3L);

        LocalDate result = retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(LocalDate.of(2025, 3, 30));

        assertEquals(LocalDate.of(2024, 3, 31), result);
    }

    @Test
    void shouldDelegateIncomeExpenseGlAccountStart() {
        when(configurationDomainService.getIncomeExpenseGlAccounts()).thenReturn("400000-899999");

        assertEquals("400000-899999", retainedEarningConfigurationService.getIncomeExpenseGlAccounts());
    }

    @Test
    void shouldReturnDefaultReportNameWhenNotConfigured() {
        when(configurationDomainService.getRetainedEarningUsedByReportName()).thenReturn(null);

        assertEquals(RetainedEarningJobConstant.TRIAL_BALANCE_SUMMARY_WITH_ASSET_OWNER,
                retainedEarningConfigurationService.getReportName());
    }

    @Test
    void shouldReturnConfiguredReportName() {
        when(configurationDomainService.getRetainedEarningUsedByReportName()).thenReturn("Custom Report");

        assertEquals("Custom Report", retainedEarningConfigurationService.getReportName());
    }

    @Test
    void shouldReturnDefaultWorkingCapitalReportNameWhenNotConfigured() {
        when(configurationDomainService.getRetainedEarningWorkingCapitalUsedByReportName()).thenReturn(null);

        assertEquals(RetainedEarningJobConstant.TRIAL_BALANCE_SUMMARY_FOR_WORKING_CAPITAL_LOANS,
                retainedEarningConfigurationService.getWorkingCapitalReportName());
    }

    @Test
    void shouldReturnConfiguredWorkingCapitalReportName() {
        when(configurationDomainService.getRetainedEarningWorkingCapitalUsedByReportName()).thenReturn("Custom WC Report");

        assertEquals("Custom WC Report", retainedEarningConfigurationService.getWorkingCapitalReportName());
    }
}
