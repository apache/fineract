/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class JobInProcessExecution extends AbstractPlatformResourceNotFoundException {

    public JobInProcessExecution(final String identifier) {
        super("error.msg.sheduler.job.inprogress", "job execution is in process for " + identifier, identifier);
    }

}
