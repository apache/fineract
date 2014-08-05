/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.data;

import java.util.Date;

public class JobDetailHistoryData {

    @SuppressWarnings("unused")
    private final Long version;

    @SuppressWarnings("unused")
    private final Date jobRunStartTime;

    @SuppressWarnings("unused")
    private final Date jobRunEndTime;

    @SuppressWarnings("unused")
    private final String status;

    @SuppressWarnings("unused")
    private final String jobRunErrorMessage;

    @SuppressWarnings("unused")
    private final String triggerType;

    @SuppressWarnings("unused")
    private final String jobRunErrorLog;

    public JobDetailHistoryData(final Long version, final Date jobRunStartTime, final Date jobRunEndTime, final String status,
            final String jobRunErrorMessage, final String triggerType, final String jobRunErrorLog) {
        this.version = version;
        this.jobRunStartTime = jobRunStartTime;
        this.jobRunEndTime = jobRunEndTime;
        this.status = status;
        this.jobRunErrorMessage = jobRunErrorMessage;
        this.triggerType = triggerType;
        this.jobRunErrorLog = jobRunErrorLog;
    }
}