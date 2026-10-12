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
package org.apache.fineract.portfolio.tax.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Entity
@Table(name = "m_tax_component_history")
public class TaxComponentHistory extends AbstractAuditableCustom {

    @Column(name = "percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal percentage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    protected TaxComponentHistory() {

    }

    private TaxComponentHistory(final BigDecimal percentage, final LocalDate startDate, final LocalDate endDate) {
        this.percentage = percentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static TaxComponentHistory createTaxComponentHistory(final BigDecimal percentage, final LocalDate startDate,
            final LocalDate endDate) {
        return new TaxComponentHistory(percentage, startDate, endDate);
    }

    public LocalDate startDate() {
        return this.startDate;
    }

    public LocalDate endDate() {
        return this.endDate;
    }

    public boolean occursOnDayFromAndUpToAndIncluding(final LocalDate target) {
        return DateUtils.isAfter(target, startDate()) && (endDate == null || !DateUtils.isAfter(target, endDate()));
    }

    public BigDecimal getPercentage() {
        return this.percentage;
    }

}
