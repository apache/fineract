package org.mifosplatform.infrastructure.jobs.data;

import java.util.Date;

public class JobDetailHistoryData {

    private Long version;
    private Date jobRunStartTime;
    private Date jobRunEndTime;
    private String status;
    private String jobRunErrorMessage;
    private String triggerType;
    private String jobRunErrorLog;

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