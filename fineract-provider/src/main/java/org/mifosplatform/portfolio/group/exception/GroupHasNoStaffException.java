/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when group resources are not found.
 */
public class GroupHasNoStaffException extends AbstractPlatformResourceNotFoundException {

    public GroupHasNoStaffException(final Long groupId) {
        super("error.msg.group.has.no.staff", "Group with identifier " + groupId + " does not have staff", groupId);
    }

}
