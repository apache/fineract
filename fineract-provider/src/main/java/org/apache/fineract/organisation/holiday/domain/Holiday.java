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
package org.apache.fineract.organisation.holiday.domain;

import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.descriptionParamName;
import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.fromDateParamName;
import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.nameParamName;
import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.officesParamName;
import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.repaymentsRescheduledToParamName;
import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.toDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dateFormatParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.localeParamName;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.holiday.api.HolidayApiConstants;
import org.apache.fineract.organisation.office.domain.Office;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import com.google.gson.JsonArray;

@Entity
@Table(name = "m_holiday", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "holiday_name") })
public class Holiday extends AbstractPersistableCustom<Long> {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "from_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "to_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column(name = "repayments_rescheduled_to", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date repaymentsRescheduledTo;
    
    @Column(name = "rescheduling_type", nullable = false)
    private int reschedulingType;

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "processed", nullable = false)
    private boolean processed;

    @Column(name = "description", length = 100)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_holiday_office", joinColumns = @JoinColumn(name = "holiday_id"), inverseJoinColumns = @JoinColumn(name = "office_id"))
    private Set<Office> offices;

    public static Holiday createNew(final Set<Office> offices, final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(HolidayApiConstants.nameParamName);
        final LocalDate fromDate = command.localDateValueOfParameterNamed(HolidayApiConstants.fromDateParamName);
        final LocalDate toDate = command.localDateValueOfParameterNamed(HolidayApiConstants.toDateParamName);
        Integer reschedulingType = null;
        if(command.parameterExists(HolidayApiConstants.reschedulingType)){
            reschedulingType = command.integerValueOfParameterNamed(HolidayApiConstants.reschedulingType);
        }
        LocalDate repaymentsRescheduledTo = null;
        if(reschedulingType == null || reschedulingType.equals(RescheduleType.RESCHEDULETOSPECIFICDATE.getValue())){
            repaymentsRescheduledTo = command
                    .localDateValueOfParameterNamed(HolidayApiConstants.repaymentsRescheduledToParamName);
        }
        final Integer status = HolidayStatusType.PENDING_FOR_ACTIVATION.getValue();
        final boolean processed = false;// default it to false. Only batch job
                                        // should update this field.
        final String description = command.stringValueOfParameterNamed(HolidayApiConstants.descriptionParamName);
        
        return new Holiday(name, fromDate, toDate, repaymentsRescheduledTo, status, processed, description, offices, reschedulingType);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("holiday" + ".update");

        final HolidayStatusType currentStatus = HolidayStatusType.fromInt(this.status);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }
        
        
        if (command.isChangeInIntegerParameterNamed(HolidayApiConstants.reschedulingType,this.reschedulingType)) {
            final Integer newValue =command.integerValueOfParameterNamed(HolidayApiConstants.reschedulingType);
            actualChanges.put(HolidayApiConstants.reschedulingType, newValue);
            this.reschedulingType = RescheduleType.fromInt(newValue).getValue();
            if(newValue.equals(RescheduleType.RESCHEDULETONEXTREPAYMENTDATE.getValue())){
                this.repaymentsRescheduledTo = null;
            }
        } 

        if (currentStatus.isPendingActivation()) {
            if (command.isChangeInLocalDateParameterNamed(fromDateParamName, getFromDateLocalDate())) {
                final String valueAsInput = command.stringValueOfParameterNamed(fromDateParamName);
                actualChanges.put(fromDateParamName, valueAsInput);
                actualChanges.put(dateFormatParamName, dateFormatAsInput);
                actualChanges.put(localeParamName, localeAsInput);
                final LocalDate newValue = command.localDateValueOfParameterNamed(fromDateParamName);
                this.fromDate = newValue.toDate();
            }

            if (command.isChangeInLocalDateParameterNamed(toDateParamName, getToDateLocalDate())) {
                final String valueAsInput = command.stringValueOfParameterNamed(toDateParamName);
                actualChanges.put(toDateParamName, valueAsInput);
                actualChanges.put(dateFormatParamName, dateFormatAsInput);
                actualChanges.put(localeParamName, localeAsInput);

                final LocalDate newValue = command.localDateValueOfParameterNamed(toDateParamName);
                this.toDate = newValue.toDate();
            }

            if (command.isChangeInLocalDateParameterNamed(repaymentsRescheduledToParamName, getRepaymentsRescheduledToLocalDate())) {
                final String valueAsInput = command.stringValueOfParameterNamed(repaymentsRescheduledToParamName);
                actualChanges.put(repaymentsRescheduledToParamName, valueAsInput);
                actualChanges.put(dateFormatParamName, dateFormatAsInput);
                actualChanges.put(localeParamName, localeAsInput);

                final LocalDate newValue = command.localDateValueOfParameterNamed(repaymentsRescheduledToParamName);
                this.repaymentsRescheduledTo = newValue.toDate();
            }

            if (command.hasParameter(officesParamName)) {
                final JsonArray jsonArray = command.arrayOfParameterNamed(officesParamName);
                if (jsonArray != null) {
                    actualChanges.put(officesParamName, command.jsonFragment(officesParamName));
                }
            }
        } else {
            if (command.isChangeInLocalDateParameterNamed(fromDateParamName, getFromDateLocalDate())) {
                baseDataValidator.reset().parameter(fromDateParamName).failWithCode("cannot.edit.holiday.in.active.state");
            }

            if (command.isChangeInLocalDateParameterNamed(toDateParamName, getToDateLocalDate())) {
                baseDataValidator.reset().parameter(toDateParamName).failWithCode("cannot.edit.holiday.in.active.state");
            }

            if (command.isChangeInLocalDateParameterNamed(repaymentsRescheduledToParamName, getRepaymentsRescheduledToLocalDate())) {
                baseDataValidator.reset().parameter(repaymentsRescheduledToParamName).failWithCode("cannot.edit.holiday.in.active.state");
            }

            if (command.hasParameter(officesParamName)) {
                baseDataValidator.reset().parameter(repaymentsRescheduledToParamName).failWithCode("cannot.edit.holiday.in.active.state");
            }

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        return actualChanges;
    }

    public boolean update(final Set<Office> newOffices) {
        if (newOffices == null) { return false; }

        boolean updated = false;
        if (this.offices != null) {
            final Set<Office> currentSetOfOffices = new HashSet<>(this.offices);
            final Set<Office> newSetOfOffices = new HashSet<>(newOffices);

            if (!(currentSetOfOffices.equals(newSetOfOffices))) {
                updated = true;
                this.offices = newOffices;
            }
        } else {
            updated = true;
            this.offices = newOffices;
        }
        return updated;
    }

    private Holiday(final String name, final LocalDate fromDate, final LocalDate toDate, final LocalDate repaymentsRescheduledTo,
            final Integer status, final boolean processed, final String description, final Set<Office> offices, final int reschedulingType) {
        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        }

        if (fromDate != null) {
            this.fromDate = fromDate.toDate();
        }

        if (toDate != null) {
            this.toDate = toDate.toDate();
        }

        if (repaymentsRescheduledTo != null) {
            this.repaymentsRescheduledTo = repaymentsRescheduledTo.toDate();
        }

        this.status = status;
        this.processed = processed;

        if (StringUtils.isNotBlank(name)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }

        if (offices != null) {
            this.offices = offices;
        }
        this.reschedulingType = reschedulingType;
    }

    protected Holiday() {}

    public LocalDate getRepaymentsRescheduledToLocalDate() {
        LocalDate repaymentsRescheduledTo = null;
        if (this.repaymentsRescheduledTo != null) {
            repaymentsRescheduledTo = new LocalDate(this.repaymentsRescheduledTo);
        }
        return repaymentsRescheduledTo;
    }

    public boolean isProcessed() {
        return this.processed;
    }

    public Set<Office> getOffices() {
        return this.offices;
    }

    public LocalDate getFromDateLocalDate() {
        LocalDate fromDate = null;
        if (this.fromDate != null) {
            fromDate = new LocalDate(this.fromDate);
        }
        return fromDate;
    }

    public LocalDate getToDateLocalDate() {
        LocalDate toDate = null;
        if (this.toDate != null) {
            toDate = new LocalDate(this.toDate);
        }
        return toDate;
    }

    public void processed() {
        this.processed = true;
    }

    public void activate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("holiday" + ".activate");

        final HolidayStatusType currentStatus = HolidayStatusType.fromInt(this.status);
        if (!currentStatus.isPendingActivation()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.pending.for.activation.state");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.status = HolidayStatusType.ACTIVE.getValue();
    }

    public void delete() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("holiday" + ".delete");

        final HolidayStatusType currentStatus = HolidayStatusType.fromInt(this.status);
        if (currentStatus.isDeleted()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("already.in.deleted.state");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        this.status = HolidayStatusType.DELETED.getValue();
    }
    
    public RescheduleType getReScheduleType() {
        return RescheduleType.fromInt(this.reschedulingType);
    }
}
