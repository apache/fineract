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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.repayment.LoanRepaymentDueBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckLoanRepaymentDueBusinessStep implements LoanCOBBusinessStep {

    private final ConfigurationDomainService configurationDomainService;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public Loan execute(Loan loan) {
        log.debug("start processing loan repayment due business step loan for loan with id [{}]", loan.getId());
        Long numberOfDaysBeforeDueDateToRaiseEvent = configurationDomainService.retrieveRepaymentDueDays();
        if (loan.getLoanProduct().getDueDaysForRepaymentEvent() != null) {
            if (loan.getLoanProduct().getDueDaysForRepaymentEvent() > 0) {
                numberOfDaysBeforeDueDateToRaiseEvent = loan.getLoanProduct().getDueDaysForRepaymentEvent().longValue();
            }
        }
        final LocalDate currentDate = DateUtils.getBusinessLocalDate();
        final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment repaymentSchedule : loanRepaymentScheduleInstallments) {
            LocalDate repaymentDate = repaymentSchedule.getDueDate();
            List<LoanStatus> nonDisbursedStatuses = Arrays.asList(LoanStatus.INVALID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL,
                    LoanStatus.APPROVED);
            if (isDueEventNeededToBeSent(loan, numberOfDaysBeforeDueDateToRaiseEvent, currentDate, repaymentSchedule, repaymentDate,
                    nonDisbursedStatuses)) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanRepaymentDueBusinessEvent(repaymentSchedule));
                break;
            }
        }
        log.debug("end processing loan repayment due business step loan for loan with id [{}]", loan.getId());
        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "CHECK_LOAN_REPAYMENT_DUE";
    }

    @Override
    public String getHumanReadableName() {
        return "Check loan repayment due";
    }

    private static boolean isDueEventNeededToBeSent(Loan loan, Long numberOfDaysBeforeDueDateToRaiseEvent, LocalDate currentDate,
            LoanRepaymentScheduleInstallment repaymentScheduleInstallment, LocalDate repaymentDate, List<LoanStatus> nonDisbursedStatuses) {
        return repaymentDate.minusDays(numberOfDaysBeforeDueDateToRaiseEvent).equals(currentDate)
                && !nonDisbursedStatuses.contains(loan.getStatus())
                && loan.getSummary().getTotalOutstanding().compareTo(BigDecimal.ZERO) > 0
                && repaymentScheduleInstallment.getTotalOutstanding(loan.getCurrency()).isGreaterThanZero();
    }
}
