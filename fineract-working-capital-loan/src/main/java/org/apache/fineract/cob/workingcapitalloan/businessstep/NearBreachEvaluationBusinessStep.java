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
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanNearBreachChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.NearBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNearBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNearBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanNearBreachEvaluationService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NearBreachEvaluationBusinessStep extends WorkingCapitalLoanCOBBusinessStep {

    private final WorkingCapitalLoanNearBreachEvaluationService nearBreachEvaluationService;
    private final WorkingCapitalLoanNearBreachActionRepository nearBreachActionRepository;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public WorkingCapitalLoan execute(final WorkingCapitalLoan loan) {
        if (!loan.isOpen()) {
            log.debug("Skipping near breach evaluation for WC loan {} - loan status is {}", loan.getId(), loan.getLoanStatus());
            return loan;
        }

        final WorkingCapitalLoanNearBreachAction latestAction = nearBreachActionRepository
                .findTopByWorkingCapitalLoanIdAndActionOrderByIdDesc(loan.getId(), NearBreachActionType.RESCHEDULE).orElse(null);
        if (!hasNearBreachConfiguration(loan, latestAction)) {
            log.debug("Skipping near breach evaluation for WC loan {} - no near breach configuration", loan.getId());
            return loan;
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (nearBreachEvaluationService.evaluateNearBreach(loan, latestAction, businessDate)) {
            businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanNearBreachChangeBusinessEvent(loan));
        }
        return loan;
    }

    private boolean hasNearBreachConfiguration(final WorkingCapitalLoan loan, final WorkingCapitalLoanNearBreachAction latestAction) {
        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        return details != null && (details.getNearBreach() != null || latestAction != null);
    }

    @Override
    public String getEnumStyledName() {
        return "WC_NEAR_BREACH_EVALUATION";
    }

    @Override
    public String getHumanReadableName() {
        return "WC Near Breach Evaluation";
    }
}
