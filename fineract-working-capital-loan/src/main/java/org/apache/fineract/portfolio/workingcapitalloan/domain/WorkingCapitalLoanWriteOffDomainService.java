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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Component;

/**
 * Holds the write-off domain behaviour that would otherwise live on {@link WorkingCapitalLoan} /
 * {@link WorkingCapitalLoanBalance}, keeping those entities as data holders. A write-off is terminal: it transitions
 * the loan to {@code CLOSED_WRITTEN_OFF}, zeroes the outstanding balance (moving each portion into a written-off
 * bucket), and records the audit trail. Undo reverses all three.
 * <p>
 * Both operations require the loan to carry a balance: the caller rejects a loan without one rather than letting a
 * write-off close an account on a balance that was never read.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanWriteOffDomainService {

    private final WorkingCapitalLoanLifecycleStateMachine loanLifecycleStateMachine;

    /**
     * Applies a write-off to the loan. The {@code writeOffReason} is optional and may be {@code null}. The
     * {@code charges} are the loan's active charges: each one's unpaid remainder is marked written off so the charge
     * view stays consistent with the zeroed balance.
     */
    public void writeOff(final WorkingCapitalLoan loan, final LocalDate writtenOffOnDate, final AppUser writtenOffBy,
            final CodeValue writeOffReason, final List<WorkingCapitalLoanCharge> charges) {
        loanLifecycleStateMachine.transition(WorkingCapitalLoanEvent.LOAN_WRITTEN_OFF, loan, writtenOffOnDate);
        loan.setWrittenOffOnDate(writtenOffOnDate);
        loan.setClosedOnDate(writtenOffOnDate);
        loan.setClosedBy(writtenOffBy);
        loan.setWriteOffReason(writeOffReason);
        zeroOutstanding(loan.getBalance());
        writeOffCharges(charges);
    }

    /**
     * Reverses a write-off applied in error: reopens the loan, restores the outstanding balance (loan-level and per
     * charge) and clears the audit trail.
     */
    public void undoWriteOff(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanCharge> charges) {
        loanLifecycleStateMachine.transition(WorkingCapitalLoanEvent.LOAN_WRITTEN_OFF_UNDO, loan, DateUtils.getBusinessLocalDate());
        loan.setWrittenOffOnDate(null);
        loan.setClosedOnDate(null);
        loan.setClosedBy(null);
        loan.setWriteOffReason(null);
        restoreOutstanding(loan.getBalance());
        restoreCharges(charges);
    }

    private void zeroOutstanding(final WorkingCapitalLoanBalance balance) {
        // Capture the outstanding portions before mutating, then move each into its written-off bucket so the computed
        // outstanding becomes zero without touching the paid columns.
        final BigDecimal principalOutstanding = balance.getPrincipalOutstanding();
        final BigDecimal feeOutstanding = balance.getFeeOutstanding();
        final BigDecimal penaltyOutstanding = balance.getPenaltyOutstanding();
        balance.setPrincipalWrittenOff(MathUtil.add(balance.getPrincipalWrittenOff(), principalOutstanding));
        balance.setFeeWrittenOff(MathUtil.add(balance.getFeeWrittenOff(), feeOutstanding));
        balance.setPenaltyWrittenOff(MathUtil.add(balance.getPenaltyWrittenOff(), penaltyOutstanding));
    }

    private void restoreOutstanding(final WorkingCapitalLoanBalance balance) {
        balance.setPrincipalWrittenOff(BigDecimal.ZERO);
        balance.setFeeWrittenOff(BigDecimal.ZERO);
        balance.setPenaltyWrittenOff(BigDecimal.ZERO);
    }

    private void writeOffCharges(final List<WorkingCapitalLoanCharge> charges) {
        for (final WorkingCapitalLoanCharge charge : charges) {
            // Capture the outstanding before mutating: it is derived from the written-off amount being set.
            final BigDecimal outstanding = charge.getAmountOutstanding();
            charge.setAmountWrittenOff(MathUtil.add(charge.getAmountWrittenOff(), outstanding));
        }
    }

    private void restoreCharges(final List<WorkingCapitalLoanCharge> charges) {
        // A single terminal write-off is the only writer of this bucket, so the undo resets it outright -- the same
        // treatment the balance buckets get in restoreOutstanding.
        for (final WorkingCapitalLoanCharge charge : charges) {
            charge.setAmountWrittenOff(BigDecimal.ZERO);
        }
    }
}
