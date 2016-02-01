/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when attempting to crate
 * charge of type penalty due at disbursement .
 */
public class ChargeDueAtDisbursementCannotBePenaltyException extends AbstractPlatformDomainRuleException {

    public ChargeDueAtDisbursementCannotBePenaltyException(final String name) {
        super("error.msg.charge.due.at.disbursement.cannot.be.penalty", "Charge '" + name + "' is invalid.", name, name);
    }
}