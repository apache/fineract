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

import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.TRIAL_BALANCE_SUMMARY_FOR_WORKING_CAPITAL_LOANS;
import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.TRIAL_BALANCE_SUMMARY_WITH_ASSET_OWNER;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.springframework.stereotype.Component;

/**
 * Job-specific configuration wrapper that isolates retained earning config access from the platform-wide
 * {@link ConfigurationDomainService}. Centralizes the fiscal year date calculation used by both the Reader and Writer.
 */
@Component
@RequiredArgsConstructor
public class RetainedEarningConfigurationService {

    private final ConfigurationDomainService configurationDomainService;

    public String getIncomeExpenseGlAccounts() {
        return configurationDomainService.getIncomeExpenseGlAccounts();
    }

    public String getRetainedEarningGlAccount() {
        return configurationDomainService.getRetainedEarningGlAccount();
    }

    public Long getOfficeId() {
        Long value = configurationDomainService.getOfficeId();
        if (value == null) {
            throw new PlatformInternalServerException("error.retained.earning.office.id.not.configured",
                    "Retained earning job office ID is not configured");
        }
        return value;
    }

    public String getReportName() {
        String configured = configurationDomainService.getRetainedEarningUsedByReportName();
        return configured != null ? configured : TRIAL_BALANCE_SUMMARY_WITH_ASSET_OWNER;
    }

    public String getWorkingCapitalReportName() {
        String configured = configurationDomainService.getRetainedEarningWorkingCapitalUsedByReportName();
        return configured != null ? configured : TRIAL_BALANCE_SUMMARY_FOR_WORKING_CAPITAL_LOANS;
    }

    public LocalDate getLastDayOfPreviousFiscalYear(LocalDate currentDate) {
        final int lastDay = configurationDomainService.getLastDayOfFinancialYear().intValue();
        final int lastMonth = configurationDomainService.getLastMonthOfFinancialYear().intValue();
        LocalDate fiscalEndThisYear = LocalDate.of(currentDate.getYear(), lastMonth, lastDay);
        return fiscalEndThisYear.isBefore(currentDate) ? fiscalEndThisYear : fiscalEndThisYear.minusYears(1);
    }
}
