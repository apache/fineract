/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when holiday resources are not found.
 */
public class HolidayNotFoundException extends AbstractPlatformResourceNotFoundException {

    public HolidayNotFoundException(final Long id) {
        super("error.msg.holiday.id.invalid", "Holiday with identifier " + id + " does not exist", id);
    }
}