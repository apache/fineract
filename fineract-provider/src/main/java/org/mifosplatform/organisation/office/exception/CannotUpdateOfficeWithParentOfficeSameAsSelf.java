/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * Exception thrown when an attempt is made update the parent of a root office.
 */
public class CannotUpdateOfficeWithParentOfficeSameAsSelf extends AbstractPlatformDomainRuleException {

    public CannotUpdateOfficeWithParentOfficeSameAsSelf(final Long officeId, final Long parentId) {
        super("error.msg.office.parentId.same.as.id", "Cannot update office with parent same as self.", officeId, parentId);
    }
}
