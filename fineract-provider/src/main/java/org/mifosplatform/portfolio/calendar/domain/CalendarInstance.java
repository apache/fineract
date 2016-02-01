/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_calendar_instance")
public class CalendarInstance extends AbstractPersistable<Long> {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_type_enum", nullable = false)
    private Integer entityTypeId;

    public CalendarInstance(final Calendar calendar, final Long entityId, final Integer entityTypeId) {
        this.calendar = calendar;
        this.entityId = entityId;
        this.entityTypeId = entityTypeId;
    }

    protected CalendarInstance() {

    }

    public static CalendarInstance from(final Calendar calendar, final Long entityId, final Integer entityTypeId) {
        return new CalendarInstance(calendar, entityId, entityTypeId);
    }

    public void updateCalendar(final Calendar calendar) {
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public Integer getEntityTypeId() {
        return this.entityTypeId;
    }

}
