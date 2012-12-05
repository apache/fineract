package org.mifosplatform.portfolio.client.command;

import java.util.Set;

/**
 * Immutable command used for create or update of notes.
 */
public class ClientNoteCommand {

    private final Long clientId;
    private final String note;

    private final transient Long id;
    private final transient Set<String> parametersPassedInRequest;
    private final transient boolean makerCheckerApproval;

    public static ClientNoteCommand clientSubResource(final ClientNoteCommand command, final Long providedClientId) {
        return new ClientNoteCommand(command.parametersPassedInRequest, command.makerCheckerApproval, command.id, providedClientId, command.note);
    }

    public ClientNoteCommand(final Set<String> parametersPassedInRequest, final boolean makerCheckerApproval, final Long id, Long clientId,
            final String note) {
        this.parametersPassedInRequest = parametersPassedInRequest;
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.clientId = clientId;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getNote() {
        return note;
    }
}