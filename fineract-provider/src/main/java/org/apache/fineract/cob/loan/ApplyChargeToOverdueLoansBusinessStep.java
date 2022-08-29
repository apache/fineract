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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplyChargeToOverdueLoansBusinessStep implements LoanCOBBusinessStep {

    private final ConfigurationDomainService configurationDomainService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanWritePlatformService loanWritePlatformService;

    @Override
    public Loan execute(Loan input) {
        final Long penaltyWaitPeriodValue = configurationDomainService.retrievePenaltyWaitPeriod();
        final Boolean backdatePenalties = configurationDomainService.isBackdatePenaltiesEnabled();
        final Collection<OverdueLoanScheduleData> overdueLoanScheduledInstallments = loanReadPlatformService
                .retrieveAllLoansWithOverdueInstallments(penaltyWaitPeriodValue, backdatePenalties);
        // TODO: this is very much not effective to get all overdue installments for each loan, a new method needs to be
        // implemented for it
        Map<Long, List<OverdueLoanScheduleData>> groupedOverdueData = overdueLoanScheduledInstallments.stream()
                .collect(Collectors.groupingBy(OverdueLoanScheduleData::getLoanId));
        for (Long loanId : groupedOverdueData.keySet()) {
            loanWritePlatformService.applyOverdueChargesForLoan(input.getId(), groupedOverdueData.get(loanId));
        }
        return input;
    }

    @Override
    public String getEnumStyledName() {
        return "APPLY_CHARGE_TO_OVERDUE_LOANS";
    }

    @Override
    public String getHumanReadableName() {
        return "Apply charge to overdue loans";
    }
}
