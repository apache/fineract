package org.mifosplatform.infrastructure.entityaccess.domain;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.entityaccess.MifosEntityAccessConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_entity_to_entity_access")
public class MifosEntityAccess extends AbstractPersistable<Long> {
	
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

    protected MifosEntityAccess () {

    }

    public static MifosEntityAccess createNew(final String entityType, final Long entityId,
    		final CodeValue accessType,
    		final String secondEntityType, final Long secondEntityId) {
        return new MifosEntityAccess(entityType, entityId, accessType,
        		secondEntityType, secondEntityId);
    }

    public MifosEntityAccess(final String entityType, final Long entityId,
    		final CodeValue accessType,
    		final String secondEntityType, final Long secondEntityId) {
    	this.entityType = entityType;
    	this.entityId = entityId;
    	this.accessType = accessType;
    	this.secondEntityType = secondEntityType;
    	this.secondEntityId = secondEntityId;
    }

    public static MifosEntityAccess fromJson(final CodeValue accessType, final JsonCommand command) {
        final String entityType = command.stringValueOfParameterNamed(
        		MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.ENTITY_TYPE.getValue());
        final Long entityId = command.longValueOfParameterNamed(
        		MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.ENTITY_ID.getValue());
        final String secondEntityType = command.stringValueOfParameterNamed(
        		MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.SECOND_ENTITY_ID.getValue());
        final Long secondEntityId = command.longValueOfParameterNamed(
        		MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.SECOND_ENTITY_ID.getValue());
        
        return new MifosEntityAccess (entityType, entityId, accessType,
        		secondEntityType, secondEntityId);

    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        String paramName = null;

        paramName = MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.ENTITY_TYPE.getValue();
        if (command.isChangeInStringParameterNamed(paramName, this.entityType)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.entityType = newValue;
        }

        paramName = MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.ENTITY_ID.getValue(); 
        if (command.isChangeInLongParameterNamed(paramName, getEntityId())) {
        	this.entityId = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, this.entityId);
        }
        
        Long existingAccessTypeId = null;
        if (this.accessType != null) {
            existingAccessTypeId = this.accessType.getId();
        }
        
        paramName = MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.ENTITY_ACCESS_TYPE_ID.getValue(); 
        if (command.isChangeInLongParameterNamed(paramName, existingAccessTypeId)) {
        	final Long newValue = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
        }

        paramName = MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.SECOND_ENTITY_TYPE.getValue();
        if (command.isChangeInStringParameterNamed(paramName, this.secondEntityType)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.secondEntityType = newValue;
        }

        paramName = MifosEntityAccessConstants.ENTITY_ACCESS_JSON_INPUT_PARAMS.SECOND_ENTITY_ID.getValue(); 
        if (command.isChangeInLongParameterNamed(paramName, getSecondEntityId())) {
        	this.secondEntityId = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, this.secondEntityId);
        }
        
        return actualChanges;
    }

    public String getEntityType() {
        return this.entityType;
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public CodeValue getAccessType() {
        return this.accessType;
    }

    public void updateAccessType(final CodeValue accessType) {
        this.accessType = accessType;
    }
    
    public String getSecondEntityType() {
        return this.secondEntityType;
    }

    public Long getSecondEntityId() {
        return this.secondEntityId;
    }

}