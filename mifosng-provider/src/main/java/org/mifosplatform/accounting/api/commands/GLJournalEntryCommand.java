package org.mifosplatform.accounting.api.commands;

import java.util.Set;

import org.joda.time.LocalDate;

/**
 * Immutable command for adding an accounting closure
 */
public class GLJournalEntryCommand {

    private final Long officeId;
    private final LocalDate entryDate;
    private final String comments;

    private final SingleDebitOrCreditEntryCommand[] credits;
    private final SingleDebitOrCreditEntryCommand[] debits;

    private final Set<String> parametersPassedInRequest;

    public GLJournalEntryCommand(Set<String> parametersPassedInRequest, Long officeId, LocalDate entryDate, String comments,
            SingleDebitOrCreditEntryCommand[] credits, SingleDebitOrCreditEntryCommand[] debits) {
        this.officeId = officeId;
        this.entryDate = entryDate;
        this.comments = comments;
        this.credits = credits;
        this.debits = debits;
        this.parametersPassedInRequest = parametersPassedInRequest;
    }

    public boolean isOfficeIdChanged() {
        return this.parametersPassedInRequest.contains("officeId");
    }

    public boolean isEntryDateChanged() {
        return this.parametersPassedInRequest.contains("entryDate");
    }

    public boolean isCommentsChanged() {
        return this.parametersPassedInRequest.contains("comments");
    }

    public boolean isCreditsChanged() {
        return this.parametersPassedInRequest.contains("credits");
    }

    public boolean isDebitsChanged() {
        return this.parametersPassedInRequest.contains("debits");
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public LocalDate getEntryDate() {
        return this.entryDate;
    }

    public String getComments() {
        return this.comments;
    }

    public SingleDebitOrCreditEntryCommand[] getCredits() {
        return this.credits;
    }

    public SingleDebitOrCreditEntryCommand[] getDebits() {
        return this.debits;
    }

}