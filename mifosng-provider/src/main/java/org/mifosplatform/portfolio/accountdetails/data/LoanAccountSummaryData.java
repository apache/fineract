/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accountdetails.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
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
    private final LoanStatusEnumData status;
    private final EnumOptionData loanType;
    private final Integer loanCycle;

    public LoanAccountSummaryData(final Long id, final String accountNo, final String externalId, final Long productId,
            final String loanProductName, final LoanStatusEnumData loanStatus, final EnumOptionData loanType, final Integer loanCycle) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = loanProductName;
        this.status = loanStatus;
        this.loanType = loanType;
        this.loanCycle = loanCycle;
    }
}