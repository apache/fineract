/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when charge is not active.
 */
public class ChargeIsNotActiveException extends AbstractPlatformDomainRuleException {

    public ChargeIsNotActiveException(final Long id, final String name) {
        super("error.msg.charge.is.not.active", "Charge '" + name + "' with identifier " + id + " is not active", name, id);
    }
}