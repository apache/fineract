/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "job_run_history")
public class ScheduledJobRunHistory extends AbstractPersistable<Long> {

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
