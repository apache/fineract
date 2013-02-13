/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.data;

import java.util.Map;

/**
 * Represents the successful result of an REST API call that results in
 * processing a command.
 */
public class CommandProcessingResultBuilder {

    private Long commandId;
    private Long officeId;
    private Long groupId;
    private Long clientId;
    private Long loanId;
    private Long entityId;
    private Map<String, Object> changes;

    public CommandProcessingResult build() {
        return CommandProcessingResult.fromDetails(commandId, officeId, groupId, clientId, loanId, entityId, changes);
    }

    public CommandProcessingResultBuilder withCommandId(final Long withCommandId) {
        this.commandId = withCommandId;
        return this;
    }

    public CommandProcessingResultBuilder with(final Map<String, Object> withChanges) {
        this.changes = withChanges;
        return this;
    }

    public CommandProcessingResultBuilder withEntityId(final Long withEntityId) {
        this.entityId = withEntityId;
        return this;
    }

    public CommandProcessingResultBuilder withOfficeId(final Long withOfficeId) {
        this.officeId = withOfficeId;
        return this;
    }

    public CommandProcessingResultBuilder withClientId(final Long withClientId) {
        this.clientId = withClientId;
        return this;
    }

    public CommandProcessingResultBuilder withGroupId(final Long withGroupId) {
        this.groupId = withGroupId;
        return this;
    }
    
    public CommandProcessingResultBuilder withLoanId(final Long withLoanId) {
        this.loanId = withLoanId;
        return this;
    }
}