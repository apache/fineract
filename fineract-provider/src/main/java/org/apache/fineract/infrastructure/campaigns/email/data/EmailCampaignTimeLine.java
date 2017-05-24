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
package org.apache.fineract.infrastructure.campaigns.email.data;

import org.joda.time.LocalDate;

public class EmailCampaignTimeLine {
    private final LocalDate submittedOnDate;
    private final String submittedByUsername;
    private final LocalDate activatedOnDate;
    private final String activatedByUsername;
    private final LocalDate closedOnDate;
    private final String closedByUsername;

    public EmailCampaignTimeLine(final LocalDate submittedOnDate, final String submittedByUsername,
                                 final LocalDate activatedOnDate, final String activatedByUsername, final LocalDate closedOnDate, final String closedByUsername) {
        this.submittedOnDate = submittedOnDate;
        this.submittedByUsername = submittedByUsername;
        this.activatedOnDate = activatedOnDate;
        this.activatedByUsername = activatedByUsername;
        this.closedOnDate = closedOnDate;
        this.closedByUsername = closedByUsername;
    }
}
