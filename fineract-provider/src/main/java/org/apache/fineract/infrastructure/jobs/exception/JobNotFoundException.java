/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when Job resources are not found.
 */
public class JobNotFoundException extends AbstractPlatformResourceNotFoundException {

    public JobNotFoundException(final String identifier) {
        super("error.msg.sheduler.job.id.invalid", "Job with identifier " + identifier + " does not exist", identifier);
    }
}