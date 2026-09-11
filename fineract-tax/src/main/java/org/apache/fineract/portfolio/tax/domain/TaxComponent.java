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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;

@Entity
@Getter
@Table(name = "m_tax_component")
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

    @OneToMany(mappedBy = "taxComponent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<TaxComponentHistory> taxComponentHistories = new HashSet<>();

    @OneToMany(cascade = CascadeType.DETACH, mappedBy = "taxComponent", orphanRemoval = false, fetch = FetchType.EAGER)
    private Set<TaxGroupMappings> taxGroupMappings = new HashSet<>();

    protected TaxComponent() {

    }

    private TaxComponent(final String name, final BigDecimal percentage, final GLAccountType debitAccountType, final GLAccount debitAccount,
            final GLAccountType creditAccountType, final GLAccount creditAccount, final LocalDate startDate) {
        this.name = name;
        this.percentage = percentage;
        if (debitAccountType != null) {
            this.debitAccountType = debitAccountType.getValue();
        }
        this.debitAccount = debitAccount;
        if (creditAccountType != null) {
            this.creditAccountType = creditAccountType.getValue();
        }
        this.creditAccount = creditAccount;
        this.startDate = startDate;
    }

    public static TaxComponent createTaxComponent(final String name, final BigDecimal percentage, final GLAccountType debitAccountType,
            final GLAccount debitAccount, final GLAccountType creditAccountType, final GLAccount creditAccount, final LocalDate startDate) {
        return new TaxComponent(name, percentage, debitAccountType, debitAccount, creditAccountType, creditAccount, startDate);
    }

    public Map<String, Object> update(final JsonCommand command) {
        return update(command, null, null, null, null);
    }

    public Map<String, Object> update(final JsonCommand command, final GLAccountType debitAccountType, final GLAccount debitAccount,
            final GLAccountType creditAccountType, final GLAccount creditAccount) {
        final Map<String, Object> changes = new HashMap<>();

        if (command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(TaxApiConstants.nameParamName);
            changes.put(TaxApiConstants.nameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        // Track whether startDate was changed independently of percentage updates
        LocalDate previousStartDate = this.startDate;

        // Handle independent startDate update if provided and changed
        if (command.parameterExists(TaxApiConstants.startDateParamName)) {
            LocalDate requestedStartDate = command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName);
            if ((requestedStartDate != null && !requestedStartDate.equals(this.startDate))
                    || (requestedStartDate == null && this.startDate != null)) {
                // Only update start date (no history here); history is handled for percentage changes below
                updateStartDate(command, changes, false);
            }
        }

        if (command.isChangeInBigDecimalParameterNamed(TaxApiConstants.percentageParamName, this.percentage)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(TaxApiConstants.percentageParamName);
            changes.put(TaxApiConstants.percentageParamName, newValue);

            // For percentage change, history must capture old and new start dates. Use the
            // start date as it was before any percentage-change-driven adjustment.
            LocalDate oldStartDate = previousStartDate;
            // Adjust start date for the new percentage; this may honor provided startDate
            updateStartDate(command, changes, true);
            LocalDate newStartDate = this.startDate;

            TaxComponentHistory history = TaxComponentHistory.createTaxComponentHistory(this, this.percentage, oldStartDate, newStartDate);
            this.taxComponentHistories.add(history);
            this.percentage = newValue;

        }

        // Handle debit account type
        if (command.isChangeInIntegerParameterNamed(TaxApiConstants.debitAccountTypeParamName, this.debitAccountType)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(TaxApiConstants.debitAccountTypeParamName);
            changes.put(TaxApiConstants.debitAccountTypeParamName, newValue);
            if (debitAccountType != null) {
                this.debitAccountType = debitAccountType.getValue();
            } else if (newValue != null) {
                final GLAccountType accountType = GLAccountType.fromInt(newValue);
                this.debitAccountType = accountType != null ? accountType.getValue() : null;
            } else {
                this.debitAccountType = null;
            }
        }

        // Handle debit account ID
        final Long currentDebitAccountId = this.debitAccount != null ? this.debitAccount.getId() : null;
        if (command.isChangeInLongParameterNamed(TaxApiConstants.debitAccountIdParamName, currentDebitAccountId)) {
            final Long newValue = command.longValueOfParameterNamed(TaxApiConstants.debitAccountIdParamName);
            changes.put(TaxApiConstants.debitAccountIdParamName, newValue);
            // debitAccount is loaded in service layer if newValue is not null, otherwise it's null
            this.debitAccount = debitAccount;
            // If account ID is set to null, also clear account type
            if (newValue == null && command.parameterExists(TaxApiConstants.debitAccountIdParamName)) {
                this.debitAccountType = null;
                if (!changes.containsKey(TaxApiConstants.debitAccountTypeParamName)) {
                    changes.put(TaxApiConstants.debitAccountTypeParamName, null);
                }
            }
        }

        // Handle credit account type
        if (command.isChangeInIntegerParameterNamed(TaxApiConstants.creditAccountTypeParamName, this.creditAccountType)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(TaxApiConstants.creditAccountTypeParamName);
            changes.put(TaxApiConstants.creditAccountTypeParamName, newValue);
            if (creditAccountType != null) {
                this.creditAccountType = creditAccountType.getValue();
            } else if (newValue != null) {
                final GLAccountType accountType = GLAccountType.fromInt(newValue);
                this.creditAccountType = accountType != null ? accountType.getValue() : null;
            } else {
                this.creditAccountType = null;
            }
        }

        // Handle credit account ID
        final Long currentCreditAccountId = this.creditAccount != null ? this.creditAccount.getId() : null;
        if (command.isChangeInLongParameterNamed(TaxApiConstants.creditAccountIdParamName, currentCreditAccountId)) {
            final Long newValue = command.longValueOfParameterNamed(TaxApiConstants.creditAccountIdParamName);
            changes.put(TaxApiConstants.creditAccountIdParamName, newValue);
            // creditAccount is loaded in service layer if newValue is not null, otherwise it's null
            this.creditAccount = creditAccount;
            // If account ID is set to null, also clear account type
            if (newValue == null && command.parameterExists(TaxApiConstants.creditAccountIdParamName)) {
                this.creditAccountType = null;
                if (!changes.containsKey(TaxApiConstants.creditAccountTypeParamName)) {
                    changes.put(TaxApiConstants.creditAccountTypeParamName, null);
                }
            }
        }

        return changes;
    }

    private void updateStartDate(final JsonCommand command, final Map<String, Object> changes, boolean setAsCurrentDate) {
        LocalDate startDate = DateUtils.getBusinessLocalDate();
        if (command.parameterExists(TaxApiConstants.startDateParamName)) {
            LocalDate startDateFromUI = command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName);
            if (startDateFromUI != null) {
                startDate = startDateFromUI;
            }
            this.startDate = startDate;
            changes.put(TaxApiConstants.startDateParamName, startDate);
        } else if (setAsCurrentDate) {
            changes.put(TaxApiConstants.startDateParamName, startDate);
            this.startDate = startDate;
        }

    }

    public BigDecimal getPercentage() {
        return this.percentage;
    }

    public LocalDate startDate() {
        return this.startDate;
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
        return DateUtils.isAfter(target, startDate());
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

    public GLAccount getDebitAccount() {
        return this.debitAccount;
    }

    public Integer getCreditAccountType() {
        return this.creditAccountType;
    }

    public GLAccount getCreditAccount() {
        return this.creditAccount;
    }

    /**
     * Checks if this tax component is in use. A tax component is considered "in use" ONLY if: - It is linked to
     * TaxGroupMappings (mapped to tax groups) - AND at least one Charge exists that references a TaxGroup containing
     * this TaxComponent
     *
     * Being mapped to TaxGroup alone is NOT considered in use.
     *
     * @param chargeUsageChecker
     *            a supplier that checks if any Charge exists using this tax component via tax groups
     * @return true if the tax component is in use (linked to charges via tax groups), false otherwise
     */
    public boolean isInUse(final Supplier<Boolean> chargeUsageChecker) {
        // First check if component is mapped to any tax groups
        if (this.taxGroupMappings == null || this.taxGroupMappings.isEmpty()) {
            return false;
        }
        // Then check if any charges exist using those tax groups
        return chargeUsageChecker != null && Boolean.TRUE.equals(chargeUsageChecker.get());
    }
}
