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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_tax_component")
public class TaxComponent extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal percentage;

    @Column(name = "debit_account_type_enum")
    private Integer debitAccountType;

    @ManyToOne
    @JoinColumn(name = "debit_account_id")
    private GLAccount debitAcount;

    @Column(name = "credit_account_type_enum")
    private Integer creditAccountType;

    @ManyToOne
    @JoinColumn(name = "credit_account_id")
    private GLAccount creditAcount;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    @JoinColumn(name = "tax_component_id", referencedColumnName = "id", nullable = false)
    private Set<TaxComponentHistory> taxComponentHistories = new HashSet<>();

    @OneToMany(cascade = CascadeType.DETACH, mappedBy = "taxComponent", orphanRemoval = false, fetch=FetchType.EAGER)
    private Set<TaxGroupMappings> taxGroupMappings = new HashSet<>();

    protected TaxComponent() {

    }

    private TaxComponent(final String name, final BigDecimal percentage, final GLAccountType debitAccountType, final GLAccount debitAcount,
            final GLAccountType creditAccountType, final GLAccount creditAcount, final LocalDate startDate) {
        this.name = name;
        this.percentage = percentage;
        if (debitAccountType != null) {
            this.debitAccountType = debitAccountType.getValue();
        }
        this.debitAcount = debitAcount;
        if (creditAccountType != null) {
            this.creditAccountType = creditAccountType.getValue();
        }
        this.creditAcount = creditAcount;
        this.startDate = startDate.toDate();
    }

    public static TaxComponent createTaxComponent(final String name, final BigDecimal percentage, final GLAccountType debitAccountType,
            final GLAccount debitAcount, final GLAccountType creditAccountType, final GLAccount creditAcount, final LocalDate startDate) {
        return new TaxComponent(name, percentage, debitAccountType, debitAcount, creditAccountType, creditAcount, startDate);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();

        if (command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(TaxApiConstants.nameParamName);
            changes.put(TaxApiConstants.nameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, this.percentage)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(TaxApiConstants.percentageParamName);
            changes.put(TaxApiConstants.percentageParamName, newValue);

            LocalDate oldStartDate = new LocalDate(this.startDate);
            updateStartDate(command, changes, true);
            LocalDate newStartDate = new LocalDate(this.startDate);

            TaxComponentHistory history = TaxComponentHistory.createTaxComponentHistory(this.percentage, oldStartDate, newStartDate);
            this.taxComponentHistories.add(history);
            this.percentage = newValue;

        }

        return changes;
    }

    private void updateStartDate(final JsonCommand command, final Map<String, Object> changes, boolean setAsCurrentDate) {
        LocalDate startDate = DateUtils.getLocalDateOfTenant();
        if (command.parameterExists(TaxApiConstants.startDateParamName)) {
            LocalDate startDateFromUI = command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName);
            if (startDateFromUI != null) {
                startDate = startDateFromUI;
            }
            this.startDate = startDate.toDate();
            changes.put(TaxApiConstants.startDateParamName, startDate);
        } else if (setAsCurrentDate) {
            changes.put(TaxApiConstants.startDateParamName, startDate);
            this.startDate = startDate.toDate();
        }

    }

    public BigDecimal getPercentage() {
        return this.percentage;
    }

    public LocalDate startDate() {
        LocalDate startDate = null;
        if (this.startDate != null) {
            startDate = new LocalDate(this.startDate);
        }
        return startDate;
    }

    public BigDecimal getApplicablePercentage(final LocalDate date) {
        BigDecimal percentage = null;
        if (occursOnDayFrom(date)) {
            percentage = getPercentage();
        } else {
            for (TaxComponentHistory componentHistory : taxComponentHistories) {
                if (componentHistory.occursOnDayFromAndUpToAndIncluding(date)) {
                    percentage = componentHistory.getPercentage();
                    break;
                }
            }
        }
        return percentage;
    }

    private boolean occursOnDayFrom(final LocalDate target) {
        return target != null && target.isAfter(startDate());
    }

    public Set<TaxComponentHistory> getTaxComponentHistories() {
        return this.taxComponentHistories;
    }

    public Set<TaxGroupMappings> getTaxGroupMappings() {
        return this.taxGroupMappings;
    }

    public Collection<LocalDate> allStartDates() {
        List<LocalDate> dates = new ArrayList<>();
        dates.add(startDate());
        for (TaxComponentHistory componentHistory : taxComponentHistories) {
            dates.add(componentHistory.startDate());
        }

        return dates;
    }

    
    public Integer getDebitAccountType() {
        return this.debitAccountType;
    }

    
    public GLAccount getDebitAcount() {
        return this.debitAcount;
    }

    
    public Integer getCreditAccountType() {
        return this.creditAccountType;
    }

    
    public GLAccount getCreditAcount() {
        return this.creditAcount;
    }
}
