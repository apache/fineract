package org.mifosplatform.portfolio.calendar.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_calendar_instance")
public class CalendarInstance extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "entity_type_enum", nullable = false)
    private Integer entityTypeId;

    public CalendarInstance(Calendar calendar, Long entityId, Integer entityTypeId) {
        this.calendar = calendar;
        this.entityId = entityId;
        this.entityTypeId = entityTypeId;
    }

    protected CalendarInstance(){

    }

    public static CalendarInstance fromJson(final Calendar calendar, final JsonCommand command) {
        final Long entityId = command.getSupportedEntityId();
        final Integer entityTypeId = CalendarEntityType.valueOf(command.getSupportedEntityType().toUpperCase()).getValue();
        return new CalendarInstance(calendar, entityId, entityTypeId);
    }
    
    public void updateCalendar(final Calendar calendar){
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
