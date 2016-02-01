/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class GroupMemberCountNotInPermissibleRangeException extends AbstractPlatformDomainRuleException {

    public GroupMemberCountNotInPermissibleRangeException(final Long groupId, final Integer minClients, final Integer maxClients) {
        super("error.msg.group.members.count.must.be.in.permissible.range", "Number of members in the group with Id " + groupId
                + " should be between " + minClients + " and " + maxClients, groupId, minClients, maxClients);
    }

}
