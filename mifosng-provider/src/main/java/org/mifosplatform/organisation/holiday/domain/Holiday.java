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

    @SuppressWarnings("unused")
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @SuppressWarnings("unused")
    @Column(name = "from_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @SuppressWarnings("unused")
    @Column(name = "to_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @SuppressWarnings("unused")
    @Column(name = "description", length = 100)
    private String description;

    @SuppressWarnings("unused")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_holiday_office", joinColumns = @JoinColumn(name = "holiday_id"), inverseJoinColumns = @JoinColumn(name = "office_id"))
    private Set<Office> offices;

    public static Holiday createNew(final Set<Office> offices, final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(HolidayApiConstants.name);
        final LocalDate fromDate = command.localDateValueOfParameterNamed(HolidayApiConstants.fromDate);
        final LocalDate toDate = command.localDateValueOfParameterNamed(HolidayApiConstants.toDate);
        final String description = command.stringValueOfParameterNamed(HolidayApiConstants.description);
        return new Holiday(name, fromDate, toDate, description, offices);
    }

    private Holiday(final String name, final LocalDate fromDate, final LocalDate toDate, final String description, final Set<Office> offices) {
        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        }

        if (fromDate != null) {
            this.fromDate = fromDate.toDate();
        }

        if (toDate != null) {
            this.toDate = toDate.toDate();
        }

        if (StringUtils.isNotBlank(name)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }

        if (offices != null) {
            this.offices = offices;
        }
    }

}
