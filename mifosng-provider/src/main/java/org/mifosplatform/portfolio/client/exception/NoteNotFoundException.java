package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when note resources are not found.
 */
public class NoteNotFoundException extends AbstractPlatformResourceNotFoundException {

    public NoteNotFoundException(Long id) {
        super("error.msg.note.id.invalid", "Note with identifier " + id + " does not exist", id);
    }

    public NoteNotFoundException(Long id, Long clientId, final String resource) {
        super("error.msg." + resource + ".note.id.invalid", "Note with identifier " + id + " does not exist for " + resource
                + " with identifier " + clientId, id, clientId);
    }
}