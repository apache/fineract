/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when attempting to delete groups.
 */
public class GroupMustBePendingToBeDeletedException extends AbstractPlatformDomainRuleException {

    public GroupMustBePendingToBeDeletedException(final Long id) {
        super("error.msg.group.cannot.be.deleted", "Group with identifier " + id + " cannot be deleted as it is not in `Pending` state.",
                id);
    }
}