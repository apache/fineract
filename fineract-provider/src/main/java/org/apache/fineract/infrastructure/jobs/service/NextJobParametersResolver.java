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

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersIncrementer;
import org.springframework.batch.core.repository.JobRepository;

/**
 * Spring Batch 6's {@link org.springframework.batch.core.launch.JobOperator#start(Job, JobParameters)} silently ignores
 * the passed {@link JobParameters} whenever the job defines a {@link JobParametersIncrementer}, delegating to
 * {@code startNextInstance(job)} instead. Fineract's job launchers still need to pass their own parameters (e.g. custom
 * job parameter ids, business dates) while jobs are identified with a {@code RunIdIncrementer}, so this replicates
 * {@code SimpleJobOperator.startNextInstance}'s incrementer logic and merges Fineract's parameters on top, so the
 * result can be run directly through {@link org.springframework.batch.core.launch.JobLauncher#run(Job, JobParameters)}
 * (also inherited by {@link org.springframework.batch.core.launch.JobOperator}).
 */
public final class NextJobParametersResolver {

    private NextJobParametersResolver() {}

    public static JobParameters getNextJobParameters(JobRepository jobRepository, Job job, JobParameters jobParameters) {
        JobParametersIncrementer incrementer = job.getJobParametersIncrementer();
        if (incrementer == null) {
            return jobParameters;
        }
        JobInstance lastInstance = jobRepository.getLastJobInstance(job.getName());
        JobParameters previousParameters;
        if (lastInstance == null) {
            previousParameters = new JobParameters();
        } else {
            JobExecution previousExecution = jobRepository.getLastJobExecution(lastInstance);
            previousParameters = previousExecution == null ? new JobParameters() : previousExecution.getJobParameters();
        }
        JobParameters nextParameters = incrementer.getNext(previousParameters);
        return new JobParametersBuilder(nextParameters).addJobParameters(jobParameters).toJobParameters();
    }
}
