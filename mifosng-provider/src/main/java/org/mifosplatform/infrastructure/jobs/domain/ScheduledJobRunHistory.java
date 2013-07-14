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

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "job_id")
    private ScheduledJobDetail scheduledJobDetail;

    @SuppressWarnings("unused")
    @Column(name = "version")
    private Long version;

    @SuppressWarnings("unused")
    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @SuppressWarnings("unused")
    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @SuppressWarnings("unused")
    @Column(name = "status")
    private String status;

    @SuppressWarnings("unused")
    @Column(name = "error_message")
    private String errorMessage;

    @SuppressWarnings("unused")
    @Column(name = "trigger_type")
    private String triggerType;

    @SuppressWarnings("unused")
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
