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
package org.apache.fineract.portfolio.savings.data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import org.joda.time.LocalDateTime;

public class SavingsAccountBlockNarrationHistoryData implements Serializable {

    private Long id;
    private LocalDateTime startDate;
    private LocalDate endDate;
    private String blockNarrationComment;
    private Long blockNarrationId;
    private String subStatus;
    private String blockNarrationValue;
    private String submittedByUsername;
    private String blockNarrationDateString;

    public SavingsAccountBlockNarrationHistoryData(Long id, Timestamp startDate, LocalDate endDate, String blockNarrationComment,
            Long blockNarration, String blockNarrationValue, String subStatus, String submittedByUsername) {
        this.id = id;
        this.startDate = LocalDateTime.fromDateFields(startDate);
        this.endDate = endDate;
        this.blockNarrationComment = blockNarrationComment;
        this.blockNarrationId = blockNarration;
        this.blockNarrationValue = blockNarrationValue;
        this.subStatus = subStatus;
        this.submittedByUsername = submittedByUsername;
        this.blockNarrationDateString = this.startDate.toString();
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getBlockNarrationComment() {
        return blockNarrationComment;
    }

    public Long getBlockNarrationId() {
        return blockNarrationId;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public String getBlockNarrationValue() {
        return blockNarrationValue;
    }

    public String getSubmittedByUsername() {
        return submittedByUsername;
    }

    public String getBlockNarrationDateString() {
        return blockNarrationDateString;
    }
}
