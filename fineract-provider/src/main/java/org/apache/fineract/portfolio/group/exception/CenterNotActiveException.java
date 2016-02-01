/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class CenterNotActiveException extends AbstractPlatformDomainRuleException {

    public CenterNotActiveException(final Long centerId) {
        super("error.msg.center.not.active.exception", "The Center with id `" + centerId + "` is not active", centerId);
    }

}
