/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.recurringDepositAmountParamName;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_account_recurring_detail")
public class DepositAccountRecurringDetail extends AbstractPersistable<Long> {

    @Column(name = "recurring_deposit_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal recurringDepositAmount;
    
    @Embedded
    private DepositRecurringDetail recurringDetail;

    @OneToOne
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount account;

    protected DepositAccountRecurringDetail() {
        super();
    }

    public static DepositAccountRecurringDetail createNew(final BigDecimal recurringDepositAmount, DepositRecurringDetail recurringDetail, SavingsAccount account) {

        return new DepositAccountRecurringDetail(recurringDepositAmount, recurringDetail, account);
    }

    private DepositAccountRecurringDetail(final BigDecimal recurringDepositAmount, DepositRecurringDetail recurringDetail, SavingsAccount account) {
        this.recurringDepositAmount = recurringDepositAmount;
        this.recurringDetail = recurringDetail;
        this.account = account;
    }

    public Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(10);
        if (command.isChangeInBigDecimalParameterNamed(recurringDepositAmountParamName, this.recurringDepositAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(recurringDepositAmountParamName);
            actualChanges.put(recurringDepositAmountParamName, newValue);
            this.recurringDepositAmount = newValue;
        }
        if (this.recurringDetail != null) {
            actualChanges.putAll(this.recurringDetail.update(command, baseDataValidator));
        }
        return actualChanges;
    }

    public DepositRecurringDetail recurringDetail() {
        return this.recurringDetail;
    }

    public void updateAccountReference(final SavingsAccount account) {
        this.account = account;
    }
    
    public Integer recurringDepositType() {
        return this.recurringDetail().recurringDepositType();
    }

    public Integer recurringDepositFrequency() {
        return this.recurringDetail().recurringDepositFrequency();
    }

    public Integer recurringDepositFrequencyTypeId() {
        return this.recurringDetail().recurringDepositFrequencyTypeId();
    }
    
    public SavingsPeriodFrequencyType recurringDepositFrequencyType() {
        return SavingsPeriodFrequencyType.fromInt(this.recurringDepositFrequencyTypeId());
    }
    
    public BigDecimal recurringDepositAmount(){
        return this.recurringDepositAmount;
    }
    
    public DepositAccountRecurringDetail copy(){
        final BigDecimal recurringDepositAmount = this.recurringDepositAmount;
        final DepositRecurringDetail recurringDetail = this.recurringDetail.copy();
        return DepositAccountRecurringDetail.createNew(recurringDepositAmount, recurringDetail, account);
    }
}