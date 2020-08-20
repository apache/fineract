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
package org.apache.fineract.infrastructure.reportmailingjob.data;

import java.time.ZonedDateTime;

/**
 * Immutable data object representing report mailing job run history data.
 **/
public final class ReportMailingJobRunHistoryData {

    private final Long id;
    private final Long reportMailingJobId;
    private final ZonedDateTime startDateTime;
    private final ZonedDateTime endDateTime;
    private final String status;
    private final String errorMessage;
    private final String errorLog;

    /**
     * ReportMailingJobRunHistoryData private constructor
     **/
    private ReportMailingJobRunHistoryData(Long id, Long reportMailingJobId, ZonedDateTime startDateTime, ZonedDateTime endDateTime,
            String status, String errorMessage, String errorLog) {
        this.id = id;
        this.reportMailingJobId = reportMailingJobId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorLog = errorLog;
    }

    /**
     * creates an instance of the ReportMailingJobRunHistoryData class
     *
     * @return ReportMailingJobRunHistoryData object
     **/
    public static ReportMailingJobRunHistoryData newInstance(Long id, Long reportMailingJobId, ZonedDateTime startDateTime,
            ZonedDateTime endDateTime, String status, String errorMessage, String errorLog) {
        return new ReportMailingJobRunHistoryData(id, reportMailingJobId, startDateTime, endDateTime, status, errorMessage, errorLog);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the reportMailingJobId
     */
    public Long getReportMailingJobId() {
        return reportMailingJobId;
    }

    /**
     * @return the startDateTime
     */
    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * @return the endDateTime
     */
    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return the errorLog
     */
    public String getErrorLog() {
        return errorLog;
    }
}
