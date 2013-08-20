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
    @SuppressWarnings("unused")
    private ScheduledJobDetail scheduledJobDetail;

    @Column(name = "version")
    @SuppressWarnings("unused")
    private Long version;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    @SuppressWarnings("unused")
    private Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    @SuppressWarnings("unused")
    private Date endTime;

    @Column(name = "status")
    @SuppressWarnings("unused")
    private String status;

    @Column(name = "error_message")
    @SuppressWarnings("unused")
    private String errorMessage;

    @Column(name = "trigger_type")
    @SuppressWarnings("unused")
    private String triggerType;

    @Column(name = "error_log")
    @SuppressWarnings("unused")
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
