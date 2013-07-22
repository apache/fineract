package org.mifosplatform.infrastructure.jobs.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.jobs.data.JobDetailData;
import org.mifosplatform.infrastructure.jobs.data.JobDetailHistoryData;
import org.mifosplatform.portfolio.group.service.SearchParameters;

public interface SchedulerJobRunnerReadService {

    public List<JobDetailData> findAllJobDeatils();

    public JobDetailData retrieveOne(Long jobId);

    public Page<JobDetailHistoryData> retrieveJobHistory(Long jobId, SearchParameters searchParameters);

    public boolean isUpdatesAllowed();

}
