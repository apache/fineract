/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class GroupExistsInCenterException extends AbstractPlatformDomainRuleException {

    public GroupExistsInCenterException(final Long centerId, final Long groupId) {
        super("error.msg.group.is.already.member.of.center", "Group with identifier " + groupId
                + " is already exists in Center with identifier " + centerId, groupId, centerId);
    }

}