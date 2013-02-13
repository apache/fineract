/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.exception;

/**
 * A {@link RuntimeException} that is thrown in the case where an invalid or
 * unknown command is attempted to be processed by platform.
 */
public class UnsupportedCommandException extends RuntimeException {

    private final String unsupportedCommandName;

    public UnsupportedCommandException(final String unsupportedCommandName) {
        this.unsupportedCommandName = unsupportedCommandName;
    }

    public String getUnsupportedCommandName() {
        return unsupportedCommandName;
    }
}