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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.springframework.stereotype.Component;

@Component
public class WorkingCapitalLoanLifecycleStateMachine {

    public void transition(final WorkingCapitalLoanEvent event, final WorkingCapitalLoan loan) {
        LoanStatus newStatus = getNextStatus(event, loan);
        if (newStatus != null) {
            loan.setLoanStatus(newStatus);
        } else {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.transition.not.allowed",
                    "Transition " + event + " is not allowed from status " + loan.getLoanStatus(), "loanStatus");
        }
    }

    public boolean canTransition(final WorkingCapitalLoanEvent event, final WorkingCapitalLoan loan) {
        return getNextStatus(event, loan) != null;
    }

    private LoanStatus getNextStatus(final WorkingCapitalLoanEvent event, final WorkingCapitalLoan loan) {
        LoanStatus from = loan.getLoanStatus();
        if (from == null) {
            return null;
        }

        return switch (event) {
            case LOAN_APPROVED -> from.isSubmittedAndPendingApproval() ? LoanStatus.APPROVED : null;
            case LOAN_APPROVAL_UNDO -> from.isApproved() ? LoanStatus.SUBMITTED_AND_PENDING_APPROVAL : null;
            case LOAN_REJECTED -> from.isSubmittedAndPendingApproval() ? LoanStatus.REJECTED : null;
            case LOAN_DISBURSED -> from.isApproved() ? LoanStatus.ACTIVE : null;
            case LOAN_DISBURSAL_UNDO -> from.isActive() ? LoanStatus.APPROVED : null;
        };
    }
}
