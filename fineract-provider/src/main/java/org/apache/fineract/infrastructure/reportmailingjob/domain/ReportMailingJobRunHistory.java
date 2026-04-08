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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_report_mailing_job_run_history")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReportMailingJobRunHistory extends AbstractPersistableCustom<Long> {

    private static final long serialVersionUID = -3757370929988421076L;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private ReportMailingJob reportMailingJob;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "error_log", nullable = false)
    private String errorLog;

    /**
     * Creates an instance of the ReportMailingJobRunHistory class
     *
     * @return ReportMailingJobRunHistory object
     **/
    public static ReportMailingJobRunHistory newInstance(final ReportMailingJob reportMailingJob, final LocalDateTime startDateTime,
            final LocalDateTime endDateTime, final String status, final String errorMessage, final String errorLog) {
        return new ReportMailingJobRunHistory().setReportMailingJob(reportMailingJob).setStartDateTime(startDateTime)
                .setEndDateTime(endDateTime).setStatus(status).setErrorMessage(errorMessage).setErrorLog(errorLog);
    }
}
