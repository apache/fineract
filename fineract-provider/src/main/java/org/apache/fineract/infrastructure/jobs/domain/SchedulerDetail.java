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
package org.apache.fineract.infrastructure.jobs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "scheduler_detail")
public class SchedulerDetail extends AbstractPersistableCustom<Long> {

    @Column(name = "execute_misfired_jobs")
    private boolean executeInstructionForMisfiredJobs;

    @Column(name = "is_suspended")
    private boolean suspended;

    @Column(name = "reset_scheduler_on_bootup")
    private boolean resetSchedulerOnBootup;

    protected SchedulerDetail() {

    }

    public boolean isExecuteInstructionForMisfiredJobs() {
        return this.executeInstructionForMisfiredJobs;
    }

    public void updateExecuteInstructionForMisfiredJobs(final boolean executeInstructionForMisfiredJobs) {
        this.executeInstructionForMisfiredJobs = executeInstructionForMisfiredJobs;
    }

    public boolean isSuspended() {
        return this.suspended;
    }

    public void updateSuspendedState(final boolean suspended) {
        this.suspended = suspended;
    }

    public boolean isResetSchedulerOnBootup() {
        return this.resetSchedulerOnBootup;
    }

    public void updateResetSchedulerOnBootup(final boolean resetSchedulerOnBootup) {
        this.resetSchedulerOnBootup = resetSchedulerOnBootup;
    }
}
