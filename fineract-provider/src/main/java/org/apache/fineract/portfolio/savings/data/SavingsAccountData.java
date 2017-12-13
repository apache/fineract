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
import java.util.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a savings account.
 */
public class SavingsAccountData {

    private final Long id;
    private final String accountNo;
    private final EnumOptionData depositType;
    private final String externalId;
    private final Long groupId;
    private final String groupName;
    private final Long clientId;
    private final String clientName;
    private final Long savingsProductId;
    private final String savingsProductName;
    private final Long fieldOfficerId;
    private final String fieldOfficerName;
    private final SavingsAccountStatusEnumData status;
    private final SavingsAccountSubStatusEnumData subStatus;
    private final SavingsAccountApplicationTimelineData timeline;
    private final CurrencyData currency;
    private final BigDecimal nominalAnnualInterestRate;
    private final EnumOptionData interestCompoundingPeriodType;
    private final EnumOptionData interestPostingPeriodType;
    private final EnumOptionData interestCalculationType;
    private final EnumOptionData interestCalculationDaysInYearType;
    private final BigDecimal minRequiredOpeningBalance;
    private final Integer lockinPeriodFrequency;
    private final EnumOptionData lockinPeriodFrequencyType;
    private final boolean withdrawalFeeForTransfers;
    private final boolean allowOverdraft;
    private final BigDecimal overdraftLimit;
    private final BigDecimal minRequiredBalance;
    private final boolean enforceMinRequiredBalance;
    private final BigDecimal minBalanceForInterestCalculation;
    private final BigDecimal onHoldFunds;
    private final boolean withHoldTax;
    private final TaxGroupData taxGroup;
    private final LocalDate lastActiveTransactionDate;
    private final boolean isDormancyTrackingActive;
    private final Integer daysToInactive;
    private final Integer daysToDormancy;
    private final Integer daysToEscheat;
    private final BigDecimal savingsAmountOnHold;

    // associations
    private final SavingsAccountSummaryData summary;
    @SuppressWarnings("unused")
    private final Collection<SavingsAccountTransactionData> transactions;

    private final Collection<SavingsAccountChargeData> charges;

    // template
    private final Collection<SavingsProductData> productOptions;
    private final Collection<StaffData> fieldOfficerOptions;
    private final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions;
    private final Collection<EnumOptionData> interestPostingPeriodTypeOptions;
    private final Collection<EnumOptionData> interestCalculationTypeOptions;
    private final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions;
    private final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;
    private final Collection<EnumOptionData> withdrawalFeeTypeOptions;
    private final Collection<ChargeData> chargeOptions;

    @SuppressWarnings("unused")
    private final SavingsAccountChargeData withdrawalFee;
    @SuppressWarnings("unused")
    private final SavingsAccountChargeData annualFee;
    private final BigDecimal nominalAnnualInterestRateOverdraft;
    private final BigDecimal minOverdraftForInterestCalculation;

    private List<DatatableData> datatables = null;

    //import field
    private Long productId;
    private String locale;
    private String dateFormat;
    private transient Integer rowIndex;
    private LocalDate submittedOnDate;

    public static SavingsAccountData importInstanceIndividual(Long clientId, Long productId, Long fieldOfficerId,LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodTypeEnum,
            EnumOptionData interestPostingPeriodTypeEnum,EnumOptionData interestCalculationTypeEnum,
            EnumOptionData interestCalculationDaysInYearTypeEnum,BigDecimal minRequiredOpeningBalance,
            Integer lockinPeriodFrequency,EnumOptionData lockinPeriodFrequencyTypeEnum, boolean applyWithdrawalFeeForTransfers,
            Integer rowIndex,String externalId,Collection<SavingsAccountChargeData> charges,boolean allowOverdraft,
            BigDecimal overdraftLimit,String locale, String dateFormat){
        return new SavingsAccountData(clientId, productId, fieldOfficerId, submittedOnDate, nominalAnnualInterestRate,
                interestCompoundingPeriodTypeEnum, interestPostingPeriodTypeEnum, interestCalculationTypeEnum,
                interestCalculationDaysInYearTypeEnum, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyTypeEnum,
                applyWithdrawalFeeForTransfers, rowIndex, externalId, charges, allowOverdraft, overdraftLimit,locale,dateFormat);

    }

    private SavingsAccountData(Long clientId, Long productId, Long fieldOfficerId,LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodType,
            EnumOptionData interestPostingPeriodType,EnumOptionData interestCalculationType,
            EnumOptionData interestCalculationDaysInYearType,BigDecimal minRequiredOpeningBalance,
            Integer lockinPeriodFrequency,EnumOptionData lockinPeriodFrequencyType, boolean withdrawalFeeForTransfers,
            Integer rowIndex,String externalId,Collection<SavingsAccountChargeData> charges,boolean allowOverdraft,
            BigDecimal overdraftLimit,String locale, String dateFormat) {
        this.id = null;
        this.accountNo = null;
        this.depositType = null;
        this.externalId = externalId;
        this.groupId = null;
        this.groupName = null;
        this.clientId = clientId;
        this.clientName = null;
        this.savingsProductId = null;
        this.savingsProductName = null;
        this.fieldOfficerId = fieldOfficerId;
        this.fieldOfficerName = null;
        this.status = null;
        this.subStatus = null;
        this.timeline = null;
        this.currency = null;
        this.nominalAnnualInterestRate = nominalAnnualInterestRate;
        this.interestCompoundingPeriodType = interestCompoundingPeriodType;
        this.interestPostingPeriodType = interestPostingPeriodType;
        this.interestCalculationType = interestCalculationType;
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType;
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;
        this.withdrawalFeeForTransfers = withdrawalFeeForTransfers;
        this.allowOverdraft = allowOverdraft;
        this.overdraftLimit = overdraftLimit;
        this.minRequiredBalance = null;
        this.enforceMinRequiredBalance = false;
        this.minBalanceForInterestCalculation = null;
        this.onHoldFunds = null;
        this.withHoldTax = false;
        this.taxGroup = null;
        this.lastActiveTransactionDate = null;
        this.isDormancyTrackingActive = false;
        this.daysToInactive = null;
        this.daysToDormancy = null;
        this.daysToEscheat = null;
        this.summary = null;
        this.transactions = null;
        this.charges = charges;
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
        this.nominalAnnualInterestRateOverdraft = null;
        this.minOverdraftForInterestCalculation = null;
        this.datatables = null;
        this.productId = productId;
        this.dateFormat=dateFormat;
        this.locale= locale;
        this.rowIndex = rowIndex;
        this.submittedOnDate=submittedOnDate;
        this.savingsAmountOnHold=null;
    }

    public static final Comparator<SavingsAccountData> ClientNameComparator = new Comparator<SavingsAccountData>() {

        @Override
        public int compare(SavingsAccountData savings1, SavingsAccountData savings2) {
            String clientOfSavings1 = savings1.getClientName().toUpperCase(Locale.ENGLISH);
            String clientOfSavings2 = savings2.getClientName().toUpperCase(Locale.ENGLISH);
            return clientOfSavings1.compareTo(clientOfSavings2);
        }
    };

    public String getClientName() {
        return clientName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getSavingsProductName() {
        return savingsProductName;
    }

    public BigDecimal getMinRequiredOpeningBalance() {
        return minRequiredOpeningBalance;
    }

    public SavingsAccountApplicationTimelineData getTimeline() {
        return timeline;
    }

    public static SavingsAccountData importInstanceGroup(Long groupId, Long productId, Long fieldOfficerId,
            LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodTypeEnum,
            EnumOptionData interestPostingPeriodTypeEnum,EnumOptionData interestCalculationTypeEnum,
            EnumOptionData interestCalculationDaysInYearTypeEnum,BigDecimal minRequiredOpeningBalance,
            Integer lockinPeriodFrequency,EnumOptionData lockinPeriodFrequencyTypeEnum,
            boolean applyWithdrawalFeeForTransfers,
            Integer rowIndex,String externalId,Collection<SavingsAccountChargeData> charges,
            boolean allowOverdraft,
            BigDecimal overdraftLimit,String locale, String dateFormat){

        return new SavingsAccountData(groupId, productId, fieldOfficerId, submittedOnDate, nominalAnnualInterestRate,
                interestCompoundingPeriodTypeEnum, interestPostingPeriodTypeEnum, interestCalculationTypeEnum,
                interestCalculationDaysInYearTypeEnum, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyTypeEnum,
                applyWithdrawalFeeForTransfers, rowIndex, externalId, charges, allowOverdraft, overdraftLimit,null,locale,dateFormat);

    }
    private SavingsAccountData(Long groupId, Long productId, Long fieldOfficerId,LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodType,
            EnumOptionData interestPostingPeriodType,EnumOptionData interestCalculationType,
            EnumOptionData interestCalculationDaysInYearType,BigDecimal minRequiredOpeningBalance,
            Integer lockinPeriodFrequency,EnumOptionData lockinPeriodFrequencyType, boolean withdrawalFeeForTransfers,
            Integer rowIndex,String externalId,Collection<SavingsAccountChargeData> charges,boolean allowOverdraft,
            BigDecimal overdraftLimit,Long id,String locale, String dateFormat) {
        this.id = id;
        this.accountNo = null;
        this.depositType = null;
        this.externalId = externalId;
        this.groupId = groupId;
        this.groupName = null;
        this.clientId = null;
        this.clientName = null;
        this.savingsProductId = null;
        this.savingsProductName = null;
        this.fieldOfficerId = fieldOfficerId;
        this.fieldOfficerName = null;
        this.status = null;
        this.subStatus = null;
        this.timeline = null;
        this.currency = null;
        this.nominalAnnualInterestRate = nominalAnnualInterestRate;
        this.interestCompoundingPeriodType = interestCompoundingPeriodType;
        this.interestPostingPeriodType = interestPostingPeriodType;
        this.interestCalculationType = interestCalculationType;
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType;
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;
        this.withdrawalFeeForTransfers = withdrawalFeeForTransfers;
        this.allowOverdraft = allowOverdraft;
        this.overdraftLimit = overdraftLimit;
        this.minRequiredBalance = null;
        this.enforceMinRequiredBalance = false;
        this.minBalanceForInterestCalculation = null;
        this.onHoldFunds = null;
        this.withHoldTax = false;
        this.taxGroup = null;
        this.lastActiveTransactionDate = null;
        this.isDormancyTrackingActive = false;
        this.daysToInactive = null;
        this.daysToDormancy = null;
        this.daysToEscheat = null;
        this.summary = null;
        this.transactions = null;
        this.charges = charges;
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
        this.nominalAnnualInterestRateOverdraft = null;
        this.minOverdraftForInterestCalculation = null;
        this.datatables = null;
        this.productId = productId;
        this.dateFormat= dateFormat;
        this.locale= locale;
        this.rowIndex = rowIndex;
        this.submittedOnDate=submittedOnDate;
        this.savingsAmountOnHold=null;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public static SavingsAccountData instance(final Long id, final String accountNo, final EnumOptionData depositType,
            final String externalId, final Long groupId, final String groupName, final Long clientId, final String clientName,
            final Long productId, final String productName, final Long fieldOfficerId, final String fieldOfficerName,
            final SavingsAccountStatusEnumData status, SavingsAccountSubStatusEnumData subStatus, final SavingsAccountApplicationTimelineData timeline,
            final CurrencyData currency, final BigDecimal interestRate,
            final EnumOptionData interestCompoundingPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType,
            final boolean withdrawalFeeForTransfers, final SavingsAccountSummaryData summary, final boolean allowOverdraft,
            final BigDecimal overdraftLimit, final BigDecimal minRequiredBalance,
            final boolean enforceMinRequiredBalance, final BigDecimal minBalanceForInterestCalculation,
            final BigDecimal onHoldFunds, final BigDecimal nominalAnnualInterestRateOverdraft,
            final BigDecimal minOverdraftForInterestCalculation, final boolean withHoldTax, final TaxGroupData taxGroup, 
            final LocalDate lastActiveTransactionDate, final boolean isDormancyTrackingActive, final Integer daysToInactive, 
            final Integer daysToDormancy, final Integer daysToEscheat, final BigDecimal savingsAmountOnHold) {

        final Collection<SavingsProductData> productOptions = null;
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

        return new SavingsAccountData(id, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, timeline, currency, interestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions,
                productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, minBalanceForInterestCalculation, onHoldFunds,
                nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup, 
                lastActiveTransactionDate, isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat, savingsAmountOnHold);
    }

    public static SavingsAccountData lookup(final Long accountId, final String accountNo, final EnumOptionData depositType) {

        final String externalId = null;
        final Long productId = null;
        final Long groupId = null;
        final Long clientId = null;
        final String clientName = null;
        final String groupName = null;
        final String productName = null;
        final Long fieldOfficerId = null;
        final String fieldOfficerName = null;
        final SavingsAccountStatusEnumData status = null;
        final SavingsAccountApplicationTimelineData timeline = null;
        final CurrencyData currency = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final EnumOptionData interestPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        // final BigDecimal withdrawalFeeAmount = null;
        // final EnumOptionData withdrawalFeeType = null;
        final boolean withdrawalFeeForTransfers = false;
        // final BigDecimal annualFeeAmount = null;
        // final MonthDay annualFeeOnMonthDay = null;
        // final LocalDate annualFeeNextDueDate = null;
        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = null;
        final BigDecimal nominalAnnualInterestRateOverdraft = null;
        final BigDecimal minOverdraftForInterestCalculation = null;
        final BigDecimal minRequiredBalance = null;
        final boolean enforceMinRequiredBalance = false;
        final BigDecimal minBalanceForInterestCalculation = null;
        final BigDecimal onHoldFunds = null;

        final SavingsAccountSummaryData summary = null;
        final Collection<SavingsAccountTransactionData> transactions = null;

        final Collection<SavingsProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;
        final SavingsAccountSubStatusEnumData subStatus = null;
        final LocalDate lastActiveTransactionDate = null;
        final boolean isDormancyTrackingActive = false;
        final Integer daysToInactive = null;
        final Integer daysToDormancy = null;
        final Integer daysToEscheat = null;
        final BigDecimal savingsAmountOnHold = null;

        return new SavingsAccountData(accountId, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, timeline, currency, nominalAnnualInterestRate,
                interestPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions,
                productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, minBalanceForInterestCalculation, onHoldFunds,
                nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup, 
                lastActiveTransactionDate, isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat, savingsAmountOnHold);
    }

    public static SavingsAccountData lookupWithProductDetails(final Long accountId, final String accountNo,
            final EnumOptionData depositType, final Long productId, final String productName, final SavingsAccountStatusEnumData status) {

        final String externalId = null;
        final Long groupId = null;
        final Long clientId = null;
        final String clientName = null;
        final String groupName = null;
        final Long fieldOfficerId = null;
        final String fieldOfficerName = null;
        final SavingsAccountApplicationTimelineData timeline = null;
        final CurrencyData currency = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final EnumOptionData interestPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        // final BigDecimal withdrawalFeeAmount = null;
        // final EnumOptionData withdrawalFeeType = null;
        final boolean withdrawalFeeForTransfers = false;
        // final BigDecimal annualFeeAmount = null;
        // final MonthDay annualFeeOnMonthDay = null;
        // final LocalDate annualFeeNextDueDate = null;
        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = null;
        final BigDecimal nominalAnnualInterestRateOverdraft = null;
        final BigDecimal minOverdraftForInterestCalculation = null;
        final BigDecimal minRequiredBalance = null;
        final boolean enforceMinRequiredBalance = false;
        final BigDecimal minBalanceForInterestCalculation = null;
        final BigDecimal onHoldFunds = null;

        final SavingsAccountSummaryData summary = null;
        final Collection<SavingsAccountTransactionData> transactions = null;

        final Collection<SavingsProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;
        final SavingsAccountSubStatusEnumData subStatus = null;
        final LocalDate lastActiveTransactionDate = null;
        final boolean isDormancyTrackingActive = false;
        final Integer daysToInactive = null;
        final Integer daysToDormancy = null;
        final Integer daysToEscheat = null;
		final BigDecimal savingsAmountOnHold = null;

        return new SavingsAccountData(accountId, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, timeline, currency, nominalAnnualInterestRate,
                interestPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions,
                productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, minBalanceForInterestCalculation, onHoldFunds,
                nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup, 
                lastActiveTransactionDate, isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat, savingsAmountOnHold);
    }

    public static SavingsAccountData withTemplateOptions(final SavingsAccountData account, final SavingsAccountData template,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsAccountChargeData> charges) {

        if (template == null) {
            final Collection<SavingsProductData> productOptions = null;
            final Collection<StaffData> fieldOfficerOptions = null;
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
            final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
            final Collection<ChargeData> chargeOptions = null;

            return withTemplateOptions(account, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                    interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                    lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, transactions, charges, chargeOptions);
        }

        return new SavingsAccountData(account.id, account.accountNo, account.depositType, account.externalId, account.groupId,
                account.groupName, account.clientId, account.clientName, account.savingsProductId, account.savingsProductName,
                account.fieldOfficerId, account.fieldOfficerName, account.status, account.subStatus, account.timeline,
                account.currency, account.nominalAnnualInterestRate, account.interestCompoundingPeriodType,
                account.interestPostingPeriodType, account.interestCalculationType, account.interestCalculationDaysInYearType,
                account.minRequiredOpeningBalance, account.lockinPeriodFrequency, account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers,
                account.summary, transactions, template.productOptions, template.fieldOfficerOptions,
                template.interestCompoundingPeriodTypeOptions, template.interestPostingPeriodTypeOptions,
                template.interestCalculationTypeOptions, template.interestCalculationDaysInYearTypeOptions,
                template.lockinPeriodFrequencyTypeOptions, template.withdrawalFeeTypeOptions, charges, template.chargeOptions, account.allowOverdraft,
                account.overdraftLimit, account.minRequiredBalance, account.enforceMinRequiredBalance,
                account.minBalanceForInterestCalculation, account.onHoldFunds, account.nominalAnnualInterestRateOverdraft,
                account.minOverdraftForInterestCalculation, account.withHoldTax, account.taxGroup, 
                account.lastActiveTransactionDate, account.isDormancyTrackingActive, account.daysToInactive, 
                account.daysToDormancy, account.daysToEscheat, account.savingsAmountOnHold);
    }

    public static SavingsAccountData withTemplateOptions(final SavingsAccountData account,
            final Collection<SavingsProductData> productOptions, final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsAccountChargeData> charges,
            final Collection<ChargeData> chargeOptions) {

        return new SavingsAccountData(account.id, account.accountNo, account.depositType, account.externalId, account.groupId,
                account.groupName, account.clientId, account.clientName, account.savingsProductId, account.savingsProductName,
                account.fieldOfficerId, account.fieldOfficerName, account.status, account.subStatus, account.timeline,
                account.currency, account.nominalAnnualInterestRate, account.interestCompoundingPeriodType,
                account.interestPostingPeriodType, account.interestCalculationType, account.interestCalculationDaysInYearType,
                account.minRequiredOpeningBalance, account.lockinPeriodFrequency, account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers,
                account.summary, transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, account.allowOverdraft,
                account.overdraftLimit, account.minRequiredBalance, account.enforceMinRequiredBalance,
                account.minBalanceForInterestCalculation, account.onHoldFunds, account.nominalAnnualInterestRateOverdraft,
                account.minOverdraftForInterestCalculation, account.withHoldTax, account.taxGroup, account.lastActiveTransactionDate, 
                account.isDormancyTrackingActive, account.daysToInactive, account.daysToDormancy, account.daysToEscheat, account.savingsAmountOnHold);
    }

    public static SavingsAccountData withClientTemplate(final Long clientId, final String clientName, final Long groupId,
            final String groupName) {

        final Long id = null;
        final String accountNo = null;
        final EnumOptionData depositType = null;
        final String externalId = null;
        final Long productId = null;
        final String productName = null;
        final Long fieldOfficerId = null;
        final String fieldOfficerName = null;
        final SavingsAccountStatusEnumData status = null;
        final SavingsAccountApplicationTimelineData timeline = null;
        final CurrencyData currency = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final EnumOptionData interestPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        // final BigDecimal withdrawalFeeAmount = null;
        // final EnumOptionData withdrawalFeeType = null;
        final boolean withdrawalFeeForTransfers = false;
        // final BigDecimal annualFeeAmount = null;
        // final MonthDay annualFeeOnMonthDay = null;
        // final LocalDate annualFeeNextDueDate = null;
        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = null;
        final BigDecimal nominalAnnualInterestRateOverdraft = null;
        final BigDecimal minOverdraftForInterestCalculation = null;
        final BigDecimal minRequiredBalance = null;
        final boolean enforceMinRequiredBalance = false;
        final BigDecimal minBalanceForInterestCalculation = null;
        final BigDecimal onHoldFunds = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;

        final SavingsAccountSummaryData summary = null;
        final Collection<SavingsAccountTransactionData> transactions = null;

        final Collection<SavingsProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;
        final SavingsAccountSubStatusEnumData subStatus = null;
        final LocalDate lastActiveTransactionDate = null;
        final boolean isDormancyTrackingActive = false;
        final Integer daysToInactive = null;
        final Integer daysToDormancy = null;
        final Integer daysToEscheat = null;
		final BigDecimal savingsAmountOnHold = null;

        return new SavingsAccountData(id, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, timeline, currency, nominalAnnualInterestRate,
                interestPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions,
                productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, minBalanceForInterestCalculation, onHoldFunds,
                nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup, 
                lastActiveTransactionDate, isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat, savingsAmountOnHold);
    }

    private SavingsAccountData(final Long id, final String accountNo, final EnumOptionData depositType, final String externalId,
            final Long groupId, final String groupName, final Long clientId, final String clientName, final Long productId,
            final String productName, final Long fieldofficerId, final String fieldofficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountSubStatusEnumData subStatus, final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency,
            final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType,
            final boolean withdrawalFeeForTransfers, final SavingsAccountSummaryData summary,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsProductData> productOptions,
            final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions, final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> withdrawalFeeTypeOptions, final Collection<SavingsAccountChargeData> charges, final Collection<ChargeData> chargeOptions,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final BigDecimal minRequiredBalance,
            final boolean enforceMinRequiredBalance, final BigDecimal minBalanceForInterestCalculation,
            final BigDecimal onHoldFunds, final BigDecimal nominalAnnualInterestRateOverdraft,
            final BigDecimal minOverdraftForInterestCalculation, final boolean withHoldTax, final TaxGroupData taxGroup, 
            final LocalDate lastActiveTransactionDate, final boolean isDormancyTrackingActive, final Integer daysToInactive, 
            final Integer daysToDormancy, final Integer daysToEscheat, final BigDecimal savingsAmountOnHold) {
        this.id = id;
        this.accountNo = accountNo;
        this.depositType = depositType;
        this.externalId = externalId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.savingsProductId = productId;
        this.savingsProductName = productName;
        this.fieldOfficerId = fieldofficerId;
        this.fieldOfficerName = fieldofficerName;
        this.status = status;
        this.subStatus = subStatus;
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
        // this.withdrawalFeeAmount = withdrawalFeeAmount;
        // this.withdrawalFeeType = withdrawalFeeType;
        this.withdrawalFeeForTransfers = withdrawalFeeForTransfers;
        // this.annualFeeAmount = annualFeeAmount;
        // this.annualFeeOnMonthDay = annualFeeOnMonthDay;
        // this.annualFeeNextDueDate = annualFeeNextDueDate;

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
        this.allowOverdraft = allowOverdraft;
        this.overdraftLimit = overdraftLimit;
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
        this.minOverdraftForInterestCalculation = minOverdraftForInterestCalculation;
        this.minRequiredBalance = minRequiredBalance;
        this.enforceMinRequiredBalance = enforceMinRequiredBalance;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
        this.onHoldFunds = onHoldFunds;
        this.withHoldTax = withHoldTax;
        this.taxGroup = taxGroup;
        this.lastActiveTransactionDate = lastActiveTransactionDate;
        this.isDormancyTrackingActive = isDormancyTrackingActive;
        this.daysToInactive = daysToInactive;
        this.daysToDormancy = daysToDormancy;
        this.daysToEscheat = daysToEscheat;
        this.savingsAmountOnHold = savingsAmountOnHold;
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
        return this.savingsProductId;
    }

    public CurrencyData currency() {
        return this.currency;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final SavingsAccountData rhs = (SavingsAccountData) obj;
        return new EqualsBuilder().append(this.id, rhs.id).append(this.accountNo, rhs.accountNo).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.id).append(this.accountNo).toHashCode();
    }

    public Collection<SavingsAccountChargeData> charges() {
        return (this.charges == null) ? new HashSet<SavingsAccountChargeData>() : this.charges;
    }

    public void setDatatables(final List<DatatableData> datatables) {
        this.datatables = datatables;
    }
}