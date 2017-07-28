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

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_tax_component_history")
public class TaxComponentHistory extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal percentage;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    protected TaxComponentHistory() {

    }

    private TaxComponentHistory(final BigDecimal percentage, final LocalDate startDate, final LocalDate endDate) {
        this.percentage = percentage;
        this.startDate = startDate.toDate();
        this.endDate = endDate.toDate();
    }

    public static TaxComponentHistory createTaxComponentHistory(final BigDecimal percentage, final LocalDate startDate,
            final LocalDate endDate) {
        return new TaxComponentHistory(percentage, startDate, endDate);
    }
    
    public LocalDate startDate(){
        LocalDate startDate = null;
        if(this.startDate != null){
            startDate = new LocalDate(this.startDate);
        }
        return startDate;
    }
    
    public LocalDate endDate(){
        LocalDate endDate = null;
        if(this.endDate != null){
            endDate = new LocalDate(this.endDate);
        }
        return endDate;
    }
    
    public boolean occursOnDayFromAndUpToAndIncluding(final LocalDate target) {
        if(this.endDate == null){
            return target != null && target.isAfter(startDate());
        }
        return target != null && target.isAfter(startDate()) && !target.isAfter(endDate());
    }

    
    public BigDecimal getPercentage() {
        return this.percentage;
    }

}
