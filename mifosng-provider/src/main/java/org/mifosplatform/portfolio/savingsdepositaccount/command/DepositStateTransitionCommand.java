package org.mifosplatform.portfolio.savingsdepositaccount.command;

import org.joda.time.LocalDate;

public class DepositStateTransitionCommand {

    private final Long accountId;
    private final LocalDate eventDate;

    private final String note;

    public DepositStateTransitionCommand(final Long accountId, final LocalDate enentDate, final String note) {
        this.accountId = accountId;
        this.eventDate = enentDate;
        this.note = note;
    }

    public Long getAccountId() {
        return accountId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getNote() {
        return note;
    }

}
