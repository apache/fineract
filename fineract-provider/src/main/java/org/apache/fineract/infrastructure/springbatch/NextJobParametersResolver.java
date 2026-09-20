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
package org.apache.fineract.infrastructure.springbatch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersIncrementer;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;

/**
 * Produces a unique {@link JobParameters} set for the next run of a job by advancing the {@code run.id} of its most
 * recent execution.
 * <p>
 * Spring Batch 6 removed {@code JobParametersBuilder(JobExplorer)}, whose {@code getNextJobParameters(Job)} did this.
 * Its natural replacement, {@code JobOperator#start(Job, JobParameters)}, silently discards the supplied parameters for
 * any job that declares an incrementer:
 *
 * <pre>
 * if (job.getJobParametersIncrementer() != null) {
 *     logger.warn("... Additional parameters will be ignored.");
 *     return startNextInstance(job);
 * }
 * return run(job, jobParameters);
 * </pre>
 *
 * Fineract has to pass its own parameters - the configured job parameters plus any custom ones - so the jobs
 * deliberately declare <em>no</em> incrementer and the increment is applied here instead. {@code start(..)} then takes
 * the parameter-respecting branch, which keeps the call sites off {@code JobLauncher#run}, deprecated for removal since
 * 6.0.
 * <p>
 * {@link RunIdIncrementer} still does the work, so the emitted parameters are identical to what a job-declared
 * incrementer produced: the same {@code run.id} key, as an identifying {@code Long}, continuing from the last
 * execution's value.
 */
public final class NextJobParametersResolver {

    private static final JobParametersIncrementer RUN_ID_INCREMENTER = new RunIdIncrementer();

    private NextJobParametersResolver() {}

    /**
     * @param jobRepository
     *            repository to read the last instance/execution from
     * @param job
     *            the job about to be launched
     * @return the last execution's parameters with {@code run.id} advanced, or a fresh set starting at
     *         {@code run.id = 1} when the job has no execution history
     */
    public static JobParameters resolve(final JobRepository jobRepository, final Job job) {
        final JobInstance lastInstance = jobRepository.getLastJobInstance(job.getName());
        final JobExecution lastExecution = lastInstance == null ? null : jobRepository.getLastJobExecution(lastInstance);
        final JobParameters parameters = lastExecution == null ? new JobParameters() : lastExecution.getJobParameters();
        return RUN_ID_INCREMENTER.getNext(parameters);
    }
}
