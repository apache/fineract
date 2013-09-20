/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.domain;

import java.util.Date;
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
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.holiday.api.HolidayApiConstants;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_holiday", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "holiday_name") })
public class Holiday extends AbstractPersistable<Long> {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "from_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "to_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column(name = "repayments_rescheduled_to", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date repaymentsRescheduledTo;

    @Column(name = "processed", nullable = false)
    private boolean processed;

    @Column(name = "description", length = 100)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_holiday_office", joinColumns = @JoinColumn(name = "holiday_id"), inverseJoinColumns = @JoinColumn(name = "office_id"))
    private Set<Office> offices;

    public static Holiday createNew(final Set<Office> offices, final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(HolidayApiConstants.name);
        final LocalDate fromDate = command.localDateValueOfParameterNamed(HolidayApiConstants.fromDate);
        final LocalDate toDate = command.localDateValueOfParameterNamed(HolidayApiConstants.toDate);
        final LocalDate repaymentsRescheduledTo = command.localDateValueOfParameterNamed(HolidayApiConstants.repaymentsRescheduledTo);
        final boolean processed = false;// default it to false. Only batch job
                                        // should update this field.
        final String description = command.stringValueOfParameterNamed(HolidayApiConstants.description);
        return new Holiday(name, fromDate, toDate, repaymentsRescheduledTo, processed, description, offices);
    }

    private Holiday(final String name, final LocalDate fromDate, final LocalDate toDate, final LocalDate repaymentsRescheduledTo,
            final boolean processed, final String description, final Set<Office> offices) {
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

        this.processed = processed;

        if (StringUtils.isNotBlank(name)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }

        if (offices != null) {
            this.offices = offices;
        }
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
}
