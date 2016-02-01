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
package org.apache.fineract.accounting.closure.data;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.fineract.organisation.office.data.OfficeData;
import org.joda.time.LocalDate;

/**
 * Immutable object representing a General Ledger Account
 * 
 * Note: no getter/setters required as google-gson will produce json from fields
 * of object.
 */
public class GLClosureData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final LocalDate closingDate;
    @SuppressWarnings("unused")
    private final boolean deleted;
    @SuppressWarnings("unused")
    private final LocalDate createdDate;
    @SuppressWarnings("unused")
    private final LocalDate lastUpdatedDate;
    @SuppressWarnings("unused")
    private final Long createdByUserId;
    @SuppressWarnings("unused")
    private final String createdByUsername;
    @SuppressWarnings("unused")
    private final Long lastUpdatedByUserId;
    @SuppressWarnings("unused")
    private final String lastUpdatedByUsername;
    @SuppressWarnings("unused")
    private final String comments;

    private Collection<OfficeData> allowedOffices = new ArrayList<>();

    public GLClosureData(final Long id, final Long officeId, final String officeName, final LocalDate closingDate, final boolean deleted,
            final LocalDate createdDate, final LocalDate lastUpdatedDate, final Long createdByUserId, final String createdByUsername,
            final Long lastUpdatedByUserId, final String lastUpdatedByUsername, final String comments) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.closingDate = closingDate;
        this.deleted = deleted;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.lastUpdatedByUserId = lastUpdatedByUserId;
        this.lastUpdatedByUsername = lastUpdatedByUsername;
        this.comments = comments;
        this.allowedOffices = null;
    }

    public final Collection<OfficeData> getAllowedOffices() {
        return this.allowedOffices;
    }

    public void setAllowedOffices(final Collection<OfficeData> allowedOffices) {
        this.allowedOffices = allowedOffices;
    }

}