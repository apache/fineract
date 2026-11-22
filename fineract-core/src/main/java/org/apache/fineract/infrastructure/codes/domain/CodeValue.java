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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_code_value", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "code_id", "code_value" }, name = "code_value_duplicate") })
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
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
        return new CodeValue().setCode(code).setLabel(StringUtils.defaultIfEmpty(label, null)).setPosition(position)
                .setDescription(description).setActive(isActive).setMandatory(mandatory);
    }

    public void updateLabel(final String label) {
        this.label = StringUtils.defaultIfEmpty(label, null);
    }

    public void updateDescription(final String description) {
        this.description = StringUtils.defaultIfEmpty(description, null);
    }

    public void updatePosition(final int position) {
        this.position = position;
    }

    public void updateActive(final boolean active) {
        this.isActive = active;
    }

    public CodeValueData toData() {
        return CodeValueData.instance(getId(), this.label, this.description, this.position, this.isActive, this.mandatory);
    }
}
