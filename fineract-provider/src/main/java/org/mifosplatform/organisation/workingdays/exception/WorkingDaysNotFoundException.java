/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when Calendar resources are not found.
 */
public class WorkingDaysNotFoundException extends AbstractPlatformResourceNotFoundException {

    public WorkingDaysNotFoundException() {
        super("error.msg.working.days.not.configured", "Must configure the Working days for the organisation.");
    }

}