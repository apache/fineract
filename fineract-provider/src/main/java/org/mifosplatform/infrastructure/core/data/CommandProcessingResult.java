/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the successful result of an REST API call that results in
 * processing a command.
 */
public class CommandProcessingResult implements Serializable {

    private Long commandId;
    private Long officeId;
    private final Long groupId;
    private final Long clientId;
    private final Long loanId;
    private final Long savingsId;
    private final Long resourceId;
    private final Long subResourceId;
    private final String transactionId;
    private final Map<String, Object> changes;
    @SuppressWarnings("unused")
    private final String resourceIdentifier;
    private final Long productId;
    private Boolean rollbackTransaction;

    public static CommandProcessingResult fromDetails(final Long commandId, final Long officeId, final Long groupId, final Long clientId,
            final Long loanId, final Long savingsId, final String resourceIdentifier, final Long entityId, final String transactionId,
            final Map<String, Object> changes, final Long productId, final Boolean rollbackTransaction, final Long subResourceId) {
        return new CommandProcessingResult(commandId, officeId, groupId, clientId, loanId, savingsId, resourceIdentifier, entityId,
                transactionId, changes, productId, rollbackTransaction, subResourceId);
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
        return new CommandProcessingResult(null, null, null, null);
    }

    /*
     * Deprecated
     */
    public CommandProcessingResult(final Long entityId) {
        if (entityId != null) {
            this.resourceIdentifier = entityId.toString();
        } else {
            this.resourceIdentifier = null;
        }
        this.resourceId = entityId;
        this.officeId = null;
        this.groupId = null;
        this.clientId = null;
        this.loanId = null;
        this.savingsId = null;
        this.transactionId = null;
        this.changes = new HashMap<>();
        this.productId = null;
        this.subResourceId = null;
    }

    private CommandProcessingResult(final Long commandId, final Long officeId, final Long groupId, final Long clientId, final Long loanId,
            final Long savingsId, final String resourceIdentifier, final Long resourceId, final String transactionId,
            final Map<String, Object> changesOnly, final Long productId, Boolean rollbackTransaction, final Long subResourceId) {
        this.commandId = commandId;
        this.officeId = officeId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.resourceIdentifier = resourceIdentifier;
        this.resourceId = resourceId;
        this.changes = changesOnly;
        this.transactionId = transactionId;
        this.productId = productId;
        this.rollbackTransaction = rollbackTransaction;
        this.subResourceId = subResourceId;
    }

    private CommandProcessingResult(final Long resourceId, final Long officeId, final Long commandId, final Map<String, Object> changesOnly) {
        if (resourceId != null) {
            this.resourceIdentifier = resourceId.toString();
        } else {
            this.resourceIdentifier = null;
        }
        this.resourceId = resourceId;
        this.officeId = officeId;
        this.groupId = null;
        this.clientId = null;
        this.loanId = null;
        this.savingsId = null;
        this.transactionId = null;
        this.commandId = commandId;
        this.changes = changesOnly;
        this.productId = null;
        this.subResourceId = null;
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

    public void setOfficeId(final Long officeId) {
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

    public Long getSavingsId() {
        return this.savingsId;
    }

    public String getTransactionId() {
        return this.transactionId;
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

    public Long getProductId() {
        return this.productId;
    }

    public boolean isRollbackTransaction() {
        return this.rollbackTransaction != null && this.rollbackTransaction;
    }

    public void setRollbackTransaction(Boolean rollbackTransaction) {
        this.rollbackTransaction = rollbackTransaction;
    }

    public Long getSubResourceId() {
        return subResourceId;
    }
}