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
package org.apache.fineract.cob.listener;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.ApplicationContext;

public class COBExecutionListenerRunner implements JobExecutionListener {

    private final List<FineractCOBBeforeJobListener> beforeJobListeners = new ArrayList<>();
    private final List<FineractCOBAfterJobListener> afterJobListeners = new ArrayList<>();

    @SuppressFBWarnings({ "CT_CONSTRUCTOR_THROW" })
    public COBExecutionListenerRunner(ApplicationContext applicationContext, String jobName) {
        addBeforeListeners(applicationContext, jobName);
        addAfterListeners(applicationContext, jobName);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        beforeJobListeners.forEach(beforeJobListener -> beforeJobListener.beforeJob(jobExecution));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        afterJobListeners.forEach(afterJobListener -> afterJobListener.afterJob(jobExecution));
    }

    private void addAfterListeners(ApplicationContext applicationContext, String jobName) {
        List<String> afterListenerClassNames = Arrays.stream(applicationContext.getBeanNamesForType(FineractCOBAfterJobListener.class))
                .toList();
        for (String afterListenerClassName : afterListenerClassNames) {
            FineractCOBAfterJobListener afterListener = (FineractCOBAfterJobListener) applicationContext.getBean(afterListenerClassName);
            if (jobName.equals(afterListener.getJobName())) {
                afterJobListeners.add(afterListener);
            }
        }
    }

    private void addBeforeListeners(ApplicationContext applicationContext, String jobName) {
        List<String> beforeListenerClassNames = Arrays.stream(applicationContext.getBeanNamesForType(FineractCOBBeforeJobListener.class))
                .toList();
        for (String beforeListenerClassName : beforeListenerClassNames) {
            FineractCOBBeforeJobListener beforeListener = (FineractCOBBeforeJobListener) applicationContext
                    .getBean(beforeListenerClassName);
            if (jobName.equals(beforeListener.getJobName())) {
                beforeJobListeners.add(beforeListener);
            }
        }
    }
}
