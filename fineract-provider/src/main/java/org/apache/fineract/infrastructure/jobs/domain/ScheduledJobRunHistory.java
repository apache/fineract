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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "job_run_history")
public class ScheduledJobRunHistory extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "job_id")
    private ScheduledJobDetail scheduledJobDetail;

    @Column(name = "version")
    private Long version;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "trigger_type")
    private String triggerType;

    @Column(name = "error_log")
    private String errorLog;

    public ScheduledJobRunHistory() {

    }

    public ScheduledJobRunHistory(final ScheduledJobDetail scheduledJobDetail, final Long version, final Date startTime,
            final Date endTime, final String status, final String errorMessage, final String triggerType, final String errorLog) {
        this.scheduledJobDetail = scheduledJobDetail;
        this.version = version;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.errorMessage = errorMessage;
        this.triggerType = triggerType;
        this.errorLog = errorLog;
    }

}
