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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_tax_group_mappings")
public class TaxGroupMappings extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "tax_component_id", nullable = false)
    private TaxComponent taxComponent;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    protected TaxGroupMappings() {}

    private TaxGroupMappings(final TaxComponent taxComponent, final LocalDate startDate, final LocalDate endDate) {

        this.taxComponent = taxComponent;
        if (startDate != null) {
            this.startDate = startDate.toDate();
        }
        if (endDate != null) {
            this.endDate = endDate.toDate();
        }
    }

    public static TaxGroupMappings createTaxGroupMappings(final TaxComponent taxComponent, final LocalDate startDate) {
        final LocalDate endDate = null;
        return new TaxGroupMappings(taxComponent, startDate, endDate);

    }

    public static TaxGroupMappings createTaxGroupMappings(final Long id, final TaxComponent taxComponent, final LocalDate endDate) {
        final LocalDate startDate = null;
        TaxGroupMappings groupMappings = new TaxGroupMappings(taxComponent, startDate, endDate);
        groupMappings.setId(id);
        return groupMappings;

    }

    public void update(final Date endDate, final List<Map<String, Object>> changes) {
        if (endDate != null && this.endDate == null) {
            this.endDate = endDate;
            Map<String, Object> map = new HashMap<>(2);
            map.put(TaxApiConstants.endDateParamName, endDate);
            map.put(TaxApiConstants.taxComponentIdParamName, this.getTaxComponent().getId());
            changes.add(map);
        }
    }

    public boolean occursOnDayFromAndUpToAndIncluding(final LocalDate target) {
        if (this.endDate == null) { return target != null && target.isAfter(startDate()); }
        return target != null && target.isAfter(startDate()) && !target.isAfter(endDate());
    }

    public TaxComponent getTaxComponent() {
        return this.taxComponent;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public LocalDate startDate() {
        LocalDate startDate = null;
        if (this.startDate != null) {
            startDate = new LocalDate(this.startDate);
        }
        return startDate;
    }

    public LocalDate endDate() {
        LocalDate endDate = null;
        if (this.endDate != null) {
            endDate = new LocalDate(this.endDate);
        }
        return endDate;
    }
}
