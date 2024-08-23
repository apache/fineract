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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.entityaccess.FineractEntityAccessConstants;

@Entity
@Table(name = "m_entity_to_entity_access")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class FineractEntityAccess extends AbstractPersistableCustom<Long> {

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @ManyToOne
    @JoinColumn(name = "access_type_code_value_id", nullable = false)
    private CodeValue accessType;

    @Column(name = "second_entity_type", length = 50)
    private String secondEntityType;

    @Column(name = "second_entity_id")
    private Long secondEntityId;

    public static FineractEntityAccess createNew(final String entityType, final Long entityId, final CodeValue accessType,
            final String secondEntityType, final Long secondEntityId) {
        return new FineractEntityAccess().setEntityType(entityType).setEntityId(entityId).setAccessType(accessType)
                .setSecondEntityType(secondEntityType).setSecondEntityId(secondEntityId);
    }

    public static FineractEntityAccess fromJson(final CodeValue accessType, final JsonCommand command) {
        final String entityType = command
                .stringValueOfParameterNamed(FineractEntityAccessConstants.EntityAccessJSONinputParams.ENTITY_TYPE.getValue());
        final Long entityId = command
                .longValueOfParameterNamed(FineractEntityAccessConstants.EntityAccessJSONinputParams.ENTITY_ID.getValue());
        final String secondEntityType = command
                .stringValueOfParameterNamed(FineractEntityAccessConstants.EntityAccessJSONinputParams.SECOND_ENTITY_ID.getValue());
        final Long secondEntityId = command
                .longValueOfParameterNamed(FineractEntityAccessConstants.EntityAccessJSONinputParams.SECOND_ENTITY_ID.getValue());

        return new FineractEntityAccess().setEntityType(entityType).setEntityId(entityId).setAccessType(accessType)
                .setSecondEntityType(secondEntityType).setSecondEntityId(secondEntityId);

    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        String paramName = null;

        paramName = FineractEntityAccessConstants.EntityAccessJSONinputParams.ENTITY_TYPE.getValue();
        if (command.isChangeInStringParameterNamed(paramName, this.entityType)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.entityType = newValue;
        }

        paramName = FineractEntityAccessConstants.EntityAccessJSONinputParams.ENTITY_ID.getValue();
        if (command.isChangeInLongParameterNamed(paramName, getEntityId())) {
            this.entityId = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, this.entityId);
        }

        Long existingAccessTypeId = null;
        if (this.accessType != null) {
            existingAccessTypeId = this.accessType.getId();
        }

        paramName = FineractEntityAccessConstants.EntityAccessJSONinputParams.ENTITY_ACCESS_TYPE_ID.getValue();
        if (command.isChangeInLongParameterNamed(paramName, existingAccessTypeId)) {
            final Long newValue = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
        }

        paramName = FineractEntityAccessConstants.EntityAccessJSONinputParams.SECOND_ENTITY_TYPE.getValue();
        if (command.isChangeInStringParameterNamed(paramName, this.secondEntityType)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.secondEntityType = newValue;
        }

        paramName = FineractEntityAccessConstants.EntityAccessJSONinputParams.SECOND_ENTITY_ID.getValue();
        if (command.isChangeInLongParameterNamed(paramName, getSecondEntityId())) {
            this.secondEntityId = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, this.secondEntityId);
        }

        return actualChanges;
    }

}
