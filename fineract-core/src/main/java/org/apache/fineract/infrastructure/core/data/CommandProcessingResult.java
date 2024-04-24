/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.data;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.domain.ExternalId;

/**
 * Represents the successful result of an REST API call that results in processing a command.
 */
@ToString
@Getter
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
    private final Map<String, Object> creditBureauReportData;
    private final String resourceIdentifier;
    private final Long productId;
    private final Long gsimId;
    private final Long glimId;
    private Boolean rollbackTransaction;
    private final ExternalId resourceExternalId;
    private final ExternalId subResourceExternalId;

    private CommandProcessingResult(final Long commandId, final Long officeId, final Long groupId, final Long clientId, final Long loanId,
            final Long savingsId, final String resourceIdentifier, final Long resourceId, final String transactionId,
            final Map<String, Object> changes, final Long productId, final Long gsimId, final Long glimId,
            final Map<String, Object> creditBureauReportData, Boolean rollbackTransaction, final Long subResourceId,
            final ExternalId resourceExternalId, final ExternalId subResourceExternalId) {
        this.commandId = commandId;
        this.officeId = officeId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.resourceIdentifier = resourceIdentifier;
        this.resourceId = resourceId;
        this.changes = changes;
        this.transactionId = transactionId;
        this.productId = productId;
        this.gsimId = gsimId;
        this.glimId = glimId;
        this.creditBureauReportData = creditBureauReportData;
        this.rollbackTransaction = rollbackTransaction;
        this.subResourceId = subResourceId;
        this.resourceExternalId = resourceExternalId;
        this.subResourceExternalId = subResourceExternalId;
    }

    protected CommandProcessingResult(final Long resourceId, final Long officeId, final Long commandId, final Map<String, Object> changes,
            Long clientId) {
        this(commandId, officeId, null, clientId, null, null, resourceId == null ? null : resourceId.toString(), resourceId, null, changes,
                null, null, null, null, null, null, ExternalId.empty(), ExternalId.empty());
    }

    protected CommandProcessingResult(final Long resourceId, final Long officeId, final Long commandId, final Map<String, Object> changes) {
        this(resourceId, officeId, commandId, changes, null);
    }

    protected CommandProcessingResult(final Long resourceId) {
        this(resourceId, null, null, null);
    }

    public static CommandProcessingResult fromCommandProcessingResult(CommandProcessingResult commandResult, final Long resourceId) {
        return new CommandProcessingResult(commandResult.commandId, commandResult.officeId, commandResult.groupId, commandResult.clientId,
                commandResult.loanId, commandResult.savingsId, commandResult.resourceIdentifier, resourceId, commandResult.transactionId,
                commandResult.changes, commandResult.productId, commandResult.gsimId, commandResult.glimId,
                commandResult.creditBureauReportData, commandResult.rollbackTransaction, commandResult.subResourceId,
                commandResult.resourceExternalId, commandResult.subResourceExternalId);
    }

    public static CommandProcessingResult fromCommandProcessingResult(CommandProcessingResult commandResult) {
        return fromCommandProcessingResult(commandResult, commandResult.getResourceId());
    }

    public static CommandProcessingResult fromDetails(final Long commandId, final Long officeId, final Long groupId, final Long clientId,
            final Long loanId, final Long savingsId, final String resourceIdentifier, final Long entityId, final Long gsimId,
            final Long glimId, final Map<String, Object> creditBureauReportData, final String transactionId,
            final Map<String, Object> changes, final Long productId, final Boolean rollbackTransaction, final Long subResourceId,
            final ExternalId resourceExternalId, final ExternalId subResourceExternalId) {
        return new CommandProcessingResult(commandId, officeId, groupId, clientId, loanId, savingsId, resourceIdentifier, entityId,
                transactionId, changes, productId, gsimId, glimId, creditBureauReportData, rollbackTransaction, subResourceId,
                resourceExternalId, subResourceExternalId);
    }

    public static CommandProcessingResult commandOnlyResult(final Long commandId) {
        return new CommandProcessingResult(null, null, commandId, null);
    }

    public static CommandProcessingResult resourceResult(final Long resourceId, final Long commandId, final Map<String, Object> changes) {
        return new CommandProcessingResult(resourceId, null, commandId, changes);
    }

    public static CommandProcessingResult resourceResult(final Long resourceId, final Long commandId) {
        return new CommandProcessingResult(resourceId, null, commandId, null);
    }

    public static CommandProcessingResult resourceResult(final Long resourceId) {
        return new CommandProcessingResult(resourceId);
    }

    public static CommandProcessingResult withChanges(final Long resourceId, final Map<String, Object> changes) {
        return new CommandProcessingResult(resourceId, null, null, changes);
    }

    public static CommandProcessingResult empty() {
        return new CommandProcessingResult(null, null, null, null);
    }

    public void setOfficeId(final Long officeId) {
        this.officeId = officeId;
    }

    public Map<String, Object> getChanges() {
        Map<String, Object> checkIfEmpty = null;
        if (this.changes != null && !this.changes.isEmpty()) {
            checkIfEmpty = this.changes;
        }
        return checkIfEmpty;
    }

    // TODO: Jackson uses getters to serialize fields. getChanges() never returns empty Map.
    // Remove when getChanges() starts returning empty Map
    @JsonGetter("changes")
    public Map<String, Object> getChangesJacksonSerialization() {
        return this.changes;
    }

    public boolean hasChanges() {
        final boolean noChanges = this.changes == null || this.changes.isEmpty();
        return !noChanges;
    }

    public boolean isRollbackTransaction() {
        return this.rollbackTransaction != null && this.rollbackTransaction;
    }

    public void setRollbackTransaction(Boolean rollbackTransaction) {
        this.rollbackTransaction = rollbackTransaction;
    }
}
