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
package org.apache.fineract.infrastructure.businessdate.domain;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;

@Getter
@Entity
@Table(name = "m_business_date", uniqueConstraints = { @UniqueConstraint(name = "uq_business_date_type", columnNames = { "type" }) })
public class BusinessDate extends AbstractAuditableCustom {

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private BusinessDateType type;

    @Column(name = "date", columnDefinition = "DATE")
    private LocalDate date;

    @Version
    private Long version;

    protected BusinessDate() {
        // TODO Auto-generated constructor stub
    }

    protected BusinessDate(@NotNull BusinessDateType type, @NotNull LocalDate date) {
        this.type = type;
        this.date = date;
    }

    public static BusinessDate instance(@NotNull BusinessDateType businessDateType, @NotNull LocalDate date) {
        return new BusinessDate(businessDateType, date);
    }

    public void updateDate(LocalDate date) {
        this.date = date;
    }
}
