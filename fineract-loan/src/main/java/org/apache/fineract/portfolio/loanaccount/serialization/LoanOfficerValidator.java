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
package org.apache.fineract.portfolio.loanaccount.serialization;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOfficerAssignmentHistory;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerAssignmentDateException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerUnassignmentDateException;
import org.springframework.stereotype.Component;

@Component
public final class LoanOfficerValidator {

    public void validateUnassignDate(final Loan loan, final LocalDate unassignDate) {
        final Optional<LoanOfficerAssignmentHistory> latestHistoryRecordOptional = loan.findLatestIncompleteHistoryRecord();

        if (latestHistoryRecordOptional.isEmpty()) {
            return;
        }

        final LocalDate startDate = latestHistoryRecordOptional.get().getStartDate();
        if (DateUtils.isAfter(startDate, unassignDate)) {
            final String errorMessage = "The Loan officer Unassign date(" + unassignDate + ") cannot be before its assignment date ("
                    + startDate + ").";
            throw new LoanOfficerUnassignmentDateException("cannot.be.before.assignment.date", errorMessage, loan.getId(),
                    loan.getLoanOfficer().getId(), startDate, unassignDate);
        } else if (DateUtils.isDateInTheFuture(unassignDate)) {
            final String errorMessage = "The Loan Officer Unassign date (" + unassignDate + ") cannot be in the future.";
            throw new LoanOfficerUnassignmentDateException("cannot.be.a.future.date", errorMessage, unassignDate);
        }
    }

    public void validateReassignment(final Loan loan, final LocalDate assignmentDate,
            final LoanOfficerAssignmentHistory lastAssignmentRecord) {
        if (loan.isSubmittedOnDateAfter(assignmentDate)) {
            final String errorMessage = "The Loan Officer assignment date (" + assignmentDate.toString()
                    + ") cannot be before loan submitted date (" + loan.getSubmittedOnDate().toString() + ").";
            throw new LoanOfficerAssignmentDateException("cannot.be.before.loan.submittal.date", errorMessage, assignmentDate,
                    loan.getSubmittedOnDate());
        }

        if (lastAssignmentRecord != null && lastAssignmentRecord.isEndDateAfter(assignmentDate)) {
            final String errorMessage = "The Loan Officer assignment date (" + assignmentDate
                    + ") cannot be before previous Loan Officer unassigned date (" + lastAssignmentRecord.getEndDate() + ").";
            throw new LoanOfficerAssignmentDateException("cannot.be.before.previous.unassignement.date", errorMessage, assignmentDate,
                    lastAssignmentRecord.getEndDate());
        }

        if (DateUtils.isDateInTheFuture(assignmentDate)) {
            final String errorMessage = "The Loan Officer assignment date (" + assignmentDate + ") cannot be in the future.";
            throw new LoanOfficerAssignmentDateException("cannot.be.a.future.date", errorMessage, assignmentDate);
        }
    }

    public void validateAssignmentDateWithHistory(final Loan loan, final Optional<LoanOfficerAssignmentHistory> latestHistoryRecord,
            final LocalDate assignmentDate) {
        if (latestHistoryRecord.isPresent() && latestHistoryRecord.get().isBeforeStartDate(assignmentDate)) {
            final String errorMessage = "Loan with identifier " + loan.getId() + " was already assigned before date " + assignmentDate;
            throw new LoanOfficerAssignmentDateException("is.before.last.assignment.date", errorMessage, loan.getId(), assignmentDate);
        }
    }
}
