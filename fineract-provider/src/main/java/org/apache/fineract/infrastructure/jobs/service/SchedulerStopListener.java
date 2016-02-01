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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Global job Listener class to Stop the temporary scheduler once job execution
 * completes
 */
@Component
public class SchedulerStopListener implements JobListener {

    private static final String name = "Singlr Trigger Global Listner";

    // MIFOSX-1184: This class cannot use constructor injection, because one of
    // its dependencies (SchedulerStopListener) has a circular dependency to
    // itself. So, slightly differently from how it's done elsewhere in this
    // code base, the following fields are not final, and there is no
    // constructor, but setters.

    private JobRegisterService jobRegisterService;

    @Autowired
    public void setJobRegisterService(JobRegisterService jobRegisterService) {
        this.jobRegisterService = jobRegisterService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void jobToBeExecuted(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, @SuppressWarnings("unused") final JobExecutionException jobException) {
        final String schedulerName = context.getTrigger().getJobDataMap().getString(SchedulerServiceConstants.SCHEDULER_NAME);
        if (schedulerName != null) {
            final Thread newThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    SchedulerStopListener.this.jobRegisterService.stopScheduler(schedulerName);
                }
            });
            newThread.run();
        }
    }

}
