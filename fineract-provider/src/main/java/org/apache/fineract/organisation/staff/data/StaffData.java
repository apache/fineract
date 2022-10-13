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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.office.data.OfficeData;

/**
 * Immutable data object representing staff data.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class StaffData implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String externalId;
    private String firstname;
    private String lastname;
    private String displayName;
    private String mobileNo;
    private Long officeId;
    private String officeName;
    private Boolean isLoanOfficer;
    private Boolean isActive;
    private LocalDate joiningDate;

    // import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;

    public static StaffData importInstance(String externalId, String firstname, String lastname, String mobileNo, Long officeId,
            Boolean isLoanOfficer, Boolean isActive, LocalDate joinedOnDate, Integer rowIndex, String locale, String dateFormat) {
        return new StaffData().setExternalId(externalId).setFirstname(firstname).setLastname(lastname).setMobileNo(mobileNo)
                .setOfficeId(officeId).setIsLoanOfficer(isLoanOfficer).setIsActive(isActive).setJoiningDate(joinedOnDate)
                .setRowIndex(rowIndex).setLocale(locale).setDateFormat(dateFormat);

    }

    @SuppressWarnings("unused")
    private Collection<OfficeData> allowedOffices;

    public static StaffData templateData(final StaffData staff, final Collection<OfficeData> allowedOffices) {
        return new StaffData().setId(staff.id).setFirstname(staff.firstname).setLastname(staff.lastname).setDisplayName(staff.displayName)
                .setOfficeId(staff.officeId).setOfficeName(staff.officeName).setIsLoanOfficer(staff.isLoanOfficer)
                .setExternalId(staff.externalId).setMobileNo(staff.mobileNo).setAllowedOffices(allowedOffices).setIsActive(staff.isActive)
                .setJoiningDate(staff.joiningDate);
    }

    public static StaffData lookup(final Long id, final String displayName) {
        return new StaffData().setId(id).setDisplayName(displayName);
    }

    public static StaffData instance(final Long id, final String firstname, final String lastname, final String displayName,
            final Long officeId, final String officeName, final Boolean isLoanOfficer, final String externalId, final String mobileNo,
            final boolean isActive, final LocalDate joiningDate) {
        return new StaffData().setId(id).setFirstname(firstname).setLastname(lastname).setDisplayName(displayName).setOfficeId(officeId)
                .setOfficeName(officeName).setIsLoanOfficer(isLoanOfficer).setExternalId(externalId).setMobileNo(mobileNo)
                .setIsActive(isActive).setJoiningDate(joiningDate);
    }
}
