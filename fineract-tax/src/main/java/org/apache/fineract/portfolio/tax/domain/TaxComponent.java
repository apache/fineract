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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Entity
@Getter
@Setter
@Table(name = "m_tax_component")
@FieldNameConstants
public class TaxComponent extends AbstractAuditableCustom {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal percentage;

    @Column(name = "debit_account_type_enum")
    private Integer debitAccountType;

    @ManyToOne
    @JoinColumn(name = "debit_account_id")
    private GLAccount debitAccount;

    @Column(name = "credit_account_type_enum")
    private Integer creditAccountType;

    @ManyToOne
    @JoinColumn(name = "credit_account_id")
    private GLAccount creditAccount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "tax_component_id", referencedColumnName = "id", nullable = false)
    private Set<TaxComponentHistory> taxComponentHistories = new HashSet<>();

    @OneToMany(cascade = CascadeType.DETACH, mappedBy = "taxComponent", orphanRemoval = false, fetch = FetchType.EAGER)
    private Set<TaxGroupMappings> taxGroupMappings = new HashSet<>();

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();

        if (command.isChangeInStringParameterNamed(Fields.name, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(Fields.name);
            changes.put(Fields.name, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInBigDecimalParameterNamed(Fields.percentage, this.percentage)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(Fields.percentage);
            changes.put(Fields.percentage, newValue);

            LocalDate oldStartDate = this.startDate;
            updateStartDate(command, changes, true);
            LocalDate newStartDate = this.startDate;

            TaxComponentHistory history = new TaxComponentHistory();
            history.setPercentage(this.percentage);
            history.setStartDate(oldStartDate);
            history.setStartDate(newStartDate);
            this.taxComponentHistories.add(history);
            this.percentage = newValue;

        }

        return changes;
    }

    private void updateStartDate(final JsonCommand command, final Map<String, Object> changes, boolean setAsCurrentDate) {
        LocalDate startDate = DateUtils.getBusinessLocalDate();
        if (command.parameterExists(Fields.startDate)) {
            LocalDate startDateFromUI = command.localDateValueOfParameterNamed(Fields.startDate);
            if (startDateFromUI != null) {
                startDate = startDateFromUI;
            }
            this.startDate = startDate;
            changes.put(Fields.startDate, startDate);
        } else if (setAsCurrentDate) {
            changes.put(Fields.startDate, startDate);
            this.startDate = startDate;
        }

    }

    public BigDecimal getApplicablePercentage(final LocalDate date) {
        if (DateUtils.isAfter(date, startDate)) {
            return getPercentage();
        } else {
            for (var componentHistory : taxComponentHistories) {
                if (occursOnDayFromAndUpToAndIncluding(componentHistory.getStartDate(), componentHistory.getEndDate(), date)) {
                    return componentHistory.getPercentage();
                }
            }
        }

        return null;
    }

    boolean occursOnDayFromAndUpToAndIncluding(final LocalDate start, final LocalDate end, final LocalDate target) {
        return DateUtils.isAfter(target, start) && (end == null || !DateUtils.isAfter(target, end));
    }
}
