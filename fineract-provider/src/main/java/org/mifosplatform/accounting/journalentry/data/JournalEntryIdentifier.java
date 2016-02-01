/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

/**
 * Represents the successful result of an REST API call.
 */
public class JournalEntryIdentifier {

    private String entityId;

    // TODO - Rename variable to commandId or taskId or something that shows
    // this is the id of a command in a table/queue for processing.
    @SuppressWarnings("unused")
    private Long makerCheckerId;

    public static JournalEntryIdentifier makerChecker(final Long makerCheckerId) {
        return new JournalEntryIdentifier(null, makerCheckerId);
    }

    public static JournalEntryIdentifier makerChecker(final String resourceId, final Long makerCheckerId) {
        return new JournalEntryIdentifier(resourceId, makerCheckerId);
    }

    public JournalEntryIdentifier() {
        //
    }

    public JournalEntryIdentifier(final String entityId) {
        this.entityId = entityId;
    }

    private JournalEntryIdentifier(final String entityId, final Long makerCheckerId) {
        this.entityId = entityId;
        this.makerCheckerId = makerCheckerId;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }
}
