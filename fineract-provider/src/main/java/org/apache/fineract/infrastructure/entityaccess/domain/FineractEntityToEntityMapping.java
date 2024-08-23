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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.entityaccess.api.FineractEntityApiResourceConstants;
import org.apache.fineract.infrastructure.entityaccess.exception.FineractEntityToEntityMappingDateException;

@Entity
@Table(name = "m_entity_to_entity_mapping", uniqueConstraints = { @UniqueConstraint(columnNames = { "rel_id", "from_id", "to_id" }) })
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class FineractEntityToEntityMapping extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "rel_id")
    private FineractEntityRelation relationId;

    @Column(name = "from_id")
    private Long fromId;

    @Column(name = "to_id")
    private Long toId;

    @Column(name = "start_date", nullable = true)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    public static FineractEntityToEntityMapping newMap(FineractEntityRelation relationId, Long fromId, Long toId, LocalDate startDate,
            LocalDate endDate) {

        return new FineractEntityToEntityMapping().setRelationId(relationId).setFromId(fromId).setToId(toId).setStartDate(startDate)
                .setEndDate(endDate);

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
            this.startDate = command.localDateValueOfParameterNamed(FineractEntityApiResourceConstants.startDate);
        }

        if (command.isChangeInDateParameterNamed(FineractEntityApiResourceConstants.endDate, this.endDate)) {
            final String valueAsInput = command.stringValueOfParameterNamed(FineractEntityApiResourceConstants.endDate);
            actualChanges.put(FineractEntityApiResourceConstants.endDate, valueAsInput);
            this.endDate = command.localDateValueOfParameterNamed(FineractEntityApiResourceConstants.endDate);
        }
        if (endDate != null && DateUtils.isBefore(endDate, startDate)) {
            throw new FineractEntityToEntityMappingDateException(startDate.toString(), endDate.toString());
        }

        return actualChanges;

    }

    /*
     * public Date getStartDate() { Date startDate = null; if (this.startDate != null) { startDate =
     * Date.fromDateFields(this.startDate); } return startDate; }
     */

    /*
     * public Date getStartDate() { return (Date) ObjectUtils.defaultIfNull(new Date(this.startDate), null); }
     */

}
