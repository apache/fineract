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
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetLoanDelinquencyTagsBusinessStep implements LoanCOBBusinessStep {

    private final LoanAccountDomainService loanAccountDomainService;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public Loan execute(Loan loan) {
        if (loan == null) {
            log.debug("Ignoring delinquency tag processing for null loan.");
            return null;
        }

        String externalId = Optional.ofNullable(loan.getExternalId()).map(ExternalId::getValue).orElse(null);
        measure(new Runnable() {

            @SuppressFBWarnings("SLF4J_MANUALLY_PROVIDED_MESSAGE")
            @Override
            public void run() {
                try {
                    log.debug("Starting delinquency tag processing for loan with Id [{}], account number [{}], external Id [{}]",
                            loan.getId(), loan.getAccountNumber(), externalId);

                    // Change the Action Context to DEFAULT for Business Date so that we can compare the loan due date
                    // to
                    // the
                    // current date and not the previous (COB) date.
                    ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);

                    final List<LoanDelinquencyAction> savedDelinquencyList = delinquencyReadPlatformService
                            .retrieveLoanDelinquencyActions(loan.getId());
                    List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                            .calculateEffectiveDelinquencyList(savedDelinquencyList);

                    if (!SetLoanDelinquencyTagsBusinessStep.this.isDelinquencyOnPause(loan, effectiveDelinquencyList)) {
                        loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate(), effectiveDelinquencyList);
                    } else {
                        log.debug("Delinquency is on pause for loan with ID [{}]", loan.getId());
                    }
                } catch (RuntimeException re) {
                    log.error(
                            "Received [{}] exception while processing delinquency tag for loan with Id [{}], account number [{}], external Id [{}]",
                            re.getMessage(), loan.getId(), loan.getAccountNumber(), externalId, re);

                    throw re;
                } finally {
                    // Change the Action Context back to COB to resume COB steps.
                    ThreadLocalContextUtil.setActionContext(ActionContext.COB);
                }
            }
        }, duration -> {
            log.debug("Ending delinquency tag processing for loan with Id [{}], account number [{}], external Id [{}], finished in [{}]ms",
                    loan.getId(), loan.getAccountNumber(), externalId, duration.toMillis());
        });

        return loan;
    }

    private boolean isDelinquencyOnPause(Loan loan, List<LoanDelinquencyActionData> effectiveDelinquencyList) {
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        boolean isPaused = isPausedOnDate(businessDate, effectiveDelinquencyList);
        boolean wasPausedOneDayBefore = isPausedOnDate(businessDate.minusDays(1), effectiveDelinquencyList);
        if ((isPaused && !wasPausedOneDayBefore) || (!isPaused && wasPausedOneDayBefore)) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
        }
        return isPaused;
    }

    private static boolean isPausedOnDate(LocalDate date, List<LoanDelinquencyActionData> effectiveDelinquencyList) {
        return effectiveDelinquencyList.stream()
                .anyMatch(pausePeriod -> !pausePeriod.getStartDate().isAfter(date) && !pausePeriod.getEndDate().isBefore(date));
    }

    @Override
    public String getEnumStyledName() {
        return "LOAN_DELINQUENCY_CLASSIFICATION";
    }

    @Override
    public String getHumanReadableName() {
        return "Loan Delinquency Classification";
    }

}
