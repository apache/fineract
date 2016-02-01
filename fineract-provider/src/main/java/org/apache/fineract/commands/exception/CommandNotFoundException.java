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
public class CommandNotFoundException extends AbstractPlatformResourceNotFoundException {

    public CommandNotFoundException(final Long id) {
        super("error.msg.command.id.invalid", "Audit with identifier " + id + " does not exist", id);
    }
}