/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accountdetails.data;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.mifosplatform.portfolio.loanaccount.data.LoanStatusEnumData;

/**
 * Immutable data object for loan accounts.
 */
@SuppressWarnings("unused")
public class LoanAccountSummaryData {

    private final Long id;
    private final String accountNo;
    private final String externalId;
    private final Long productId;
    private final String productName;
    private final String shortProductName;
    private final LoanStatusEnumData status;
    private final EnumOptionData loanType;
    private final Integer loanCycle;
    private final LoanApplicationTimelineData timeline;
    private final Boolean inArrears;
    private final BigDecimal originalLoan;
    private final BigDecimal loanBalance;
    private final BigDecimal amountPaid;
    
    public LoanAccountSummaryData(final Long id, final String accountNo, final String externalId, final Long productId,
            final String loanProductName, final String shortLoanProductName, final LoanStatusEnumData loanStatus, final EnumOptionData loanType, final Integer loanCycle,
            final LoanApplicationTimelineData timeline, final Boolean inArrears,final BigDecimal originalLoan,final BigDecimal loanBalance,final BigDecimal amountPaid) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = loanProductName;
        this.shortProductName = shortLoanProductName;
        this.status = loanStatus;
        this.loanType = loanType;
        this.loanCycle = loanCycle;
        this.timeline = timeline;
        this.inArrears = inArrears;
        this.loanBalance = loanBalance;
        this.originalLoan = originalLoan;
        this.amountPaid = amountPaid;
    }
}