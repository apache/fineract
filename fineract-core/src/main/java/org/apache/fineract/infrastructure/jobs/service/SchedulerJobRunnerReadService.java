/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.jobs.service;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.apache.fineract.infrastructure.core.api.IdTypeResolver;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.jobs.data.JobDetailData;
import org.apache.fineract.infrastructure.jobs.data.JobDetailHistoryData;

public interface SchedulerJobRunnerReadService {

    List<JobDetailData> findAllJobDetails();

    JobDetailData retrieveOne(@NotNull IdTypeResolver.IdType idType, String identifier);

    Page<JobDetailHistoryData> retrieveJobHistory(@NotNull IdTypeResolver.IdType idType, String identifier,
            SearchParameters searchParameters);

    @NotNull
    Long retrieveId(@NotNull IdTypeResolver.IdType idType, String identifier);

    boolean isUpdatesAllowed();
}
