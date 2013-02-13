/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
