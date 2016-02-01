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
package org.apache.fineract.organisation.staff.data;

import java.util.Collection;

import org.apache.fineract.organisation.office.data.OfficeData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing staff data.
 */
public class StaffData {

    private final Long id;
    private final String externalId;
    private final String firstname;
    private final String lastname;
    private final String displayName;
    private final String mobileNo;
    private final Long officeId;
    private final String officeName;
    private final Boolean isLoanOfficer;
    private final Boolean isActive;
    private final LocalDate joiningDate;

    @SuppressWarnings("unused")
    private final Collection<OfficeData> allowedOffices;

    public static StaffData templateData(final StaffData staff, final Collection<OfficeData> allowedOffices) {
        return new StaffData(staff.id, staff.firstname, staff.lastname, staff.displayName, staff.officeId, staff.officeName,
                staff.isLoanOfficer, staff.externalId, staff.mobileNo, allowedOffices, staff.isActive, staff.joiningDate);
    }

    public static StaffData lookup(final Long id, final String displayName) {
        return new StaffData(id, null, null, displayName, null, null, null, null, null, null, null, null);
    }

    public static StaffData instance(final Long id, final String firstname, final String lastname, final String displayName,
            final Long officeId, final String officeName, final Boolean isLoanOfficer, final String externalId, final String mobileNo,
            final boolean isActive, final LocalDate joiningDate) {
        return new StaffData(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer, externalId, mobileNo, null,
                isActive, joiningDate);
    }

    private StaffData(final Long id, final String firstname, final String lastname, final String displayName, final Long officeId,
            final String officeName, final Boolean isLoanOfficer, final String externalId, final String mobileNo,
            final Collection<OfficeData> allowedOffices, final Boolean isActive, final LocalDate joiningDate) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.displayName = displayName;
        this.officeName = officeName;
        this.isLoanOfficer = isLoanOfficer;
        this.externalId = externalId;
        this.officeId = officeId;
        this.mobileNo = mobileNo;
        this.allowedOffices = allowedOffices;
        this.isActive = isActive;
        this.joiningDate = joiningDate;
    }

    public Long getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getOfficeName() {
        return this.officeName;
    }

    public LocalDate getJoiningDate() {
        return this.joiningDate;
    }

    public Long getOfficeId() {
        return this.officeId;
    }
}