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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanNearBreachChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanNearBreachEvaluationService.NearBreachParameters;
import org.springframework.stereotype.Component;

/**
 * Keeps the near breach of the breach schedule in step with the monetary events and schedule rebuilds between two COB
 * runs, and raises the near breach change event when a value moved.
 */
@Component
@RequiredArgsConstructor
class WorkingCapitalLoanNearBreachRederivation {

    private final WorkingCapitalLoanNearBreachEvaluationService nearBreachEvaluationService;
    private final BusinessEventNotifierService businessEventNotifierService;

    /**
     * Re-derives the near breach of {@code period} when it is still open. The caller has already established that the
     * breach evaluation of the loan is enabled.
     */
    void rederiveOpenPeriod(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachSchedule period) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (period.getToDate().isBefore(businessDate)) {
            return;
        }
        final Optional<NearBreachParameters> parameters = nearBreachEvaluationService.resolveParametersWithBreachEvaluationEnabled(loan);
        if (parameters.isPresent() && nearBreachEvaluationService.rederiveNearBreach(List.of(period), parameters.get(),
                nearBreachEffectiveDate(businessDate))) {
            notifyNearBreachChanged(loan);
        }
    }

    /**
     * Re-derives the near breach of the periods that went stale since {@code baseline} was taken. The event is raised
     * when the evaluation ran and a period, or the open period of the loan, holds a different value than in the
     * baseline.
     */
    void rederiveSince(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanBreachSchedule> periods,
            final WorkingCapitalLoanNearBreachBaseline baseline, final boolean rederiveClosedValues) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        periods.forEach(baseline::carryOverToRegenerated);
        final Optional<NearBreachParameters> parameters = nearBreachEvaluationService.resolveParameters(loan);
        if (parameters.isEmpty()) {
            return;
        }
        final List<WorkingCapitalLoanBreachSchedule> stale = periods.stream()
                .filter(period -> baseline.isStale(period, businessDate, rederiveClosedValues)).toList();
        nearBreachEvaluationService.rederiveNearBreach(stale, parameters.get(), nearBreachEffectiveDate(businessDate));
        if (baseline.hasChanged(periods, businessDate)) {
            notifyNearBreachChanged(loan);
        }
    }

    // A checkpoint counts once its day has closed, so outside COB the near breach is judged as of the latest COB date.
    private static LocalDate nearBreachEffectiveDate(final LocalDate businessDate) {
        return businessDate.minusDays(1);
    }

    private void notifyNearBreachChanged(final WorkingCapitalLoan loan) {
        businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanNearBreachChangeBusinessEvent(loan));
    }
}
