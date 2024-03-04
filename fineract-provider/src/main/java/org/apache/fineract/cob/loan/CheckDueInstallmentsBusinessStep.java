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

import static org.apache.fineract.infrastructure.core.diagnostics.performance.MeasuringUtil.measure;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountCustomSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckDueInstallmentsBusinessStep implements LoanCOBBusinessStep {

    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public Loan execute(Loan loan) {
        if (loan == null) {
            log.debug("Ignoring custom snapshot event processing for null loan.");
            return null;
        }

        String externalId = Optional.ofNullable(loan.getExternalId()).map(ExternalId::getValue).orElse(null);
        measure(new Runnable() {

            @SuppressFBWarnings("SLF4J_MANUALLY_PROVIDED_MESSAGE")
            @Override
            public void run() {
                try {
                    log.debug("Starting custom snapshot event processing for loan with id [{}], account number [{}], external Id [{}].",
                            loan.getId(), loan.getAccountNumber(), externalId);

                    if (loan.getRepaymentScheduleInstallments() != null && loan.getRepaymentScheduleInstallments().size() > 0) {
                        final LocalDate currentDate = DateUtils.getBusinessLocalDate();
                        boolean shouldPostCustomSnapshotBusinessEvent = false;
                        for (int i = 0; i < loan.getRepaymentScheduleInstallments().size(); i++) {
                            if (loan.getRepaymentScheduleInstallments().get(i).getDueDate().equals(currentDate)
                                    && loan.getRepaymentScheduleInstallments().get(i).isNotFullyPaidOff()) {
                                shouldPostCustomSnapshotBusinessEvent = true;
                            }
                        }

                        if (shouldPostCustomSnapshotBusinessEvent) {
                            // Change the Action Context to DEFAULT for Business Date so that we can compare the loan
                            // due date
                            // to the current date and not the previous (COB) date when calculation collection data.
                            ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
                            businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountCustomSnapshotBusinessEvent(loan));
                        }
                    }
                } catch (RuntimeException re) {
                    log.error(
                            "Received [{}] exception while processing custom snapshot event for loan with Id [{}], account number [{}], external Id [{}].",
                            re.getMessage(), loan.getId(), loan.getAccountNumber(), externalId, re);

                    throw re;
                } finally {
                    // Change the Action Context back to COB to resume COB steps.
                    ThreadLocalContextUtil.setActionContext(ActionContext.COB);
                }
            }
        }, duration -> {
            log.debug(
                    "Ending custom snapshot event processing for loan with Id [{}], account number [{}], external Id [{}], finished in [{}]ms.",
                    loan.getId(), loan.getAccountNumber(), externalId, duration.toMillis());
        });

        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "CHECK_DUE_INSTALLMENTS";
    }

    @Override
    public String getHumanReadableName() {
        return "Check Due Installments";
    }

}
