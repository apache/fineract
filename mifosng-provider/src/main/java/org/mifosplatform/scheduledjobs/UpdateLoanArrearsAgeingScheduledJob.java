/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.scheduledjobs;

import java.util.Date;

import org.mifosplatform.scheduledjobs.service.ScheduledJobRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateLoanArrearsAgeingScheduledJob {

    private final static Logger logger = LoggerFactory.getLogger(UpdateLoanArrearsAgeingScheduledJob.class);

    private final ScheduledJobRunnerService scheduledJobRunnerService;

    public UpdateLoanArrearsAgeingScheduledJob(final ScheduledJobRunnerService scheduledJobRunnerService) {
        this.scheduledJobRunnerService = scheduledJobRunnerService;
    }

    public void execute() {
        logger.info("Running Job B " + new Date());

        this.scheduledJobRunnerService.updateLoanArrearsAgeingDetails();

        logger.info("Finishing Job B " + new Date());
    }
}
