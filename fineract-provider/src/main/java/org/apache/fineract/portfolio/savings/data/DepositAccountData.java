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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;

/**
 * Immutable data object representing abstract for Fixed and Recurring Deposit
 * Accounts Accounts.
 */
public class DepositAccountData {

    protected final Long id;
    protected final String accountNo;
    protected final String externalId;
    protected final Long groupId;
    protected final String groupName;
    protected final Long clientId;
    protected final String clientName;
    protected final Long depositProductId;
    protected final String depositProductName;
    protected final Long fieldOfficerId;
    protected final String fieldOfficerName;
    protected final SavingsAccountStatusEnumData status;
    protected final SavingsAccountApplicationTimelineData timeline;
    protected final CurrencyData currency;
    protected final BigDecimal nominalAnnualInterestRate;
    protected final EnumOptionData interestCompoundingPeriodType;
    protected final EnumOptionData interestPostingPeriodType;
    protected final EnumOptionData interestCalculationType;
    protected final EnumOptionData interestCalculationDaysInYearType;
    protected final BigDecimal minRequiredOpeningBalance;
    protected final Integer lockinPeriodFrequency;
    protected final EnumOptionData lockinPeriodFrequencyType;
    protected final boolean withdrawalFeeForTransfers;
    protected final EnumOptionData depositType;
    protected final BigDecimal minBalanceForInterestCalculation;
    protected final boolean withHoldTax;
    protected final TaxGroupData taxGroup;

    // associations
    protected final SavingsAccountSummaryData summary;
    protected final Collection<SavingsAccountTransactionData> transactions;

    protected final Collection<SavingsAccountChargeData> charges;
    protected final DepositAccountInterestRateChartData accountChart;

    // template
    protected final Collection<DepositProductData> productOptions;
    protected final Collection<StaffData> fieldOfficerOptions;
    protected final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions;
    protected final Collection<EnumOptionData> interestPostingPeriodTypeOptions;
    protected final Collection<EnumOptionData> interestCalculationTypeOptions;
    protected final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions;
    protected final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;
    protected final Collection<EnumOptionData> withdrawalFeeTypeOptions;
    protected final Collection<ChargeData> chargeOptions;

    protected final SavingsAccountChargeData withdrawalFee;
    protected final SavingsAccountChargeData annualFee;

    protected final DepositAccountInterestRateChartData chartTemplate;

    //import fields
    private Long productId;

    public DepositAccountData(Long clientId,Long productId,Long fieldOfficerId,
            EnumOptionData interestCompoundingPeriodType, EnumOptionData interestPostingPeriodType,
            EnumOptionData interestCalculationType,EnumOptionData interestCalculationDaysInYearType,Integer lockinPeriodFrequency,
            EnumOptionData lockinPeriodFrequencyType,String externalId,Collection<SavingsAccountChargeData> charges) {
        this.id = null;
        this.accountNo = null;
        this.externalId = externalId;
        this.groupId = null;
        this.groupName = null;
        this.clientId = clientId;
        this.clientName = null;
        this.depositProductId = null;
        this.depositProductName = null;
        this.fieldOfficerId = fieldOfficerId;
        this.fieldOfficerName = null;
        this.status = null;
        this.timeline = null;
        this.currency = null;
        this.nominalAnnualInterestRate = null;
        this.interestCompoundingPeriodType = interestCompoundingPeriodType;
        this.interestPostingPeriodType = interestPostingPeriodType;
        this.interestCalculationType = interestCalculationType;
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType;
        this.minRequiredOpeningBalance = null;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;
        this.withdrawalFeeForTransfers = false;
        this.depositType = null;
        this.minBalanceForInterestCalculation = null;
        this.withHoldTax = false;
        this.taxGroup = null;
        this.summary = null;
        this.transactions = null;
        this.charges = charges;
        this.accountChart = null;
        this.productOptions = null;
        this.fieldOfficerOptions = null;
        this.interestCompoundingPeriodTypeOptions = null;
        this.interestPostingPeriodTypeOptions = null;
        this.interestCalculationTypeOptions = null;
        this.interestCalculationDaysInYearTypeOptions = null;
        this.lockinPeriodFrequencyTypeOptions = null;
        this.withdrawalFeeTypeOptions = null;
        this.chargeOptions = null;
        this.withdrawalFee = null;
        this.annualFee = null;
        this.chartTemplate = null;
        this.productId=productId;
    }

    public static DepositAccountData instance(final Long id, final String accountNo, final String externalId, final Long groupId,
            final String groupName, final Long clientId, final String clientName, final Long productId, final String productName,
            final Long fieldOfficerId, final String fieldOfficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal interestRate,
            final EnumOptionData interestCompoundingPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final boolean withdrawalFeeForTransfers,
            final SavingsAccountSummaryData summary, final EnumOptionData depositType, final BigDecimal minBalanceForInterestCalculation,
            final boolean withHoldTax, final TaxGroupData taxGroup) {

        final Collection<DepositProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountTransactionData> transactions = null;
        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;
        final DepositAccountInterestRateChartData accountChart = null;
        final DepositAccountInterestRateChartData chartTemplate = null;

        return new DepositAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions, productOptions,
                fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, depositType,
                minBalanceForInterestCalculation, withHoldTax, taxGroup);
    }

    public static DepositAccountData lookup(final Long id, final String accountNo, final EnumOptionData depositType) {

        final String externalId = null;
        final Long groupId = null;
        final String groupName = null;
        final Long clientId = null;
        final String clientName = null;
        final Long productId = null;
        final String productName = null;
        final Long fieldOfficerId = null;
        final String fieldOfficerName = null;
        final SavingsAccountStatusEnumData status = null;
        final SavingsAccountApplicationTimelineData timeline = null;
        final CurrencyData currency = null;
        final BigDecimal interestRate = null;
        final EnumOptionData interestCompoundingPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final boolean withdrawalFeeForTransfers = false;
        final SavingsAccountSummaryData summary = null;
        final Collection<DepositProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountTransactionData> transactions = null;
        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;
        final DepositAccountInterestRateChartData accountChart = null;
        final DepositAccountInterestRateChartData chartTemplate = null;
        final BigDecimal minBalanceForInterestCalculation = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;

        return new DepositAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions, productOptions,
                fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, depositType,
                minBalanceForInterestCalculation, withHoldTax, taxGroup);
    }

    protected DepositAccountData(final Long id, final String accountNo, final String externalId, final Long groupId,
            final String groupName, final Long clientId, final String clientName, final Long productId, final String productName,
            final Long fieldofficerId, final String fieldofficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal nominalAnnualInterestRate,
            final EnumOptionData interestPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final boolean withdrawalFeeForTransfers,
            final SavingsAccountSummaryData summary, final Collection<SavingsAccountTransactionData> transactions,
            final Collection<DepositProductData> productOptions, final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountChargeData> charges, final Collection<ChargeData> chargeOptions,
            final DepositAccountInterestRateChartData accountChart, final DepositAccountInterestRateChartData chartTemplate,
            final EnumOptionData depositType, final BigDecimal minBalanceForInterestCalculation, final boolean withHoldTax,
            final TaxGroupData taxGroup) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.depositProductId = productId;
        this.depositProductName = productName;
        this.fieldOfficerId = fieldofficerId;
        this.fieldOfficerName = fieldofficerName;
        this.status = status;
        this.timeline = timeline;
        this.currency = currency;
        this.nominalAnnualInterestRate = nominalAnnualInterestRate;
        this.interestCompoundingPeriodType = interestPeriodType;
        this.interestPostingPeriodType = interestPostingPeriodType;
        this.interestCalculationType = interestCalculationType;
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType;
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;
        this.withdrawalFeeForTransfers = withdrawalFeeForTransfers;
        this.summary = summary;
        this.transactions = transactions;
        this.productOptions = productOptions;
        this.fieldOfficerOptions = fieldOfficerOptions;
        this.interestCompoundingPeriodTypeOptions = interestCompoundingPeriodTypeOptions;
        this.interestPostingPeriodTypeOptions = interestPostingPeriodTypeOptions;
        this.interestCalculationTypeOptions = interestCalculationTypeOptions;
        this.interestCalculationDaysInYearTypeOptions = interestCalculationDaysInYearTypeOptions;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
        this.withdrawalFeeTypeOptions = withdrawalFeeTypeOptions;

        this.charges = charges;// charges associated with Savings account
        // charges available for adding to Savings account
        this.chargeOptions = chargeOptions;

        this.withdrawalFee = getWithdrawalFee();

        this.annualFee = getAnnualFee();

        this.accountChart = accountChart;
        this.chartTemplate = chartTemplate;
        this.depositType = depositType;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
        this.taxGroup = taxGroup;
        this.withHoldTax = withHoldTax;
    }

    private SavingsAccountChargeData getWithdrawalFee() {
        for (SavingsAccountChargeData charge : this.charges()) {
            if (charge.isWithdrawalFee()) return charge;
        }
        return null;
    }

    private SavingsAccountChargeData getAnnualFee() {
        for (SavingsAccountChargeData charge : this.charges()) {
            if (charge.isAnnualFee()) return charge;
        }
        return null;
    }

    public Long id() {
        return this.id;
    }

    public Long clientId() {
        return this.clientId;
    }

    public Long groupId() {
        return this.groupId;
    }

    public Long productId() {
        return this.depositProductId;
    }

    public CurrencyData currency() {
        return this.currency;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final DepositAccountData rhs = (DepositAccountData) obj;
        return new EqualsBuilder().append(this.id, rhs.id).append(this.accountNo, rhs.accountNo).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.id).append(this.accountNo).toHashCode();
    }

    public Collection<SavingsAccountChargeData> charges() {
        return (this.charges == null) ? new HashSet<SavingsAccountChargeData>() : this.charges;
    }

    public EnumOptionData depositType() {
        return depositType;
    }

    public String accountNo() {
        return accountNo;
    }
}