package org.mifosplatform.infrastructure.jobs.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SchedulerJobApiConstants {

    // response parameters
    public static final String jobIdentifierParamName = "jobId";
    public static final String displayNameParamName = "displayName";
    public static final String nextRunTimeParamName = "nextRunTime";
    public static final String initializingErrorParamName = "initializingError";
    public static final String jobActiveStatusParamName = "active";
    public static final String currentlyRunningParamName = "currentlyRunning";
    public static final String lastRunHistoryObjParamName = "lastRunHistory";
    

    public static final String versionParamName = "version";
    public static final String jobRunStartTimeParamName = "jobRunStartTime";
    public static final String jobRunEndTimeParamName = "jobRunEndTime";
    public static final String statusParamName = "status";
    public static final String jobRunErrorMessageParamName = "jobRunErrorMessage";
    public static final String triggerTypeParamName = "triggerType";
    public static final String jobRunErrorLogParamName = "jobRunErrorLog";

    public static final Set<String> JOB_DETAIL_RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(jobIdentifierParamName,
            displayNameParamName, nextRunTimeParamName, initializingErrorParamName, jobActiveStatusParamName,currentlyRunningParamName, lastRunHistoryObjParamName));

    public static final Set<String> JOB_HISTORY_RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(versionParamName,
            jobRunStartTimeParamName, jobRunEndTimeParamName, statusParamName, jobRunErrorMessageParamName, triggerTypeParamName,
            jobRunErrorLogParamName));

}
