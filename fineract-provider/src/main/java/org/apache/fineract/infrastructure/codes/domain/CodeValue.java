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
package org.apache.fineract.infrastructure.codes.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.codes.CodeConstants.CODEVALUE_JSON_INPUT_PARAMS;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_code_value", uniqueConstraints = { @UniqueConstraint(columnNames = { "code_id", "code_value" }, name = "code_value_duplicate") })
public class CodeValue extends AbstractPersistableCustom<Long> {

    @Column(name = "code_value", length = 100)
    private String label;

    @Column(name = "order_position")
    private int position;

    @Column(name = "code_description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    @Column(name = "is_active")
    private boolean isActive;
    
    @Column(name = "is_mandatory")
    private boolean mandatory;

    public static CodeValue createNew(final Code code, final String label, final int position, final String description,
            final boolean isActive, final boolean mandatory) {
        return new CodeValue(code, label, position, description, isActive, mandatory);
    }

    protected CodeValue() {
        //
    }

    private CodeValue(final Code code, final String label, final int position, final String description, 
            final boolean isActive, final boolean mandatory) {
        this.code = code;
        this.label = StringUtils.defaultIfEmpty(label, null);
        this.position = position;
        this.description = description;
        this.isActive = isActive;
        this.mandatory = mandatory;
    }

    public String label() {
        return this.label;
    }

    public int position() {
        return this.position;
    }

    public static CodeValue fromJson(final Code code, final JsonCommand command) {

        final String label = command.stringValueOfParameterNamed(CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue());
        Integer position = command.integerValueSansLocaleOfParameterNamed(CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue());
        String description = command.stringValueOfParameterNamed(CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue());
        Boolean isActiveObj = command.booleanObjectValueOfParameterNamed(CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue());
        boolean isActive = true;
        if (isActiveObj != null) {
            isActive = isActiveObj;
        }
        if (position == null) {
            position = new Integer(0);
        }
        
        Boolean mandatory = command.booleanPrimitiveValueOfParameterNamed(
                CODEVALUE_JSON_INPUT_PARAMS.IS_MANDATORY.getValue());
        
        // if the "mandatory" Boolean object is null, then set it to false by default
        if (mandatory == null) {
            mandatory = false;
        }
        
        return new CodeValue(code, label, position.intValue(), description, isActive, mandatory);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(2);

        final String labelParamName = CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue();
        if (command.isChangeInStringParameterNamed(labelParamName, this.label)) {
            final String newValue = command.stringValueOfParameterNamed(labelParamName);
            actualChanges.put(labelParamName, newValue);
            this.label = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String decriptionParamName = CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue();
        if (command.isChangeInStringParameterNamed(decriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(decriptionParamName);
            actualChanges.put(decriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String positionParamName = CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(positionParamName, this.position)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(positionParamName);
            actualChanges.put(positionParamName, newValue);
            this.position = newValue.intValue();
        }

        final String isActiveParamName = CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue();
        if (command.isChangeInBooleanParameterNamed(isActiveParamName, this.isActive)) {
            final Boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isActiveParamName);
            actualChanges.put(isActiveParamName, newValue);
            this.isActive = newValue.booleanValue();
        }

        return actualChanges;
    }

    public CodeValueData toData() {
        return CodeValueData.instance(getId(), this.label, this.position, this.isActive, this.mandatory);
    }
}