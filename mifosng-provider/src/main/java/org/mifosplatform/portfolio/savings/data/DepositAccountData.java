/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.charge.data.ChargeData;

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

    public static DepositAccountData instance(final Long id, final String accountNo, final String externalId, final Long groupId,
            final String groupName, final Long clientId, final String clientName, final Long productId, final String productName,
            final Long fieldOfficerId, final String fieldOfficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal interestRate,
            final EnumOptionData interestCompoundingPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final boolean withdrawalFeeForTransfers,
            final SavingsAccountSummaryData summary, final EnumOptionData depositType, final BigDecimal minBalanceForInterestCalculation) {

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
                minBalanceForInterestCalculation);
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

        return new DepositAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions, productOptions,
                fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, depositType,
                minBalanceForInterestCalculation);
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
            final EnumOptionData depositType, final BigDecimal minBalanceForInterestCalculation) {
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