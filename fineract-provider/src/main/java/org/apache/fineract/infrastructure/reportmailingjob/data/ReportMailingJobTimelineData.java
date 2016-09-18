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

import org.joda.time.LocalDate;

/** 
 * Immutable data object represent the timeline events of a report mailing job (creation)
 **/
@SuppressWarnings("unused")
public class ReportMailingJobTimelineData {
    private final LocalDate createdOnDate;
    private final String createdByUsername;
    private final String createdByFirstname;
    private final String createdByLastname;
    private final LocalDate updatedOnDate;
    private final String updatedByUsername;
    private final String updatedByFirstname;
    private final String updatedByLastname;
    
    /**
     * @param createdOnDate
     * @param createdByUsername
     * @param createdByFirstname
     * @param createdByLastname
     * @param updatedOnDate
     * @param updatedByUsername
     * @param updatedByFirstname
     * @param updatedByLastname
     */
    public ReportMailingJobTimelineData(LocalDate createdOnDate, String createdByUsername, String createdByFirstname,
            String createdByLastname, LocalDate updatedOnDate, String updatedByUsername, String updatedByFirstname,
            String updatedByLastname) {
        this.createdOnDate = createdOnDate;
        this.createdByUsername = createdByUsername;
        this.createdByFirstname = createdByFirstname;
        this.createdByLastname = createdByLastname;
        this.updatedOnDate = updatedOnDate;
        this.updatedByUsername = updatedByUsername;
        this.updatedByFirstname = updatedByFirstname;
        this.updatedByLastname = updatedByLastname;
    }
}
