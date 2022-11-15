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
package org.apache.fineract.infrastructure.jobs.service.jobname;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobNameService {

    private final Collection<JobNameProvider> providers;

    public JobNameData getJobByHumanReadableName(String jobName) {
        Optional<JobNameData> optionalJob = getJobNames().stream().filter(jn -> jobName.equals(jn.getHumanReadableName())).findAny();
        return optionalJob.orElseThrow(() -> new IllegalArgumentException("Job not found by name: " + jobName));
    }

    private Set<JobNameData> getJobNames() {
        return providers.stream().map(JobNameProvider::provide).flatMap(Collection::stream).collect(toSet());
    }
}
