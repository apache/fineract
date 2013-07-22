package org.mifosplatform.infrastructure.jobs.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when Job execution is in progress.
 */
public class OperationNotAllowedException extends AbstractPlatformResourceNotFoundException {

    public OperationNotAllowedException(final String jobNames) {
        super("error.msg.sheduler.job.currently.running", "Exicution is in-process for jobs " + jobNames
                + "...., so update operations are not allowed at this moment", jobNames);
    }
}