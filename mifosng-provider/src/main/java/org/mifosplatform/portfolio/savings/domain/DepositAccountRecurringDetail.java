/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.mandatoryRecommendedDepositAmountParamName;

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
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_account_recurring_detail")
public class DepositAccountRecurringDetail extends AbstractPersistable<Long> {

    @Column(name = "mandatory_recommended_deposit_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal mandatoryRecommendedDepositAmount;

    @Column(name = "total_overdue_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalOverdueAmount;

    @Column(name = "is_calendar_inherited", nullable = false)
    private boolean isCalendarInherited;
    
    @Column(name = "no_of_overdue_installments", nullable = false)
    private Integer noOfOverdueInstallments;

    @Embedded
    private DepositRecurringDetail recurringDetail;

    @OneToOne
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount account;
    
    /**
     * 
     */
    public DepositAccountRecurringDetail() {
        this.noOfOverdueInstallments = 0;
        this.isCalendarInherited = false;
    }

    

    public static DepositAccountRecurringDetail createNew(final BigDecimal mandatoryRecommendedDepositAmount,
            final DepositRecurringDetail recurringDetail, final SavingsAccount account, final boolean isCalendarInherited) {
        final BigDecimal totalOverdueAmount = null;
        final Integer noOfOverdueInstallments = null;
        return new DepositAccountRecurringDetail(mandatoryRecommendedDepositAmount, totalOverdueAmount, noOfOverdueInstallments,
                recurringDetail, account, isCalendarInherited);
    }

    /**
     * @param mandatoryRecommendedDepositAmount
     * @param totalOverdueAmount
     * @param noOfOverdueInstallments
     * @param recurringDetail
     * @param account
     */
    protected DepositAccountRecurringDetail(final BigDecimal mandatoryRecommendedDepositAmount, final BigDecimal totalOverdueAmount,
            final Integer noOfOverdueInstallments, final DepositRecurringDetail recurringDetail, final SavingsAccount account, final boolean isCalendarInherited) {
        this.mandatoryRecommendedDepositAmount = mandatoryRecommendedDepositAmount;
        this.totalOverdueAmount = totalOverdueAmount;
        this.noOfOverdueInstallments = noOfOverdueInstallments;
        this.recurringDetail = recurringDetail;
        this.account = account;
        this.isCalendarInherited = isCalendarInherited;
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(10);
        if (command.isChangeInBigDecimalParameterNamed(mandatoryRecommendedDepositAmountParamName, this.mandatoryRecommendedDepositAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(mandatoryRecommendedDepositAmountParamName);
            actualChanges.put(mandatoryRecommendedDepositAmountParamName, newValue);
            this.mandatoryRecommendedDepositAmount = newValue;
        }
        if (this.recurringDetail != null) {
            actualChanges.putAll(this.recurringDetail.update(command));
        }
        return actualChanges;
    }

    public DepositRecurringDetail recurringDetail() {
        return this.recurringDetail;
    }

    public void updateAccountReference(final SavingsAccount account) {
        this.account = account;
    }

    public boolean isMandatoryDeposit() {
        return this.recurringDetail.isMandatoryDeposit();
    }

    public boolean allowWithdrawal() {
        return this.recurringDetail.allowWithdrawal();
    }

    public boolean adjustAdvanceTowardsFuturePayments() {
        return this.recurringDetail.adjustAdvanceTowardsFuturePayments();
    }

    public BigDecimal mandatoryRecommendedDepositAmount() {
        return this.mandatoryRecommendedDepositAmount;
    }

    public boolean isCalendarInherited(){
        return this.isCalendarInherited;
    }
    
    public DepositAccountRecurringDetail copy() {
        final BigDecimal mandatoryRecommendedDepositAmount = this.mandatoryRecommendedDepositAmount;
        final DepositRecurringDetail recurringDetail = this.recurringDetail.copy();
        final boolean isCalendarInherited = this.isCalendarInherited;
        return DepositAccountRecurringDetail.createNew(mandatoryRecommendedDepositAmount, recurringDetail, null, isCalendarInherited);
    }
    
    public void updateOverdueDetails(final int noOfOverdueInstallments, final Money totalOverdueAmount){
        this.noOfOverdueInstallments = noOfOverdueInstallments;
        this.totalOverdueAmount = totalOverdueAmount.getAmount();
    }
}