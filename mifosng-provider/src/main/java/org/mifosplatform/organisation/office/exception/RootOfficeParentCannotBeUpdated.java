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
public class RootOfficeParentCannotBeUpdated extends AbstractPlatformDomainRuleException {

    public RootOfficeParentCannotBeUpdated() {
        super("error.msg.office.cannot.update.parent.office.of.root.office", "The root office must not be set with a parent office.");
    }
}
