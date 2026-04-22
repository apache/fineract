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
package org.apache.fineract.portfolio.delinquency.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "m_delinquency_range", uniqueConstraints = {
        @UniqueConstraint(name = "uq_delinquency_range_classification", columnNames = { "classification" }) })
public class DelinquencyRange extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "classification", nullable = false)
    private String classification;

    @Column(name = "min_age_days", nullable = false)
    private Integer minimumAgeDays;

    @Column(name = "max_age_days", nullable = true)
    private Integer maximumAgeDays;

    @Version
    private Long version;

    protected DelinquencyRange(@NotNull String classification, @NotNull Integer minimumAgeDays, Integer maximumAgeDays) {
        this.classification = classification;
        this.minimumAgeDays = minimumAgeDays;
        this.maximumAgeDays = maximumAgeDays;
    }

    public static DelinquencyRange instance(@NotNull String classification, @NotNull Integer minimumAge, Integer maximumAge) {
        return new DelinquencyRange(classification, minimumAge, maximumAge);
    }

}
