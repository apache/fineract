package org.mifosplatform.infrastructure.jobs.data;

import java.util.Date;

public class JobDetailData {

    private Long jobId;

    private String displayName;

    private Date nextRunTime;

    private String initializingError;

    private boolean active;

    private boolean currentlyRunning;

    private JobDetailHistoryData lastRunHistory;

    public JobDetailData(final Long jobId, final String displayName, final Date nextRunTime, final String initializingError,
            final boolean active, final boolean currentlyRunning, final JobDetailHistoryData lastRunHistory) {
        this.jobId = jobId;
        this.displayName = displayName;
        this.nextRunTime = nextRunTime;
        this.initializingError = initializingError;
        this.active = active;
        this.lastRunHistory = lastRunHistory;
        this.currentlyRunning = currentlyRunning;
    }

}
