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
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAmortizationScheduleWriteService;
import org.springframework.stereotype.Component;

/**
 * Tells the projected amortization schedule that another day has gone by.
 *
 * <p>
 * The schedule only learns that time has passed from something happening to the loan - a payment, a rate change. A
 * borrower who simply stops paying produces no such event, so without this step the instalment dates they let go by are
 * left blank, indistinguishable from days that have not arrived yet. This fills them in as nil payments, so anyone
 * reading the schedule can see that nothing has come in for those days.
 *
 * <p>
 * It changes the actual columns only. A zeroed date is not a transaction and does not restate the plan; that happens
 * when money actually lands.
 *
 * <p>
 * Runs ahead of the steps that read the schedule, so they see one that is current as at today.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MissedPaymentAcknowledgementBusinessStep extends WorkingCapitalLoanCOBBusinessStep {

    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;

    @Override
    public WorkingCapitalLoan execute(final WorkingCapitalLoan input) {
        final boolean isDisbursed = input.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).anyMatch(Objects::nonNull);
        if (!isDisbursed) {
            log.debug("Skipping missed payment acknowledgement for WC loan {} - not yet disbursed", input.getId());
            return input;
        }

        // Not DateUtils.getBusinessLocalDate(): under the COB action context that returns the day before the business
        // date, which would leave the schedule perpetually one day short of the batch that is running it.
        final LocalDate businessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.BUSINESS_DATE);

        amortizationScheduleWriteService.acknowledgeElapsedPeriods(input, businessDate);

        return input;
    }

    @Override
    public String getEnumStyledName() {
        return "WC_MISSED_PAYMENT_ACKNOWLEDGEMENT";
    }

    @Override
    public String getHumanReadableName() {
        return "WC Missed Payment Acknowledgement";
    }
}
