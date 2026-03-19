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
package org.apache.fineract.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOfficerAssignmentHistory;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanOfficerValidator;

@RequiredArgsConstructor
public class LoanOfficerService {

    private final LoanOfficerValidator loanOfficerValidator;

    public void reassignLoanOfficer(final Loan loan, final Staff newLoanOfficer, final LocalDate assignmentDate) {
        final Optional<LoanOfficerAssignmentHistory> latestHistoryRecord = loan.findLatestIncompleteHistoryRecord();
        final LoanOfficerAssignmentHistory lastAssignmentRecord = loan.findLastAssignmentHistoryRecord(newLoanOfficer);

        // assignment date should not be less than loan submitted date
        loanOfficerValidator.validateReassignment(loan, assignmentDate, lastAssignmentRecord);
        loanOfficerValidator.validateAssignmentDateWithHistory(loan, latestHistoryRecord, assignmentDate);

        if (latestHistoryRecord.isPresent() && loan.getLoanOfficer().getId().equals(newLoanOfficer.getId())) {
            latestHistoryRecord.get().updateStartDate(assignmentDate);
        } else if (latestHistoryRecord.isPresent() && latestHistoryRecord.get().matchesStartDateOf(assignmentDate)) {
            latestHistoryRecord.get().updateLoanOfficer(newLoanOfficer);
            loan.setLoanOfficer(newLoanOfficer);
        } else {
            // loan officer correctly changed from previous loan officer to new loan officer
            latestHistoryRecord.ifPresent(loanOfficerAssignmentHistory -> loanOfficerAssignmentHistory.updateEndDate(assignmentDate));

            loan.setLoanOfficer(newLoanOfficer);
            if (loan.isNotSubmittedAndPendingApproval()) {
                final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory = LoanOfficerAssignmentHistory.createNew(loan,
                        loan.getLoanOfficer(), assignmentDate);
                loan.getLoanOfficerHistory().add(loanOfficerAssignmentHistory);
            }
        }
    }

    public void updateLoanOfficerOnLoanApplication(final Loan loan, final Staff newLoanOfficer) {
        if (!loan.isSubmittedAndPendingApproval()) {
            Long loanOfficerId = null;
            if (loan.getLoanOfficer() != null) {
                loanOfficerId = loan.getLoanOfficer().getId();
            }
            throw new LoanOfficerAssignmentException(loan.getId(), loanOfficerId);
        }
        loan.setLoanOfficer(newLoanOfficer);
    }
}
