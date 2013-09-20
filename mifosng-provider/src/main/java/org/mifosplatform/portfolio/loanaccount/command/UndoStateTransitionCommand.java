/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
        return this.note;
    }
}