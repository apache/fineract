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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.MathUtil;
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

    /**
     * Determines and applies the status transition implied by the loan's balance after a monetary transaction:
     * overpaid, repaid in full, or reopened from a matured state. Mirrors
     * {@code DefaultLoanLifecycleStateMachine.determineAndTransition} in the core loan module.
     * <p>
     * Closure is decided on the amount that is <em>due</em> as of {@code transactionDate}: the outstanding principal
     * plus any fee/penalty charge whose due date has arrived. A not-yet-due (in advance) charge does not block closure,
     * while a due-but-unpaid fee/penalty keeps the loan open even once the principal is settled.
     */
    public void determineAndTransition(final WorkingCapitalLoan loan, final LocalDate transactionDate,
            final List<WorkingCapitalLoanCharge> activeCharges) {
        if (loan.getBalance() == null) {
            return;
        }
        final LoanStatus currentStatus = loan.getLoanStatus();
        final BigDecimal overpaymentAmount = MathUtil.nullToZero(loan.getBalance().getOverpaymentAmount());
        final BigDecimal dueOutstanding = dueOutstanding(loan, transactionDate, activeCharges);
        if (overpaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (currentStatus == null || !currentStatus.isOverpaid()) {
                transition(WorkingCapitalLoanEvent.LOAN_OVERPAID, loan);
            }
            if (loan.getMaturedOnDate() == null) {
                loan.setMaturedOnDate(transactionDate);
            }
        } else if (dueOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            if (currentStatus == null || !currentStatus.isClosedObligationsMet()) {
                transition(WorkingCapitalLoanEvent.LOAN_REPAID_IN_FULL, loan);
            }
            if (loan.getMaturedOnDate() == null) {
                loan.setMaturedOnDate(transactionDate);
            }
        } else if (dueOutstanding.compareTo(BigDecimal.ZERO) > 0 && loan.getMaturedOnDate() != null
                && canTransition(WorkingCapitalLoanEvent.LOAN_REOPENED, loan)) {
            transition(WorkingCapitalLoanEvent.LOAN_REOPENED, loan);
            loan.setMaturedOnDate(null);
        }
    }

    /**
     * Outstanding amount that is due as of {@code asOf}: principal (always due for closure) plus every active
     * fee/penalty charge whose due date is on or before {@code asOf}. In-advance charges are excluded so they do not
     * keep an otherwise settled loan open.
     */
    private BigDecimal dueOutstanding(final WorkingCapitalLoan loan, final LocalDate asOf,
            final List<WorkingCapitalLoanCharge> activeCharges) {
        final BigDecimal principalOutstanding = MathUtil.nullToZero(loan.getBalance().getPrincipalOutstanding());
        if (activeCharges == null || activeCharges.isEmpty()) {
            return principalOutstanding;
        }
        final BigDecimal dueChargeOutstanding = activeCharges.stream()
                .filter(charge -> charge.getDueDate() != null && !charge.getDueDate().isAfter(asOf))
                .map(charge -> MathUtil.nullToZero(charge.getAmountOutstanding())).reduce(BigDecimal.ZERO, BigDecimal::add);
        return principalOutstanding.add(dueChargeOutstanding);
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
            case LOAN_REPAID_IN_FULL -> (from.isActive() || from.isOverpaid()) ? LoanStatus.CLOSED_OBLIGATIONS_MET : null;
            case LOAN_OVERPAID -> (from.isActive() || from.isClosedObligationsMet() || from.isOverpaid()) ? LoanStatus.OVERPAID : null;
            case LOAN_REOPENED -> (from.isOverpaid() || from.isClosedObligationsMet()) ? LoanStatus.ACTIVE : null;
            case LOAN_CREDIT_BALANCE_REFUND_IN_FULL -> from.isOverpaid() ? LoanStatus.CLOSED_OBLIGATIONS_MET : null;
        };
    }
}
