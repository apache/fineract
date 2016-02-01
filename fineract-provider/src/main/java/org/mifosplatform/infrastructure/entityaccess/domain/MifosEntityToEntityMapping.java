/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.entityaccess.api.MifosEntityApiResourceConstants;
import org.mifosplatform.infrastructure.entityaccess.exception.MifosEntityToEntityMappingDateException;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_entity_to_entity_mapping", uniqueConstraints = { @UniqueConstraint(columnNames = { "rel_id", "from_id", "to_id" }) })
public class MifosEntityToEntityMapping extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "rel_id")
    private MifosEntityRelation relationId;

    @Column(name = "from_id")
    private Long fromId;

    @Column(name = "to_id")
    private Long toId;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    private MifosEntityToEntityMapping(final MifosEntityRelation relationId, final Long fromId, final Long toId, final Date startDate,
            final Date endDate) {
        this.relationId = relationId;
        this.fromId = fromId;
        this.toId = toId;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    public MifosEntityToEntityMapping() {
        //
    }

    public static MifosEntityToEntityMapping newMap(MifosEntityRelation relationId, Long fromId, Long toId, Date startDate, Date endDate) {

        return new MifosEntityToEntityMapping(relationId, fromId, toId, startDate, endDate);

    }

    public Map<String, Object> updateMap(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInLongParameterNamed(MifosEntityApiResourceConstants.fromEnityType, this.fromId)) {
            final Long newValue = command.longValueOfParameterNamed(MifosEntityApiResourceConstants.fromEnityType);
            actualChanges.put(MifosEntityApiResourceConstants.fromEnityType, newValue);
            this.fromId = newValue;
        }

        if (command.isChangeInLongParameterNamed(MifosEntityApiResourceConstants.toEntityType, this.toId)) {
            final Long newValue = command.longValueOfParameterNamed(MifosEntityApiResourceConstants.toEntityType);
            actualChanges.put(MifosEntityApiResourceConstants.toEntityType, newValue);
            this.toId = newValue;
        }

        if (command.isChangeInDateParameterNamed(MifosEntityApiResourceConstants.startDate, this.startDate)) {
            final String valueAsInput = command.stringValueOfParameterNamed(MifosEntityApiResourceConstants.startDate);
            actualChanges.put(MifosEntityApiResourceConstants.startDate, valueAsInput);
            final Date startDate = command.DateValueOfParameterNamed(MifosEntityApiResourceConstants.startDate);
            this.startDate = startDate;
        }

        if (command.isChangeInDateParameterNamed(MifosEntityApiResourceConstants.endDate, this.endDate)) {
            final String valueAsInput = command.stringValueOfParameterNamed(MifosEntityApiResourceConstants.endDate);
            actualChanges.put(MifosEntityApiResourceConstants.endDate, valueAsInput);
            final Date endDate = command.DateValueOfParameterNamed(MifosEntityApiResourceConstants.endDate);
            this.endDate = endDate;
        }
        if (startDate != null && endDate != null) {
            if (endDate.before(startDate)) { throw new MifosEntityToEntityMappingDateException(startDate.toString(), endDate.toString()); }
        }

        return actualChanges;

    }

    public MifosEntityRelation getRelationId() {
        return this.relationId;
    }

    public void setRelationId(MifosEntityRelation relationId) {
        this.relationId = relationId;
    }

    /*
     * public Date getStartDate() { Date startDate = null; if (this.startDate !=
     * null) { startDate = Date.fromDateFields(this.startDate); } return
     * startDate; }
     */

    /*
     * public Date getStartDate() { return (Date) ObjectUtils.defaultIfNull(new
     * Date(this.startDate), null); }
     */

}
