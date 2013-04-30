/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosplatform.accounting.autoposting.api.AutoPostingJsonInputParams;
import org.mifosplatform.accounting.rule.domain.AccountingRule;
import org.mifosplatform.infrastructure.codes.domain.Code;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "acc_auto_posting", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "auto_posting_name_unique") })
public class AutoPosting extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = true, length = 100)
    private String name;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = true)
    private Office office;

    @Column(name = "type_enum", nullable = false)
    private Integer productTypeEnum;

    @Column(name = "product_id", nullable = true)
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "charge_id", nullable = true)
    private Charge charge;

    @ManyToOne
    @JoinColumn(name = "event", nullable = false)
    private Code event;

    @ManyToOne
    @JoinColumn(name = "event_attribute", nullable = true)
    private CodeValue eventAttribute;

    @ManyToOne
    @JoinColumn(name = "accounting_rule_id", nullable = true)
    private AccountingRule accountingRule;

    protected AutoPosting() {
        //
    }

    private AutoPosting(Office office, Charge charge, Code event, CodeValue eventAttribute, AccountingRule accountingRule, String name,
            String description, Integer productTypeEnum, Long productId) {
        this.name = name;
        this.description = description;
        this.office = office;
        this.productTypeEnum = productTypeEnum;
        this.productId = productId;
        this.charge = charge;
        this.event = event;
        this.eventAttribute = eventAttribute;
        this.accountingRule = accountingRule;
    }

    public static AutoPosting fromJson(Office office, Charge charge, Code event, CodeValue eventAttribute, AccountingRule accountingRule,
            JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(AutoPostingJsonInputParams.NAME.getValue());
        final String description = command.stringValueOfParameterNamed(AutoPostingJsonInputParams.DESCRIPTION.getValue());
        final Integer productTypeEnum = command.integerValueSansLocaleOfParameterNamed(AutoPostingJsonInputParams.PRODUCT_TYPE_ENUM
                .getValue());
        final Long productId = command.longValueOfParameterNamed(AutoPostingJsonInputParams.PRODUCT_ID.getValue());
        return new AutoPosting(office, charge, event, eventAttribute, accountingRule, name, description, productTypeEnum, productId);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(5);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.OFFICE_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.CHARGE_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.EVENT_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.EVENT_ATTRIBUTE_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.ACCOUNTING_RULE_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.NAME.getValue(), this.name);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.DESCRIPTION.getValue(), this.description);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.PRODUCT_TYPE_ENUM.getValue(), this.productTypeEnum, true);
        handlePropertyUpdate(command, actualChanges, AutoPostingJsonInputParams.PRODUCT_ID.getValue(), this.productId);
        return actualChanges;
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final String propertyToBeUpdated) {
        if (command.isChangeInStringParameterNamed(paramName, propertyToBeUpdated)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(AutoPostingJsonInputParams.DESCRIPTION.getValue())) {
                this.description = newValue;
            } else if (paramName.equals(AutoPostingJsonInputParams.NAME.getValue())) {
                this.name = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            Integer propertyToBeUpdated, boolean sansLocale) {
        if (command.isChangeInIntegerParameterNamed(paramName, propertyToBeUpdated)) {
            Integer newValue = null;
            if (sansLocale) {
                newValue = command.integerValueSansLocaleOfParameterNamed(paramName);
            } else {
                newValue = command.integerValueOfParameterNamed(paramName);
            }
            actualChanges.put(paramName, newValue);
            propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(AutoPostingJsonInputParams.PRODUCT_TYPE_ENUM.getValue())) {
                this.productTypeEnum = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final Long propertyToBeUpdated) {
        if (command.isChangeInLongParameterNamed(paramName, propertyToBeUpdated)) {
            final Long newValue = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(AutoPostingJsonInputParams.OFFICE_ID.getValue())) {
                // do nothing as this is a nested property
            } else if (paramName.equals(AutoPostingJsonInputParams.CHARGE_ID.getValue())) {
                // do nothing as this is a nested property
            } else if (paramName.equals(AutoPostingJsonInputParams.EVENT_ID.getValue())) {
                // do nothing as this is a nested property
            } else if (paramName.equals(AutoPostingJsonInputParams.EVENT_ATTRIBUTE_ID.getValue())) {
                // do nothing as this is a nested property
            } else if (paramName.equals(AutoPostingJsonInputParams.ACCOUNTING_RULE_ID.getValue())) {
                // do nothing as this is a nested property
            }
        }
    }

    public Office getOffice() {
        return this.office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Charge getCharge() {
        return this.charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public Code getEvent() {
        return this.event;
    }

    public void setEvent(Code event) {
        this.event = event;
    }

    public CodeValue getEventAttribute() {
        return this.eventAttribute;
    }

    public void setEventAttribute(CodeValue eventAttribute) {
        this.eventAttribute = eventAttribute;
    }

    public AccountingRule getAccountingRule() {
        return this.accountingRule;
    }

    public void setAccountingRule(AccountingRule accountingRule) {
        this.accountingRule = accountingRule;
    }

}