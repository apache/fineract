/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when journal entry resources are not found.
 */
public class JournalEntryNotFoundException extends AbstractPlatformResourceNotFoundException {

    public JournalEntryNotFoundException(final String transactionId) {
        super("error.msg.journalEntries.transactionId.invalid", "Journal Entries with transaction Identifier " + transactionId
                + " does not exist or are not system generated/reversible ", transactionId);
    }

    public JournalEntryNotFoundException(final Long entryId) {
        super("error.msg.journalEntries.id.invalid", "Journal Entry with entry Id " + entryId + " does not exist ", entryId);
    }
}