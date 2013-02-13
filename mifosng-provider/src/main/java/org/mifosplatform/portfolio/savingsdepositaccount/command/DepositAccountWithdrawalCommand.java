/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.command;

import org.joda.time.LocalDate;

public class DepositAccountWithdrawalCommand {

    private final Long accountId;
    private final String note;
    private final LocalDate maturesOnDate;

    public DepositAccountWithdrawalCommand(final Long accountId, final String note, final LocalDate maturesOnDate) {
        this.accountId = accountId;
        this.note = note;
        this.maturesOnDate = maturesOnDate;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getNote() {
        return note;
    }

    public LocalDate getMaturesOnDate() {
        return maturesOnDate;
    }
}
