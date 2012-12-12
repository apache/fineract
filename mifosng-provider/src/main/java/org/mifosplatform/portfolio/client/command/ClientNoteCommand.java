package org.mifosplatform.portfolio.client.command;

/**
 * Immutable command used for create or update of notes.
 */
public class ClientNoteCommand {

    private final String note;

    public ClientNoteCommand(final String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }
}