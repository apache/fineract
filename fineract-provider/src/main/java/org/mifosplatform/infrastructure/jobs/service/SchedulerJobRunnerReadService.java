/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.jobs.data.JobDetailData;
import org.mifosplatform.infrastructure.jobs.data.JobDetailHistoryData;
import org.mifosplatform.infrastructure.core.service.SearchParameters;

public interface SchedulerJobRunnerReadService {

    public List<JobDetailData> findAllJobDeatils();

    public JobDetailData retrieveOne(Long jobId);

    public Page<JobDetailHistoryData> retrieveJobHistory(Long jobId, SearchParameters searchParameters);

    public boolean isUpdatesAllowed();

}
