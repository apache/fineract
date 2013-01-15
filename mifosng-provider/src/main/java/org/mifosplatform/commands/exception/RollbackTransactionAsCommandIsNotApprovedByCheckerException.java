package org.mifosplatform.commands.exception;

public class RollbackTransactionAsCommandIsNotApprovedByCheckerException extends RuntimeException {

    /**
     * Used to pass back the changes detected when updating a resource so the command json can be changed to only the changes.
     */
    private final String jsonOfChangesOnly;

    public RollbackTransactionAsCommandIsNotApprovedByCheckerException(final String jsonOfChangesOnly) {
        this.jsonOfChangesOnly = jsonOfChangesOnly;
    }
    
    public String getJsonOfChangesOnly() {
        return this.jsonOfChangesOnly;
    }
}
