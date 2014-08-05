/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class GroupNotActiveException extends AbstractPlatformDomainRuleException {

    public GroupNotActiveException(final Long groupId) {
        super("error.msg.group.not.active.exception", "The Group with id `" + groupId + "` is not active", groupId);
    }

}
