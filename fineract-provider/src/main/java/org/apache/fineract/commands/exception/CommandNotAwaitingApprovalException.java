/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class CommandNotAwaitingApprovalException extends AbstractPlatformResourceNotFoundException {

    public CommandNotAwaitingApprovalException(final Long id) {
        super("error.msg.command.id.not.awaiting.approval", "Audit with identifier " + id + " is Not Awaiting Approval", id);
    }
}