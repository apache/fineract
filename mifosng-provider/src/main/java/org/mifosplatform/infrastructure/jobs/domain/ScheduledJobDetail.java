package org.mifosplatform.infrastructure.jobs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "job")
public class ScheduledJobDetail extends AbstractPersistable<Long> {

    @Column(name = "name")
    private String jobName;

    @SuppressWarnings("unused")
    @Column(name = "display_name")
    private String jobDisplayName;

    @Column(name = "cron_expression")
    private String croneExpression;

    @SuppressWarnings("unused")
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "task_priority")
    private Short taskPriority;

    @Column(name = "group_name")
    private String groupName;

    @SuppressWarnings("unused")
    @Column(name = "previous_run_start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date previousRunStartTime;

    @SuppressWarnings("unused")
    @Column(name = "next_run_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextRunTime;

    @Column(name = "job_key")
    private String jobKey;

    @SuppressWarnings("unused")
    @Column(name = "initializing_errorlog")
    private String errorLog;

    @Column(name = "is_active")
    private boolean activeSchedular;

    @Column(name = "currently_running")
    private boolean currentlyRunning;

    public ScheduledJobDetail() {

    }

    public ScheduledJobDetail(final String jobName, final String jobDisplayName, final String croneExpression, Date createTime,
            final Short taskPriority, final String groupName, final Date previousRunStartTime, final Date nextRunTime, final String jobKey,
            final String errorLog, final boolean activeSchedular, final boolean currentlyRunning) {
        this.jobName = jobName;
        this.jobDisplayName = jobDisplayName;
        this.croneExpression = croneExpression;
        this.createTime = createTime;
        this.taskPriority = taskPriority;
        this.groupName = groupName;
        this.previousRunStartTime = previousRunStartTime;
        this.nextRunTime = nextRunTime;
        this.jobKey = jobKey;
        this.errorLog = errorLog;
        this.activeSchedular = activeSchedular;
        this.currentlyRunning = currentlyRunning;
    }

    public String getJobName() {
        return this.jobName;
    }

    public String getCroneExpression() {
        return this.croneExpression;
    }

    public Short getTaskPriority() {
        return this.taskPriority;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getJobKey() {
        return this.jobKey;
    }

    public boolean isActiveSchedular() {
        return this.activeSchedular;
    }

    public void updateCroneExpression(final String croneExpression) {
        this.croneExpression = croneExpression;
    }

    public void updatePreviousRunStartTime(final Date previousRunStartTime) {
        this.previousRunStartTime = previousRunStartTime;
    }

    public void updateNextRunTime(final Date nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public void updateJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public void updateErrorLog(final String errorLog) {
        this.errorLog = errorLog;
    }

    public boolean isCurrentlyRunning() {
        return this.currentlyRunning;
    }

    public void updateCurrentlyRunningStatus(boolean currentlyRunning) {
        this.currentlyRunning = currentlyRunning;
    }

}
