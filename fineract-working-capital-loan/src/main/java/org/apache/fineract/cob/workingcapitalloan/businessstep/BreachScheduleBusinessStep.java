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
package org.apache.fineract.cob.workingcapitalloan.businessstep;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBreachChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBreachScheduleChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanBreachScheduleService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BreachScheduleBusinessStep extends WorkingCapitalLoanCOBBusinessStep {

    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public WorkingCapitalLoan execute(final WorkingCapitalLoan loan) {
        if (loan.isNotDisbursed()) {
            log.debug("Skipping breach schedule for WC loan {} - not yet disbursed", loan.getId());
            return loan;
        }

        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        if (details == null || details.getBreach() == null) {
            log.debug("Skipping breach schedule for WC loan {} - no breach configuration", loan.getId());
            return loan;
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();

        boolean scheduleChanged = false;
        if (!breachScheduleService.hasSchedule(loan.getId())) {
            scheduleChanged = breachScheduleService.generateInitialPeriod(loan);
        }

        scheduleChanged |= breachScheduleService.generateNextPeriodIfNeeded(loan, businessDate);
        final boolean breachFlagChanged = breachScheduleService.evaluateBreach(loan, businessDate);
        breachScheduleService.recalculatePastDueAmount(loan);

        if (scheduleChanged) {
            businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanBreachScheduleChangedBusinessEvent(loan));
        }
        if (breachFlagChanged) {
            businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanBreachChangeBusinessEvent(loan));
        }

        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "WC_BREACH_SCHEDULE";
    }

    @Override
    public String getHumanReadableName() {
        return "WC Breach Schedule";
    }
}
