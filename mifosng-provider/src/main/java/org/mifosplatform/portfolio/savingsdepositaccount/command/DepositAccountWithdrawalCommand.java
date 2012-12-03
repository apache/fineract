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
