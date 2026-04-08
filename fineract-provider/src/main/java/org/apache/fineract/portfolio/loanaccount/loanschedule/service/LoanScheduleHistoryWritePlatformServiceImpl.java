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
package org.apache.fineract.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistoryRepository;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoanScheduleHistoryWritePlatformServiceImpl implements LoanScheduleHistoryWritePlatformService {

    private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
    private final LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository;

    private final PlatformSecurityContext platformSecurityContext;

    @Override
    public List<LoanRepaymentScheduleHistory> createLoanScheduleArchive(
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Loan loan, LoanRescheduleRequest loanRescheduleRequest) {
        Integer version = this.loanScheduleHistoryReadPlatformService.fetchCurrentVersionNumber(loan.getId()) + 1;
        final MonetaryCurrency currency = loan.getCurrency();
        final List<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = new ArrayList<>();

        for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : repaymentScheduleInstallments) {
            final Integer installmentNumber = repaymentScheduleInstallment.getInstallmentNumber();
            LocalDate fromDate = null;
            LocalDate dueDate = null;

            if (repaymentScheduleInstallment.getFromDate() != null) {
                fromDate = repaymentScheduleInstallment.getFromDate();
            }

            if (repaymentScheduleInstallment.getDueDate() != null) {
                dueDate = repaymentScheduleInstallment.getDueDate();
            }

            final BigDecimal principal = repaymentScheduleInstallment.getPrincipal(currency).getAmount();
            final BigDecimal interestCharged = repaymentScheduleInstallment.getInterestCharged(currency).getAmount();
            final BigDecimal feeChargesCharged = repaymentScheduleInstallment.getFeeChargesCharged(currency).getAmount();
            final BigDecimal penaltyCharges = repaymentScheduleInstallment.getPenaltyChargesCharged(currency).getAmount();

            Map<String, Object> oldDates = null;
            OffsetDateTime createdOnDate = DateUtils.getAuditOffsetDateTime();
            LocalDateTime oldCreatedOnDate = null;
            LocalDateTime oldLastModifiedOnDate = null;
            if (repaymentScheduleInstallment.getCreatedDate().isPresent()) {
                createdOnDate = repaymentScheduleInstallment.getCreatedDate().get();
            } else if (repaymentScheduleInstallment.getId() != null) {
                oldDates = loanScheduleHistoryReadPlatformService.fetchOldAuditDates(repaymentScheduleInstallment.getId());
                oldCreatedOnDate = (LocalDateTime) oldDates.get("created_date");
                oldLastModifiedOnDate = (LocalDateTime) oldDates.get("lastmodified_date");
            }

            final Long createdByUser = repaymentScheduleInstallment.getCreatedBy()
                    .orElse(platformSecurityContext.authenticatedUser().getId());
            final Long lastModifiedByUser = repaymentScheduleInstallment.getLastModifiedBy()
                    .orElse(platformSecurityContext.authenticatedUser().getId());

            OffsetDateTime lastModifiedOnDate = DateUtils.getAuditOffsetDateTime();
            if (repaymentScheduleInstallment.getLastModifiedDate().isPresent()) {
                lastModifiedOnDate = repaymentScheduleInstallment.getLastModifiedDate().get();
            } else if (repaymentScheduleInstallment.getId() != null && oldDates == null) {
                oldDates = loanScheduleHistoryReadPlatformService.fetchOldAuditDates(repaymentScheduleInstallment.getId());
                oldCreatedOnDate = (LocalDateTime) oldDates.get("created_date");
                oldLastModifiedOnDate = (LocalDateTime) oldDates.get("lastmodified_date");
            }

            LoanRepaymentScheduleHistory loanRepaymentScheduleHistory = LoanRepaymentScheduleHistory.instance(loan, loanRescheduleRequest,
                    installmentNumber, fromDate, dueDate, principal, interestCharged, feeChargesCharged, penaltyCharges, oldCreatedOnDate,
                    createdByUser, lastModifiedByUser, oldLastModifiedOnDate, version, createdOnDate, lastModifiedOnDate);

            loanRepaymentScheduleHistoryList.add(loanRepaymentScheduleHistory);
        }
        return loanRepaymentScheduleHistoryList;
    }

    @Override
    public void createAndSaveLoanScheduleArchive(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Loan loan,
            LoanRescheduleRequest loanRescheduleRequest) {
        List<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = createLoanScheduleArchive(repaymentScheduleInstallments, loan,
                loanRescheduleRequest);
        this.loanRepaymentScheduleHistoryRepository.saveAll(loanRepaymentScheduleHistoryList);

    }

}
