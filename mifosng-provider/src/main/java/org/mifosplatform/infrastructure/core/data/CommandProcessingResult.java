/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the successful result of an REST API call that results in
 * processing a command.
 */
public class CommandProcessingResult {

    private Long commandId;
    private Long officeId;
    private final Long groupId;
    private final Long clientId;
    private final Long loanId;
    private Long resourceId;
    private Map<String, Object> changes;

    public static CommandProcessingResult fromDetails(final Long commandId, final Long officeId, final Long groupId, final Long clientId,
            final Long loanId, final Long entityId, final Map<String, Object> changes) {
        return new CommandProcessingResult(commandId, officeId, groupId, clientId, loanId, entityId, changes);
    }

    public static CommandProcessingResult commandOnlyResult(final Long commandId) {
        return new CommandProcessingResult(null, null, commandId, null);
    }

    public static CommandProcessingResult resourceResult(final Long resourceId, final Long commandId) {
        return new CommandProcessingResult(resourceId, null, commandId, null);
    }

    public static CommandProcessingResult resourceResult(final Long resourceId, final Long commandId, final Map<String, Object> changes) {
        return new CommandProcessingResult(resourceId, null, commandId, changes);
    }

    public static CommandProcessingResult subResourceResult(final Long resourceId, final Long subResourceId, final Long commandId) {
        return new CommandProcessingResult(resourceId, subResourceId, commandId, null);
    }

    public static CommandProcessingResult subResourceResult(final Long resourceId, final Long subResourceId, final Long commandId,
            final Map<String, Object> changes) {
        return new CommandProcessingResult(resourceId, subResourceId, commandId, changes);
    }

    public static CommandProcessingResult withChanges(final Long resourceId, final Map<String, Object> changes) {
        return new CommandProcessingResult(resourceId, null, null, changes);
    }

    public static CommandProcessingResult empty() {
        return new CommandProcessingResult(Long.valueOf(-1), Long.valueOf(-1), Long.valueOf(-1), null);
    }

    public CommandProcessingResult(final Long entityId) {
        this.resourceId = entityId;
        this.officeId = null;
        this.groupId = null;
        this.clientId = null;
        this.loanId = null;
        this.changes = new HashMap<String, Object>();
    }

    private CommandProcessingResult(final Long commandId, final Long officeId, final Long groupId, final Long clientId, final Long loanId,
            final Long resourceId, final Map<String, Object> changesOnly) {
        this.commandId = commandId;
        this.officeId = officeId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.resourceId = resourceId;
        this.changes = changesOnly;
    }

    private CommandProcessingResult(final Long resourceId, final Long officeId, final Long commandId, final Map<String, Object> changesOnly) {
        this.resourceId = resourceId;
        this.officeId = officeId;
        this.groupId = null;
        this.clientId = null;
        this.loanId = null;
        this.commandId = commandId;
        this.changes = changesOnly;
    }

    public Long commandId() {
        return this.commandId;
    }

    public Long resourceId() {
        return this.resourceId;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Map<String, Object> getChanges() {
        Map<String, Object> checkIfEmpty = null;
        if (this.changes != null && !this.changes.isEmpty()) {
            checkIfEmpty = this.changes;
        }
        return checkIfEmpty;
    }

    public boolean hasChanges() {
        final boolean noChanges = this.changes == null || this.changes.isEmpty();
        return !noChanges;
    }
}