/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when CalendarInstance resources are not
 * found.
 */
public class CalendarInstanceNotFoundException extends AbstractPlatformResourceNotFoundException {

    public CalendarInstanceNotFoundException(final Long id) {
        super("error.msg.calendar.instance.id.invalid", "Calendar Instance with identifier " + id + " does not exist", id);
    }

    public CalendarInstanceNotFoundException(final String postFix, final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg.calendar.instance." + postFix, defaultUserMessage, defaultUserMessageArgs);
    }
}