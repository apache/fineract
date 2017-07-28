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
package org.apache.fineract.organisation.teller.domain;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides the base model for a cashier. Represents a row in the
 * &quot;m_cashiers&quot; database table, with each column mapped to a property
 * of this class.
 * 
 * @author Markus Geiss
 * @since 2.0.0
 */
@Entity
@Table(name = "m_cashiers", uniqueConstraints = { @UniqueConstraint(name = "ux_cashiers_staff_teller", columnNames = { "staff_id",
        "teller_id" }) })
public class Cashier extends AbstractPersistableCustom<Long> {

    // ManyToOne(fetch = FetchType.LAZY)
    // JoinColumn(name = "office_id", nullable = false)
    @Transient
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teller_id", nullable = false)
    private Teller teller;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "full_day", nullable = true)
    private Boolean isFullDay;

    @Column(name = "start_time", nullable = true, length = 10)
    private String startTime;

    @Column(name = "end_time", nullable = true, length = 10)
    private String endTime;

    /**
     * Creates a new cashier.
     */
    public Cashier() {
        super();
    }

    public static Cashier fromJson(final Office cashierOffice, final Teller teller, final Staff staff, final String startTime,
            final String endTime, final JsonCommand command) {
        // final Long tellerId = teller.getId();
        // final Long staffId = command.longValueOfParameterNamed("staffId");
        final String description = command.stringValueOfParameterNamed("description");
        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");
        final Boolean isFullDay = command.booleanObjectValueOfParameterNamed("isFullDay");
        /*
         * final String startTime =
         * command.stringValueOfParameterNamed("startTime"); final String
         * endTime = command.stringValueOfParameterNamed("endTime");
         */

        return new Cashier(cashierOffice, teller, staff, description, startDate, endDate, isFullDay, startTime, endTime);
    }

    public Cashier(Office office, Teller teller, Staff staff, String description, LocalDate startDate, LocalDate endDate,
            Boolean isFullDay, String startTime, String endTime) {
        this.office = office;
        this.teller = teller;
        this.staff = staff;
        this.description = description;
        this.startDate = startDate.toDate();
        this.endDate = endDate.toDate();
        this.isFullDay = isFullDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        final String startDateParamName = "startDate";
        if (command.isChangeInLocalDateParameterNamed(startDateParamName, getStartLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(startDateParamName);
            actualChanges.put(startDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
            this.startDate = newValue.toDate();
        }

        final String endDateParamName = "endDate";
        if (command.isChangeInLocalDateParameterNamed(endDateParamName, getEndLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(endDateParamName);
            actualChanges.put(endDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(endDateParamName);
            this.endDate = newValue.toDate();
        }

        final Boolean isFullDay = command.booleanObjectValueOfParameterNamed("isFullDay");

        final String isFullDayParamName = "isFullDay";
        if (command.isChangeInBooleanParameterNamed(isFullDayParamName, this.isFullDay)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isFullDayParamName);
            actualChanges.put(isFullDayParamName, newValue);
            /*
             * this.startTime="00"; this.endTime="00";
             */
            this.isFullDay = newValue;
        }

        if (!isFullDay) {
            String newStartHour = "";
            String newStartMin = "";
            String newEndHour = "";
            String newEndMin = "";
            final String hourStartTimeParamName = "hourStartTime";
            final String minStartTimeParamName = "minStartTime";
            final String hourEndTimeParamName = "hourEndTime";
            final String minEndTimeParamName = "minEndTime";
            if (command.isChangeInLongParameterNamed(hourStartTimeParamName, this.getHourFromStartTime())
                    || command.isChangeInLongParameterNamed(minStartTimeParamName, this.getMinFromStartTime())) {
                newStartHour = command.stringValueOfParameterNamed(hourStartTimeParamName);
                if(newEndHour.equalsIgnoreCase("0")){
                    newEndHour= newEndHour + "0";
                }
                actualChanges.put(hourStartTimeParamName, newStartHour);
                newStartMin = command.stringValueOfParameterNamed(minStartTimeParamName);
                if(newStartMin.equalsIgnoreCase("0")){
                    newStartMin= newStartMin + "0";
                }
                actualChanges.put(minStartTimeParamName, newStartMin);
                this.startTime = newStartHour + ":" + newStartMin;
            }

            if (command.isChangeInLongParameterNamed(hourEndTimeParamName, this.getHourFromEndTime())
                    || command.isChangeInLongParameterNamed(minEndTimeParamName, this.getMinFromEndTime())) {
                newEndHour = command.stringValueOfParameterNamed(hourEndTimeParamName);
                if(newEndHour.equalsIgnoreCase("0")){
                    newEndHour= newEndHour + "0";
                }
                actualChanges.put(hourEndTimeParamName, newEndHour);
                newEndMin = command.stringValueOfParameterNamed(minEndTimeParamName);
                if(newEndMin.equalsIgnoreCase("0")){
                    newEndMin= newEndMin + "0";
                }
                actualChanges.put(minEndTimeParamName, newEndMin);
                this.endTime = newEndHour + ":" + newEndMin;
            }

        }

        return actualChanges;
    }

    /**
     * Returns the office of this cashier.
     * 
     * @return the office of this cashier
     * @see org.apache.fineract.organisation.office.domain.Office
     */
    public Office getOffice() {
        return office;
    }

    public Long getHourFromStartTime() {
        if (this.startTime != null && !this.startTime.equalsIgnoreCase("")) {
            String[] extractHourFromStartTime = this.startTime.split(":");
            Long hour = Long.parseLong(extractHourFromStartTime[1]);
            return hour;
        }
        return null;
    }

    public Long getMinFromStartTime() {
        if (this.startTime != null && !this.startTime.equalsIgnoreCase("")) {
            String[] extractMinFromStartTime = this.startTime.split(":");
            Long min = Long.parseLong(extractMinFromStartTime[1]);
            return min;
        }
        return null;
    }

    public Long getHourFromEndTime() {
        if (this.endTime != null && !this.endTime.equalsIgnoreCase("")) {
            String[] extractHourFromEndTime = this.endTime.split(":");
            Long hour = Long.parseLong(extractHourFromEndTime[0]);
            return hour;
        }
        return null;
    }

    public Long getMinFromEndTime() {
        if (this.endTime != null && !this.endTime.equalsIgnoreCase("")) {
            String[] extractMinFromEndTime = this.endTime.split(":");
            Long min = Long.parseLong(extractMinFromEndTime[1]);
            return min;
        }
        return null;
    }

    /**
     * Sets the office of this cashier.
     * 
     * @param office
     *            the office of this cashier
     * @see org.apache.fineract.organisation.office.domain.Office
     */
    public void setOffice(Office office) {
        this.office = office;
    }

    /**
     * Returns the staff of this cashier.
     * 
     * @return the staff of this cashier
     * @see org.apache.fineract.organisation.staff.domain.Staff
     */
    public Staff getStaff() {
        return staff;
    }

    /**
     * Sets the staff of this cashier.
     * 
     * @param staff
     *            the staff of this cashier
     * @see org.apache.fineract.organisation.staff.domain.Staff
     */
    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    /**
     * Returns the teller of this cashier.
     * 
     * @return the teller of this cashier
     * @see org.apache.fineract.organisation.teller.domain.Teller
     */
    public Teller getTeller() {
        return teller;
    }

    /**
     * Sets the teller of this cashier.
     * 
     * @param teller
     *            the teller of this cashier
     * @see org.apache.fineract.organisation.teller.domain.Teller
     */
    public void setTeller(Teller teller) {
        this.teller = teller;
    }

    /**
     * Returns the description of this cashier. .
     * 
     * @return the description of this cashier or {@code null} if not present.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this cashier.
     * 
     * @param description
     *            the description of this cashier
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the valid from date of this cashier.
     * 
     * <p>
     * The valid from/to dates may be used to define a time period in which the
     * cashier is assignable to a teller.
     * </p>
     * 
     * @return the valid from date of this cashier
     */
    public Date getStartDate() {
        return startDate;
    }

    public LocalDate getStartLocalDate() {
        LocalDate startLocalDate = null;
        if (this.startDate != null) {
            startLocalDate = LocalDate.fromDateFields(this.startDate);
        }
        return startLocalDate;
    }

    /**
     * Sets the valid from date of this cashier.
     * 
     * <p>
     * The valid from/to dates may be used to define a time period in which the
     * cashier is assignable to a teller.
     * </p>
     * 
     * @param  startDate validFrom
     *            the valid from date of this cashier
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the valid to date of this cashier.
     * 
     * <p>
     * The valid from/to dates may be used to define a time period in which the
     * cashier is assignable to a teller.
     * </p>
     * 
     * @return the valid to date of this cashier
     */
    public Date getEndDate() {
        return endDate;
    }

    public LocalDate getEndLocalDate() {
        LocalDate endLocalDate = null;
        if (this.endDate != null) {
            endLocalDate = LocalDate.fromDateFields(this.endDate);
        }
        return endLocalDate;
    }

    /**
     * Sets the valid to date of this cashier.
     * 
     * <p>
     * The valid from/to dates may be used to define a time period in which the
     * cashier is assignable to a teller.
     * </p>
     * 
     * @param endDate validTo
     *            the valid to date of this cashier
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns whether this cashier works part time or not.
     * 
     * @return {@code true} if this cashier works part time; {@code false}
     *         otherwise
     */
    public Boolean isFullDay() {
        return isFullDay;
    }

    /**
     * Sets the part time flag of this cashier.
     * 
     * @param isFullDay partTime
     *            the part time flag of this cashier
     */
    public void setFullDay(Boolean isFullDay) {
        this.isFullDay = isFullDay;
    }

    /**
     * Returns the start time of this cashier.
     * 
     * <p>
     * The start/end times may be used to define a time period in which the
     * cashier works part time.
     * </p>
     * 
     * @return the start time of this cashier
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Set the start time of this cashier.
     * 
     * <p>
     * The start/end times may be used to define a time period in which the
     * cashier works part time.
     * </p>
     * 
     * @param startTime
     *            the start time of this cashier
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the end time of this cashier.
     * 
     * <p>
     * The start/end times may be used to define a time period in which the
     * cashier works part time.
     * </p>
     * 
     * @return the end time of this cashier
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of this cashier.
     * 
     * <p>
     * The start/end times may be used to define a time period in which the
     * cashier works part time.
     * </p>
     * 
     * @param endTime
     *            the end time of this cashier
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
