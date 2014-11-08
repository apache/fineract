/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanApprovalData {

    private final LocalDate approvalDate;
    private final BigDecimal approvalAmount;

    public LoanApprovalData(final BigDecimal approvalAmount, final LocalDate approvalDate) {
        this.approvalDate = approvalDate;
        this.approvalAmount = approvalAmount;
    }

    public LocalDate getApprovalDate() {
        return this.approvalDate;
    }

    public BigDecimal getApprovalAmount() {
        return this.approvalAmount;
    }

}