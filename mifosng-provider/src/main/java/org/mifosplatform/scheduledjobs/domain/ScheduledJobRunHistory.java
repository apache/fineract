package org.mifosplatform.scheduledjobs.domain;

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
@Table(name = "scheduled_job_runhistory")
public class ScheduledJobRunHistory extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "job_id")
    private ScheduledJobDetails scheduledJobDetails;

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

    @Column(name = "errormessage")
    private String errorMessage;

    @Column(name = "triggertype")
    private String triggerType;

    @Column(name = "errorlog")
    private String errorLog;

    public ScheduledJobRunHistory() {

    }

    public ScheduledJobRunHistory(final ScheduledJobDetails scheduledJobDetails, final Long version, final Date startTime,
            final Date endTime, final String status, final String errorMessage, final String triggerType, final String errorLog) {
        this.scheduledJobDetails = scheduledJobDetails;
        this.version = version;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.errorMessage = errorMessage;
        this.triggerType = triggerType;
        this.errorLog = errorLog;
    }

}
