/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.command;

import java.math.BigDecimal;

public class DepositAccountWithdrawInterestCommand {

    private final Long accountId;
    private final BigDecimal withdrawInterest;
    private final String note;

    public DepositAccountWithdrawInterestCommand(final Long accountId, BigDecimal withdrawInterest, String note) {
        this.accountId = accountId;
        this.withdrawInterest = withdrawInterest;
        this.note = note;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getWithdrawInterest() {
        return withdrawInterest;
    }

    public String getNote() {
        return note;
    }
}
