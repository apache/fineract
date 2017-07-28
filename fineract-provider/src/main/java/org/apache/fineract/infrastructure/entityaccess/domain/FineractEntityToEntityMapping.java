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
package org.apache.fineract.infrastructure.entityaccess.domain;

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

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.entityaccess.api.FineractEntityApiResourceConstants;
import org.apache.fineract.infrastructure.entityaccess.exception.FineractEntityToEntityMappingDateException;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_entity_to_entity_mapping", uniqueConstraints = { @UniqueConstraint(columnNames = { "rel_id", "from_id", "to_id" }) })
public class FineractEntityToEntityMapping extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "rel_id")
    private FineractEntityRelation relationId;

    @Column(name = "from_id")
    private Long fromId;

    @Column(name = "to_id")
    private Long toId;

    @Column(name = "start_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    private FineractEntityToEntityMapping(final FineractEntityRelation relationId, final Long fromId, final Long toId, final Date startDate,
            final Date endDate) {
        this.relationId = relationId;
        this.fromId = fromId;
        this.toId = toId;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    public FineractEntityToEntityMapping() {
        //
    }

    public static FineractEntityToEntityMapping newMap(FineractEntityRelation relationId, Long fromId, Long toId, Date startDate, Date endDate) {

        return new FineractEntityToEntityMapping(relationId, fromId, toId, startDate, endDate);

    }

    public Map<String, Object> updateMap(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInLongParameterNamed(FineractEntityApiResourceConstants.fromEnityType, this.fromId)) {
            final Long newValue = command.longValueOfParameterNamed(FineractEntityApiResourceConstants.fromEnityType);
            actualChanges.put(FineractEntityApiResourceConstants.fromEnityType, newValue);
            this.fromId = newValue;
        }

        if (command.isChangeInLongParameterNamed(FineractEntityApiResourceConstants.toEntityType, this.toId)) {
            final Long newValue = command.longValueOfParameterNamed(FineractEntityApiResourceConstants.toEntityType);
            actualChanges.put(FineractEntityApiResourceConstants.toEntityType, newValue);
            this.toId = newValue;
        }

        if (command.isChangeInDateParameterNamed(FineractEntityApiResourceConstants.startDate, this.startDate)) {
            final String valueAsInput = command.stringValueOfParameterNamed(FineractEntityApiResourceConstants.startDate);
            actualChanges.put(FineractEntityApiResourceConstants.startDate, valueAsInput);
            final Date startDate = command.DateValueOfParameterNamed(FineractEntityApiResourceConstants.startDate);
            this.startDate = startDate;
        }

        if (command.isChangeInDateParameterNamed(FineractEntityApiResourceConstants.endDate, this.endDate)) {
            final String valueAsInput = command.stringValueOfParameterNamed(FineractEntityApiResourceConstants.endDate);
            actualChanges.put(FineractEntityApiResourceConstants.endDate, valueAsInput);
            final Date endDate = command.DateValueOfParameterNamed(FineractEntityApiResourceConstants.endDate);
            this.endDate = endDate;
        }
        if (startDate != null && endDate != null) {
            if (endDate.before(startDate)) { throw new FineractEntityToEntityMappingDateException(startDate.toString(), endDate.toString()); }
        }

        return actualChanges;

    }

    public FineractEntityRelation getRelationId() {
        return this.relationId;
    }

    public void setRelationId(FineractEntityRelation relationId) {
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
