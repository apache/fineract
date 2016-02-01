/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;

/**
 * A {@link RuntimeException} thrown when Job execution is in progress.
 */
public class OperationNotAllowedException extends AbstractPlatformServiceUnavailableException {

    public OperationNotAllowedException(final String jobNames) {
        super("error.msg.sheduler.job.currently.running", "Execution is in-process for jobs " + jobNames
                + "...., so update operations are not allowed at this moment", jobNames);
    }
}