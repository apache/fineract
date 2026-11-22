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
package org.apache.fineract.infrastructure.reportmailingjob.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailAttachmentFileFormat;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_report_mailing_job", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unique_name") })
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReportMailingJob extends AbstractAuditableCustom {

    private static final long serialVersionUID = -2197602941230009227L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "recurrence", nullable = true)
    private String recurrence;

    @Column(name = "email_recipients", nullable = false)
    private String emailRecipients;

    @Column(name = "email_subject", nullable = false)
    private String emailSubject;

    @Column(name = "email_message", nullable = false)
    private String emailMessage;

    @Column(name = "email_attachment_file_format", nullable = false)
    private String emailAttachmentFileFormat;

    @ManyToOne
    @JoinColumn(name = "stretchy_report_id", nullable = false)
    private Report stretchyReport;

    @Column(name = "stretchy_report_param_map", nullable = true)
    private String stretchyReportParamMap;

    @Column(name = "previous_run_datetime", nullable = true)
    private LocalDateTime previousRunDateTime;

    @Column(name = "next_run_datetime", nullable = true)
    private LocalDateTime nextRunDateTime;

    @Column(name = "previous_run_status", nullable = true)
    private String previousRunStatus;

    @Column(name = "previous_run_error_log", nullable = true)
    private String previousRunErrorLog;

    @Column(name = "previous_run_error_message", nullable = true)
    private String previousRunErrorMessage;

    @Column(name = "number_of_runs", nullable = false)
    private Integer numberOfRuns;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_as_userid", nullable = false)
    private AppUser runAsUser;

    /**
     * create a new instance of the ReportMailingJob for a new entry
     *
     * @return ReportMailingJob object
     **/
    public static ReportMailingJob newInstance(final String name, final String description, final LocalDateTime startDateTime,
            final String recurrence, final String emailRecipients, final String emailSubject, final String emailMessage,
            final ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat, final Report stretchyReport,
            final String stretchyReportParamMap, final boolean isActive, final AppUser runAsUser) {
        return new ReportMailingJob().setName(name).setDescription(description).setStartDateTime(startDateTime).setRecurrence(recurrence)
                .setEmailRecipients(emailRecipients).setEmailSubject(emailSubject).setEmailMessage(emailMessage)
                .setEmailAttachmentFileFormat(emailAttachmentFileFormat.getValue()).setStretchyReport(stretchyReport)
                .setStretchyReportParamMap(stretchyReportParamMap).setActive(isActive).setDeleted(false).setRunAsUser(runAsUser);
    }

    /**
     * delete the report mailing job, set the isDeleted property to 1 and alter the name
     *
     *
     **/
    public void delete() {
        this.isDeleted = true;
        this.isActive = false;
        this.name = this.name + "_deleted_" + this.getId();
    }

    /**
     * increase the numberOfRuns by 1
     *
     *
     **/
    public void increaseNumberOfRunsByOne() {
        this.numberOfRuns++;
    }

}
