/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistoryRepository;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanScheduleHistoryWritePlatformServiceImpl implements LoanScheduleHistoryWritePlatformService {

    private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
    private final LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository;

    @Autowired
    public LoanScheduleHistoryWritePlatformServiceImpl(final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService,
            final LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository) {
        this.loanScheduleHistoryReadPlatformService = loanScheduleHistoryReadPlatformService;
        this.loanRepaymentScheduleHistoryRepository = loanRepaymentScheduleHistoryRepository;

    }

    @Override
    public List<LoanRepaymentScheduleHistory> createLoanScheduleArchive(
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Loan loan, LoanRescheduleRequest loanRescheduleRequest) {
        Integer version = this.loanScheduleHistoryReadPlatformService.fetchCurrentVersionNumber(loan.getId()) + 1;
        final MonetaryCurrency currency = loan.getCurrency();
        final List<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = new ArrayList<>();

        for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : repaymentScheduleInstallments) {
            final Integer installmentNumber = repaymentScheduleInstallment.getInstallmentNumber();
            Date fromDate = null;
            Date dueDate = null;

            if (repaymentScheduleInstallment.getFromDate() != null) {
                fromDate = repaymentScheduleInstallment.getFromDate().toDate();
            }

            if (repaymentScheduleInstallment.getDueDate() != null) {
                dueDate = repaymentScheduleInstallment.getDueDate().toDate();
            }

            final BigDecimal principal = repaymentScheduleInstallment.getPrincipal(currency).getAmount();
            final BigDecimal interestCharged = repaymentScheduleInstallment.getInterestCharged(currency).getAmount();
            final BigDecimal feeChargesCharged = repaymentScheduleInstallment.getFeeChargesCharged(currency).getAmount();
            final BigDecimal penaltyCharges = repaymentScheduleInstallment.getPenaltyChargesCharged(currency).getAmount();

            Date createdOnDate = null;
            if (repaymentScheduleInstallment.getCreatedDate() != null) {
                createdOnDate = repaymentScheduleInstallment.getCreatedDate().toDate();
            }

            final AppUser createdByUser = repaymentScheduleInstallment.getCreatedBy();
            final AppUser lastModifiedByUser = repaymentScheduleInstallment.getLastModifiedBy();

            Date lastModifiedOnDate = null;

            if (repaymentScheduleInstallment.getLastModifiedDate() != null) {
                lastModifiedOnDate = repaymentScheduleInstallment.getLastModifiedDate().toDate();
            }

            LoanRepaymentScheduleHistory loanRepaymentScheduleHistory = LoanRepaymentScheduleHistory.instance(loan, loanRescheduleRequest,
                    installmentNumber, fromDate, dueDate, principal, interestCharged, feeChargesCharged, penaltyCharges, createdOnDate,
                    createdByUser, lastModifiedByUser, lastModifiedOnDate, version);

            loanRepaymentScheduleHistoryList.add(loanRepaymentScheduleHistory);
        }
        return loanRepaymentScheduleHistoryList;
    }

    @Override
    public void createAndSaveLoanScheduleArchive(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Loan loan,
            LoanRescheduleRequest loanRescheduleRequest) {
        List<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = createLoanScheduleArchive(repaymentScheduleInstallments,
                loan, loanRescheduleRequest);
        this.loanRepaymentScheduleHistoryRepository.save(loanRepaymentScheduleHistoryList);

    }

}
