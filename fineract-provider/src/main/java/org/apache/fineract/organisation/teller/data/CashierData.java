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
package org.apache.fineract.organisation.teller.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.staff.data.StaffData;

/**
 * Represents a cashier, providing access to the cashier's office, staff information, teller, and more.
 *
 * @author Markus Geiss
 *
 * @since 2.0.0
 * @see org.apache.fineract.organisation.teller.domain.Cashier
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public final class CashierData implements Serializable {

    private Long id;
    private Long tellerId;
    private Long officeId;
    private Long staffId;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isFullDay;
    private String startTime;
    private String endTime;

    // Template fields
    private String officeName;
    private String tellerName;
    private String staffName;
    private Collection<StaffData> staffOptions;

    /**
     * Creates a new cashier.
     *
     * <p>
     * The valid from/to dates may be used to define a time period in which the cashier is assignable to a teller.
     * </p>
     *
     * <p>
     * The start/end times may be used to define a time period in which the cashier works part time.
     * </p>
     *
     * @param id
     *            the primary key of this cashier
     * @param officeId
     *            the primary key of the related office
     * @param officeName
     *            the primary key of the related staff
     * @param staffId
     *            the primary key of the related teller
     * @param staffName
     * @param tellerId
     *            the primary key of the related teller
     * @param tellerName
     * @param description
     *            the description of this cashier
     * @param startDate
     *            the valid from date of this cashier
     * @param endDate
     *            the valid to date of this cashier
     * @param isFullDay
     *            the part time flag of this cashier
     * @param startTime
     *            the start time of this cashier
     * @param endTime
     *            the end time of this cashier
     * @return
     */
    public static CashierData instance(final Long id, final Long officeId, String officeName, final Long staffId, final String staffName,
            final Long tellerId, final String tellerName, final String description, final LocalDate startDate, final LocalDate endDate,
            final Boolean isFullDay, final String startTime, final String endTime) {
        return new CashierData().setId(id).setOfficeId(officeId).setOfficeName(officeName).setStaffId(staffId).setStaffName(staffName)
                .setTellerId(tellerId).setTellerName(tellerName).setDescription(description).setStartDate(startDate).setEndDate(endDate)
                .setIsFullDay(isFullDay).setStartTime(startTime).setEndTime(endTime);
    }

    /*
     * Creates a new cashier.
     */
    public static CashierData template(final Long officeId, final String officeName, final Long tellerId, final String tellerName,
            final Collection<StaffData> staffOptions) {
        return new CashierData().setOfficeId(officeId).setOfficeName(officeName).setTellerId(tellerId).setTellerName(tellerName)
                .setStaffOptions(staffOptions);
    }
}
