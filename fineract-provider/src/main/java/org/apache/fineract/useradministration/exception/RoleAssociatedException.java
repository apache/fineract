/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class RoleAssociatedException extends AbstractPlatformDomainRuleException {

    public RoleAssociatedException(final String errorcode, final Long id) {
        super(errorcode, "Role with identifier " + id + " associated with users", id);
    }
}