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
package org.apache.fineract.accounting.reconciliation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "reconciliation_rule")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReconciliationRule extends AbstractPersistableCustom<Long> {

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "gl_account_id")
    private Long glAccountId;

    @Column(name = "match_condition", length = 50, nullable = false)
    private String matchCondition;

    @Column(name = "condition_value", length = 255)
    private String conditionValue;

    @Column(name = "date_tolerance_days", nullable = false)
    private Integer dateToleranceDays = 0;

    @Column(name = "amount_tolerance", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountTolerance = BigDecimal.ZERO;

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    public static ReconciliationRule create(String name, String description, Long glAccountId, String matchCondition,
            String conditionValue, Integer dateToleranceDays, BigDecimal amountTolerance, Integer priority, Long createdBy,
            OffsetDateTime createdDate) {
        return new ReconciliationRule().setName(name).setDescription(description).setGlAccountId(glAccountId)
                .setMatchCondition(matchCondition).setConditionValue(conditionValue).setDateToleranceDays(dateToleranceDays)
                .setAmountTolerance(amountTolerance).setPriority(priority).setCreatedBy(createdBy).setCreatedDate(createdDate)
                .setActive(true);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void update(String name, String description, String matchCondition, String conditionValue, Integer dateToleranceDays,
            BigDecimal amountTolerance, Integer priority) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (matchCondition != null) {
            this.matchCondition = matchCondition;
        }
        if (conditionValue != null) {
            this.conditionValue = conditionValue;
        }
        if (dateToleranceDays != null) {
            this.dateToleranceDays = dateToleranceDays;
        }
        if (amountTolerance != null) {
            this.amountTolerance = amountTolerance;
        }
        if (priority != null) {
            this.priority = priority;
        }
    }
}
