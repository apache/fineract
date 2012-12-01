package org.mifosplatform.portfolio.loanaccount.command;

/**
 * Immutable command for undo'ing a state transition.
 */
public class UndoStateTransitionCommand {

    private final Long loanId;
    private final String note;

    public UndoStateTransitionCommand(final Long loanId, final String note) {
        this.loanId = loanId;
        this.note = note;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public String getNote() {
        return note;
    }
}