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

public class UpdateLoanPaidInAdvanceScheduledJob {

    private final static Logger logger = LoggerFactory.getLogger(UpdateLoanPaidInAdvanceScheduledJob.class);

    private final ScheduledJobRunnerService scheduledJobRunnerService;

    public UpdateLoanPaidInAdvanceScheduledJob(final ScheduledJobRunnerService scheduledJobRunnerService) {
        this.scheduledJobRunnerService = scheduledJobRunnerService;
    }

    public void execute() {
        logger.info("Running Job UpdateLoanPaidInAdvanceScheduledJob " + new Date());

        this.scheduledJobRunnerService.updateLoanPaidInAdvance();

        logger.info("Finishing Job UpdateLoanPaidInAdvanceScheduledJob " + new Date());
    }
}
