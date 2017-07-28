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
package org.apache.fineract.portfolio.calendar.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_calendar_instance")
public class CalendarInstance extends AbstractPersistableCustom<Long> {

    @ManyToOne(cascade = CascadeType.PERSIST)
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
