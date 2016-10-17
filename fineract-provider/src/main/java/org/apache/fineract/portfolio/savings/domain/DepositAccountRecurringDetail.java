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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.mandatoryRecommendedDepositAmountParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_deposit_account_recurring_detail")
public class DepositAccountRecurringDetail extends AbstractPersistableCustom<Long> {

    @Column(name = "mandatory_recommended_deposit_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal mandatoryRecommendedDepositAmount;

    @Column(name = "total_overdue_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalOverdueAmount;

    @Column(name = "is_calendar_inherited", nullable = false)
    private boolean isCalendarInherited;

    @Column(name = "no_of_overdue_installments", nullable = true)
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
            final Integer noOfOverdueInstallments, final DepositRecurringDetail recurringDetail, final SavingsAccount account,
            final boolean isCalendarInherited) {
        this.mandatoryRecommendedDepositAmount = mandatoryRecommendedDepositAmount;
        this.totalOverdueAmount = totalOverdueAmount;
        this.noOfOverdueInstallments = noOfOverdueInstallments;
        this.recurringDetail = recurringDetail;
        this.account = account;
        this.isCalendarInherited = isCalendarInherited;
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);
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

    public Map<String, Object> updateMandatoryRecommendedDepositAmount(BigDecimal newMandatoryRecommendedDepositAmount,
            LocalDate effectiveDate, Boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);
        actualChanges.put(mandatoryRecommendedDepositAmountParamName, newMandatoryRecommendedDepositAmount);
        this.mandatoryRecommendedDepositAmount = newMandatoryRecommendedDepositAmount;
        RecurringDepositAccount depositAccount = (RecurringDepositAccount) this.account;
        if (depositAccount.isNotActive()) {
            final String defaultUserMessage = "Updates to the recommended deposit amount are allowed only when the underlying account is active.";
            final ApiParameterError error = ApiParameterError.generalError("error.msg."
                    + DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + ".is.not.active", defaultUserMessage);
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        depositAccount.updateScheduleInstallmentsWithNewRecommendedDepositAmount(newMandatoryRecommendedDepositAmount, effectiveDate);
        depositAccount.updateOverduePayments(DateUtils.getLocalDateOfTenant());
        MathContext mc = MathContext.DECIMAL64;
        Boolean isPreMatureClosure = false;
        depositAccount.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);
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

    public boolean isCalendarInherited() {
        return this.isCalendarInherited;
    }

    public DepositAccountRecurringDetail copy() {
        final BigDecimal mandatoryRecommendedDepositAmount = this.mandatoryRecommendedDepositAmount;
        final DepositRecurringDetail recurringDetail = this.recurringDetail.copy();
        final boolean isCalendarInherited = this.isCalendarInherited;
        return DepositAccountRecurringDetail.createNew(mandatoryRecommendedDepositAmount, recurringDetail, null, isCalendarInherited);
    }

    public void updateOverdueDetails(final int noOfOverdueInstallments, final Money totalOverdueAmount) {
        this.noOfOverdueInstallments = noOfOverdueInstallments;
        this.totalOverdueAmount = totalOverdueAmount.getAmount();
    }
}