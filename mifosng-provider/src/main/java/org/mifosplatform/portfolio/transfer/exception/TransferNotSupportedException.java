/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when not supported loan
 * template type is sent.
 */
public class TransferNotSupportedException extends AbstractPlatformDomainRuleException {

    /**
     * Default Constructor (cannot transfer entities with active loan/savings
     * accounts)
     */
    public TransferNotSupportedException() {
        super("error.msg.entity.transfers.with.active.accounts", "Cannot transfer Clients/Groups having active loan or savings accounts");
    }

    /**
     * Overloaded Constructor (Cannot transfer Clients with active accounts
     * between groups with different meeting frequency)
     * 
     * @param clientId
     * @param sourceGroupId
     * @param destinationGroupId
     */
    public TransferNotSupportedException(Long clientId, Long sourceGroupId, Long destinationGroupId) {
        super("error.msg.client.transfers.with.active.accounts.between.groups.with.different.meeting.frequency",
                "Cannot transfer Clients with active accounts between groups with different meeting frequency", clientId, sourceGroupId,
                destinationGroupId);
    }

    /**
     * Overloaded Constructor (Cannot transfer Clients with active (JLG)
     * accounts to a group with no meetings defined)
     * 
     * @param clientId
     * @param sourceGroupId
     * @param destinationGroupId
     */
    public TransferNotSupportedException(Long clientId, Long sourceGroupId, Long destinationGroupId, boolean destinationGroupMeetingAbsent) {
        super("error.msg.client.transfers.with.active.accounts.to.group.with.no.meeting.frequency",
                "Cannot transfer Client with active accounts to a groups with no meeting frequency", clientId, sourceGroupId,
                destinationGroupId,destinationGroupMeetingAbsent);
    }
}