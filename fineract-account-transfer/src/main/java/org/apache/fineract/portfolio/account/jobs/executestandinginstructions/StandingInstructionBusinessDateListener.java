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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;

/**
 * Runs the worker step on the business date, not the close-of-business date.
 *
 * <p>
 * Every remote-partitioning worker message passes through {@code InputChannelInterceptor}, which sets
 * {@link ActionContext#COB} because the loan COB worker needs it. Under that context the business date resolves to the
 * COB date, one day behind. Standing instructions are not part of the close of business: the partitioner counts the due
 * set on the business date, so the worker must read, check dues and post transfers on that same date. Without this
 * listener a loan installment due today is not yet due on the worker, and a transfer would be dated yesterday.
 * </p>
 *
 * <p>
 * The step has no task executor, so the chunks run on the thread this listener sets.
 * </p>
 */
public class StandingInstructionBusinessDateListener implements StepExecutionListener {

    @Override
    public void beforeStep(final StepExecution stepExecution) {
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        // Hand the thread back in the context the interceptor set; the interceptor resets it after the message.
        ThreadLocalContextUtil.setActionContext(ActionContext.COB);
        return stepExecution.getExitStatus();
    }
}
