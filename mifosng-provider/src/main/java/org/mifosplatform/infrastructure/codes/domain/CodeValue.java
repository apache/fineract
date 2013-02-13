/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.codes.CodeConstants.CODEVALUE_JSON_INPUT_PARAMS;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_code_value", uniqueConstraints = { @UniqueConstraint(columnNames = { "code_id", "code_value" }, name = "code_value_duplicate") })
public class CodeValue extends AbstractPersistable<Long> {

    @Column(name = "code_value", length = 100)
    private String label;

    @Column(name = "order_position")
    private int position;

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    public static CodeValue createNew(Code code, final String label, final int position) {
        return new CodeValue(code, label, position);
    }

    protected CodeValue() {
        //
    }

    private CodeValue(final Code code, final String label, final int position) {
        this.code = code;
        this.label = StringUtils.defaultIfEmpty(label, null);
        this.position = position;
    }

    public String label() {
        return label;
    }

    public int position() {
        return position;
    }
    
    public static CodeValue fromJson(Code code, final JsonCommand command) {

        final String label = command.stringValueOfParameterNamed(CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue());
        Integer position = command.integerValueSansLocaleOfParameterNamed(CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue());
        if(position == null) position = new Integer(0);
        return new CodeValue(code, label, position.intValue());
    }
    
    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(2);

        final String labelParamName = CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue();
        if (command.isChangeInStringParameterNamed(labelParamName, this.label)) {
            final String newValue = command.stringValueOfParameterNamed(labelParamName);
            actualChanges.put(labelParamName, newValue);
            this.label = StringUtils.defaultIfEmpty(newValue, null);
        }
        
        final String positionParamName = CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(positionParamName, this.position)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(positionParamName);
            actualChanges.put(positionParamName, newValue);
            this.position = newValue.intValue();
        }
        
        return actualChanges;
    }

    public CodeValueData toData() {
        return CodeValueData.instance(this.getId(), this.label, this.position);
    }
}