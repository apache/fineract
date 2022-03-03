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

import java.time.LocalDate;
import java.time.ZoneId;
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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;

@Entity
@Table(name = "m_tax_group_mappings")
public class TaxGroupMappings extends AbstractAuditableCustom {

    @ManyToOne
    @JoinColumn(name = "tax_group_id", nullable = false)
    private TaxGroup taxGroup;

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
            this.startDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (endDate != null) {
            this.endDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
        if (this.endDate == null) {
            return target != null && target.isAfter(startDate());
        }
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
            startDate = LocalDate.ofInstant(this.startDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return startDate;
    }

    public LocalDate endDate() {
        LocalDate endDate = null;
        if (this.endDate != null) {
            endDate = LocalDate.ofInstant(this.endDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return endDate;
    }

    public void setTaxGroup(TaxGroup taxGroup) {
        this.taxGroup = taxGroup;
    }

    public TaxGroup getTaxGroup() {
        return taxGroup;
    }
}
