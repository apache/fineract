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