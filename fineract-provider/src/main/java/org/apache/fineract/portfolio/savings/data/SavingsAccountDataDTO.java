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

import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

public class SavingsAccountDataDTO {

    private final Client client;
    private final Group group;
    private final Long savingsProductId;
    private final LocalDate applicationDate;
    private final AppUser appliedBy;
    private final DateTimeFormatter fmt;

    public SavingsAccountDataDTO(final Client client, final Group group, final Long savingsProductId,
            final LocalDate applicationDate, final AppUser appliedBy, final DateTimeFormatter fmt) {
        this.client = client;
        this.group = group;
        this.savingsProductId = savingsProductId;
        this.applicationDate = applicationDate;
        this.appliedBy = appliedBy;
        this.fmt = fmt;
    }

    public Client getClient() {
        return this.client;
    }

    public Group getGroup() {
        return this.group;
    }

    public Long getSavingsProduct() {
        return this.savingsProductId;
    }

    public LocalDate getApplicationDate() {
        return this.applicationDate;
    }

    public AppUser getAppliedBy() {
        return this.appliedBy;
    }

    public DateTimeFormatter getFmt() {
        return this.fmt;
    }
}
