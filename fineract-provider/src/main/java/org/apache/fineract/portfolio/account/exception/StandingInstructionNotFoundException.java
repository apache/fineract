/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan resources are not found.
 */
public class StandingInstructionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public StandingInstructionNotFoundException(final Long id) {
        super("error.msg.standinginstruction.id.invalid", "AccountTransferStandingInstruction with identifier " + id + " does not exist",
                id);
    }
}