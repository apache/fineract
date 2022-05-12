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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;

/**
 * Immutable data object representing a savings account.
 */
public final class SavingsAccountData implements Serializable {

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
    private final String reasonForBlock;
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
    private final BigDecimal maxAllowedLienLimit;
    private final boolean lienAllowed;
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
    private transient List<SavingsAccountTransactionData> savingsAccountTransactionData = new ArrayList<>();

    private List<DatatableData> datatables = null;

    // import field
    private Long productId;
    private String locale;
    private String dateFormat;
    private transient Integer rowIndex;
    private transient Date startInterestCalculationDate;
    private LocalDate submittedOnDate;
    private transient SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    private transient SavingsHelper savingsHelper;

    private transient SavingsAccountSummaryData savingsAccountSummaryData;
    private transient Date activatedOnDate;
    private transient LocalDate lockedInUntilDate;
    private transient ClientData clientData;
    private transient SavingsProductData savingsProductData;
    private transient List<SavingsAccountTransactionData> newSavingsAccountTransactionData = new ArrayList<>();
    private transient GroupGeneralData groupGeneralData;
    private transient Long officeId;
    private transient Set<Long> existingTransactionIds = new HashSet<>();
    private transient Set<Long> existingReversedTransactionIds = new HashSet<>();
    private transient Long glAccountIdForSavingsControl;
    private transient Long glAccountIdForInterestOnSavings;

    public static SavingsAccountData importInstanceIndividual(Long clientId, Long productId, Long fieldOfficerId, LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodTypeEnum,
            EnumOptionData interestPostingPeriodTypeEnum, EnumOptionData interestCalculationTypeEnum,
            EnumOptionData interestCalculationDaysInYearTypeEnum, BigDecimal minRequiredOpeningBalance, Integer lockinPeriodFrequency,
            EnumOptionData lockinPeriodFrequencyTypeEnum, boolean applyWithdrawalFeeForTransfers, Integer rowIndex, String externalId,
            Collection<SavingsAccountChargeData> charges, boolean allowOverdraft, BigDecimal overdraftLimit, String locale,
            String dateFormat) {
        return new SavingsAccountData(clientId, productId, fieldOfficerId, submittedOnDate, nominalAnnualInterestRate,
                interestCompoundingPeriodTypeEnum, interestPostingPeriodTypeEnum, interestCalculationTypeEnum,
                interestCalculationDaysInYearTypeEnum, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyTypeEnum,
                applyWithdrawalFeeForTransfers, rowIndex, externalId, charges, allowOverdraft, overdraftLimit, locale, dateFormat);

    }

    private SavingsAccountData(Long clientId, Long productId, Long fieldOfficerId, LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodType, EnumOptionData interestPostingPeriodType,
            EnumOptionData interestCalculationType, EnumOptionData interestCalculationDaysInYearType, BigDecimal minRequiredOpeningBalance,
            Integer lockinPeriodFrequency, EnumOptionData lockinPeriodFrequencyType, boolean withdrawalFeeForTransfers, Integer rowIndex,
            String externalId, Collection<SavingsAccountChargeData> charges, boolean allowOverdraft, BigDecimal overdraftLimit,
            String locale, String dateFormat) {
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
        this.reasonForBlock = null;
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
        this.maxAllowedLienLimit = null;
        this.lienAllowed = false;
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
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.rowIndex = rowIndex;
        this.submittedOnDate = submittedOnDate;
        this.savingsAmountOnHold = null;
    }

    public static final Comparator<SavingsAccountData> ClientNameComparator = new Comparator<SavingsAccountData>() {

        @Override
        public int compare(SavingsAccountData savings1, SavingsAccountData savings2) {
            String clientOfSavings1 = savings1.getClientName().toUpperCase(Locale.ENGLISH);
            String clientOfSavings2 = savings2.getClientName().toUpperCase(Locale.ENGLISH);
            return clientOfSavings1.compareTo(clientOfSavings2);
        }
    };

    public void setNewSavingsAccountTransactionData(final SavingsAccountTransactionData savingsAccountTransactionData) {
        this.newSavingsAccountTransactionData.add(savingsAccountTransactionData);
    }

    public List<SavingsAccountTransactionData> getNewSavingsAccountTransactionData() {
        return this.newSavingsAccountTransactionData;
    }

    public void setSavingsAccountSummaryData(final SavingsAccountSummaryData savingsAccountSummaryData) {
        this.savingsAccountSummaryData = savingsAccountSummaryData;
    }

    public void setSavingsProduct(final SavingsProductData savingsProductData) {
        this.savingsProductData = savingsProductData;
    }

    public void setSavingsAccountTransactionData(final SavingsAccountTransactionData savingsAccountTransactionData) {
        this.savingsAccountTransactionData.add(savingsAccountTransactionData);
    }

    public void setSubmittedOnDate(final LocalDate submittedOnDate) {
        this.submittedOnDate = submittedOnDate;
    }

    public void setLockedInUntilDate(final LocalDate lockedInUntilDate) {
        this.lockedInUntilDate = lockedInUntilDate;
    }

    public void setStartInterestCalculationDate(final Date startInterestCalculationDate) {
        this.startInterestCalculationDate = startInterestCalculationDate;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public void setClientData(final ClientData clientData) {
        this.clientData = clientData;
    }

    public void setGroupGeneralData(final GroupGeneralData groupGeneralData) {
        this.groupGeneralData = groupGeneralData;
    }

    public void setUpdatedTransactions(List<SavingsAccountTransactionData> savingsAccountTransactionDataList) {
        this.savingsAccountTransactionData = new ArrayList<>();
        this.savingsAccountTransactionData.addAll(savingsAccountTransactionDataList);
    }

    public void setOfficeId(final Long officeId) {
        this.officeId = officeId;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public BigDecimal getNominalAnnualInterestRate() {
        return this.nominalAnnualInterestRate;
    }

    public BigDecimal getNominalAnnualInterestRateOverdraft() {
        return this.nominalAnnualInterestRateOverdraft;
    }

    public boolean isAllowOverdraft() {
        return this.allowOverdraft;
    }

    public TaxGroupData getTaxGroupData() {
        return this.taxGroup;
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

    public Long getId() {
        return this.id;
    }

    public void updateTransactions(final SavingsAccountTransactionData savingsAccountTransactionData) {
        this.savingsAccountTransactionData.add(savingsAccountTransactionData);
    }

    public boolean withHoldTax() {
        return this.withHoldTax;
    }

    public DepositAccountType depositAccountType() {
        return DepositAccountType.fromInt(this.depositType.getId().intValue());
    }

    public void setGlAccountIdForSavingsControl(final Long glAccountIdForSavingsControl) {
        this.glAccountIdForSavingsControl = glAccountIdForSavingsControl;
    }

    public void setGlAccountIdForInterestOnSavings(final Long glAccountIdForInterestOnSavings) {
        this.glAccountIdForInterestOnSavings = glAccountIdForInterestOnSavings;
    }

    public Long getGlAccountIdForSavingsControl() {
        return this.glAccountIdForSavingsControl;
    }

    public Long getGlAccountIdForInterestOnSavings() {
        return this.glAccountIdForInterestOnSavings;
    }

    public SavingsAccountSummaryData getSavingsAccountSummaryData() {
        return this.savingsAccountSummaryData;
    }

    public List<SavingsAccountTransactionData> getSavingsAccountTransactionData() {
        return this.savingsAccountTransactionData;
    }

    public void setHelpers(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final SavingsHelper savingsHelper) {
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.savingsHelper = savingsHelper;
    }

    public BigDecimal getMinBalanceForInterestCalculation() {
        return this.minBalanceForInterestCalculation;
    }

    public BigDecimal getMinOverdraftForInterestCalculation() {
        return this.minOverdraftForInterestCalculation;
    }

    public Integer getInterestPostingPeriodType() {
        return this.interestPostingPeriodType.getId().intValue();
    }

    public Integer getDepositType() {
        return this.depositType.getId().intValue();
    }

    public Integer getInterestCompoundingPeriodType() {
        return this.interestCompoundingPeriodType.getId().intValue();
    }

    public Integer getInterestCalculationType() {
        return this.interestCalculationType.getId().intValue();
    }

    public Integer getInterestCalculationDaysInYearType() {
        return this.interestCalculationDaysInYearType.getId().intValue();
    }

    public LocalDate getLockedInUntilDate() {
        return this.lockedInUntilDate;
    }

    public SavingsAccountTransactionData findLastTransaction(final LocalDate date) {

        SavingsAccountTransactionData savingsTransaction = null;
        List<SavingsAccountTransactionData> trans = getTransactions();
        for (final SavingsAccountTransactionData transaction : trans) {
            if (transaction.isNotReversed() && transaction.occursOn(date)) {
                savingsTransaction = transaction;
                break;
            }
        }

        return savingsTransaction;
    }

    public List<SavingsAccountTransactionData> getTransactions() {
        return this.savingsAccountTransactionData;
    }

    public LocalDate getStartInterestCalculationDate() {
        LocalDate startInterestCalculationLocalDate = null;
        if (this.startInterestCalculationDate != null) {
            startInterestCalculationLocalDate = LocalDate.ofInstant(this.startInterestCalculationDate.toInstant(),
                    DateUtils.getDateTimeZoneOfTenant());
        } else {
            startInterestCalculationLocalDate = getActivationLocalDate();
        }
        return startInterestCalculationLocalDate;
    }

    public LocalDate getActivationLocalDate() {
        LocalDate activationLocalDate = null;
        if (this.timeline.getActivatedOnDate() != null) {
            activationLocalDate = this.timeline.getActivatedOnDate();
        }
        return activationLocalDate;
    }

    public Integer getLockinPeriodFrequencyType() {
        return this.lockinPeriodFrequencyType.getId().intValue();
    }

    public Collection<Long> findCurrentTransactionIdsWithPivotDateConfig() {

        final Collection<Long> ids = new ArrayList<>();
        List<SavingsAccountTransactionData> trans = this.savingsAccountTransactionData;
        for (final SavingsAccountTransactionData transaction : trans) {
            ids.add(transaction.getId());
        }
        return ids;
    }

    public Collection<Long> findCurrentReversedTransactionIdsWithPivotDateConfig() {
        final Collection<Long> ids = new ArrayList<>();
        List<SavingsAccountTransactionData> trans = this.savingsAccountTransactionData;
        // time consuming
        for (final SavingsAccountTransactionData transaction : trans) {
            if (transaction.isReversed()) {
                ids.add(transaction.getId());
            }
        }
        return ids;
    }

    public Long officeId() {
        Long officeId = null;
        if (this.clientData != null) {
            officeId = this.clientData.officeId();
        } else if (this.groupId != null) {
            officeId = this.groupGeneralData.officeId();
        }
        return officeId;
    }

    public List<SavingsAccountTransactionData> getSavingsAccountTransactionsWithPivotConfig() {
        return this.transactions.stream().collect(Collectors.toList());
    }

    public Boolean isAccrualBasedAccountingEnabledOnSavingsProduct() {
        return this.savingsProductData.isAccrualBasedAccountingEnabled();
    }

    public Boolean isCashBasedAccountingEnabledOnSavingsProduct() {
        return this.savingsProductData.isCashBasedAccountingEnabled();
    }

    public static SavingsAccountData importInstanceGroup(Long groupId, Long productId, Long fieldOfficerId, LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodTypeEnum,
            EnumOptionData interestPostingPeriodTypeEnum, EnumOptionData interestCalculationTypeEnum,
            EnumOptionData interestCalculationDaysInYearTypeEnum, BigDecimal minRequiredOpeningBalance, Integer lockinPeriodFrequency,
            EnumOptionData lockinPeriodFrequencyTypeEnum, boolean applyWithdrawalFeeForTransfers, Integer rowIndex, String externalId,
            Collection<SavingsAccountChargeData> charges, boolean allowOverdraft, BigDecimal overdraftLimit, String locale,
            String dateFormat) {

        return new SavingsAccountData(groupId, productId, fieldOfficerId, submittedOnDate, nominalAnnualInterestRate,
                interestCompoundingPeriodTypeEnum, interestPostingPeriodTypeEnum, interestCalculationTypeEnum,
                interestCalculationDaysInYearTypeEnum, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyTypeEnum,
                applyWithdrawalFeeForTransfers, rowIndex, externalId, charges, allowOverdraft, overdraftLimit, null, locale, dateFormat);

    }

    private SavingsAccountData(Long groupId, Long productId, Long fieldOfficerId, LocalDate submittedOnDate,
            BigDecimal nominalAnnualInterestRate, EnumOptionData interestCompoundingPeriodType, EnumOptionData interestPostingPeriodType,
            EnumOptionData interestCalculationType, EnumOptionData interestCalculationDaysInYearType, BigDecimal minRequiredOpeningBalance,
            Integer lockinPeriodFrequency, EnumOptionData lockinPeriodFrequencyType, boolean withdrawalFeeForTransfers, Integer rowIndex,
            String externalId, Collection<SavingsAccountChargeData> charges, boolean allowOverdraft, BigDecimal overdraftLimit, Long id,
            String locale, String dateFormat) {
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
        this.reasonForBlock = null;
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
        this.maxAllowedLienLimit = null;
        this.lienAllowed = false;
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
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.rowIndex = rowIndex;
        this.submittedOnDate = submittedOnDate;
        this.savingsAmountOnHold = null;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public static SavingsAccountData instance(final Long id, final String accountNo, final EnumOptionData depositType,
            final String externalId, final Long groupId, final String groupName, final Long clientId, final String clientName,
            final Long productId, final String productName, final Long fieldOfficerId, final String fieldOfficerName,
            final SavingsAccountStatusEnumData status, SavingsAccountSubStatusEnumData subStatus, final String reasonForBlock,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal interestRate,
            final EnumOptionData interestCompoundingPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType,
            final boolean withdrawalFeeForTransfers, final SavingsAccountSummaryData summary, final boolean allowOverdraft,
            final BigDecimal overdraftLimit, final BigDecimal minRequiredBalance, final boolean enforceMinRequiredBalance,
            final BigDecimal maxAllowedLienLimit, final boolean lienAllowed, final BigDecimal minBalanceForInterestCalculation,
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
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, reasonForBlock, timeline, currency, interestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary,
                transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit, minRequiredBalance,
                enforceMinRequiredBalance, maxAllowedLienLimit, lienAllowed, minBalanceForInterestCalculation, onHoldFunds,
                nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup, lastActiveTransactionDate,
                isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat, savingsAmountOnHold);
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
        final BigDecimal maxAllowedLienLimit = null;
        final boolean lienAllowed = false;
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
        final String reasonForBlock = null;
        final LocalDate lastActiveTransactionDate = null;
        final boolean isDormancyTrackingActive = false;
        final Integer daysToInactive = null;
        final Integer daysToDormancy = null;
        final Integer daysToEscheat = null;
        final BigDecimal savingsAmountOnHold = null;

        return new SavingsAccountData(accountId, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, reasonForBlock, timeline, currency,
                nominalAnnualInterestRate, interestPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeForTransfers, summary, transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, maxAllowedLienLimit, lienAllowed, minBalanceForInterestCalculation,
                onHoldFunds, nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup,
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
        final BigDecimal maxAllowedLienLimit = null;
        final boolean lienAllowed = false;
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
        final String reasonForBlock = null;
        final LocalDate lastActiveTransactionDate = null;
        final boolean isDormancyTrackingActive = false;
        final Integer daysToInactive = null;
        final Integer daysToDormancy = null;
        final Integer daysToEscheat = null;
        final BigDecimal savingsAmountOnHold = null;

        return new SavingsAccountData(accountId, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, reasonForBlock, timeline, currency,
                nominalAnnualInterestRate, interestPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeForTransfers, summary, transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, maxAllowedLienLimit, lienAllowed, minBalanceForInterestCalculation,
                onHoldFunds, nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup,
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
                account.fieldOfficerId, account.fieldOfficerName, account.status, account.subStatus, account.reasonForBlock,
                account.timeline, account.currency, account.nominalAnnualInterestRate, account.interestCompoundingPeriodType,
                account.interestPostingPeriodType, account.interestCalculationType, account.interestCalculationDaysInYearType,
                account.minRequiredOpeningBalance, account.lockinPeriodFrequency, account.lockinPeriodFrequencyType,
                account.withdrawalFeeForTransfers, account.summary, transactions, template.productOptions, template.fieldOfficerOptions,
                template.interestCompoundingPeriodTypeOptions, template.interestPostingPeriodTypeOptions,
                template.interestCalculationTypeOptions, template.interestCalculationDaysInYearTypeOptions,
                template.lockinPeriodFrequencyTypeOptions, template.withdrawalFeeTypeOptions, charges, template.chargeOptions,
                account.allowOverdraft, account.overdraftLimit, account.minRequiredBalance, account.enforceMinRequiredBalance,
                account.maxAllowedLienLimit, account.lienAllowed, account.minBalanceForInterestCalculation, account.onHoldFunds,
                account.nominalAnnualInterestRateOverdraft, account.minOverdraftForInterestCalculation, account.withHoldTax,
                account.taxGroup, account.lastActiveTransactionDate, account.isDormancyTrackingActive, account.daysToInactive,
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
                account.fieldOfficerId, account.fieldOfficerName, account.status, account.subStatus, account.reasonForBlock,
                account.timeline, account.currency, account.nominalAnnualInterestRate, account.interestCompoundingPeriodType,
                account.interestPostingPeriodType, account.interestCalculationType, account.interestCalculationDaysInYearType,
                account.minRequiredOpeningBalance, account.lockinPeriodFrequency, account.lockinPeriodFrequencyType,
                account.withdrawalFeeForTransfers, account.summary, transactions, productOptions, fieldOfficerOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges,
                chargeOptions, account.allowOverdraft, account.overdraftLimit, account.minRequiredBalance,
                account.enforceMinRequiredBalance, account.maxAllowedLienLimit, account.lienAllowed,
                account.minBalanceForInterestCalculation, account.onHoldFunds, account.nominalAnnualInterestRateOverdraft,
                account.minOverdraftForInterestCalculation, account.withHoldTax, account.taxGroup, account.lastActiveTransactionDate,
                account.isDormancyTrackingActive, account.daysToInactive, account.daysToDormancy, account.daysToEscheat,
                account.savingsAmountOnHold);
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
        final BigDecimal maxAllowedLienLimit = null;
        final boolean lienAllowed = false;
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
        final String reasonForBlock = null;
        final LocalDate lastActiveTransactionDate = null;
        final boolean isDormancyTrackingActive = false;
        final Integer daysToInactive = null;
        final Integer daysToDormancy = null;
        final Integer daysToEscheat = null;
        final BigDecimal savingsAmountOnHold = null;

        return new SavingsAccountData(id, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, subStatus, reasonForBlock, timeline, currency,
                nominalAnnualInterestRate, interestPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeForTransfers, summary, transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, maxAllowedLienLimit, lienAllowed, minBalanceForInterestCalculation,
                onHoldFunds, nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup,
                lastActiveTransactionDate, isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat, savingsAmountOnHold);
    }

    private SavingsAccountData(final Long id, final String accountNo, final EnumOptionData depositType, final String externalId,
            final Long groupId, final String groupName, final Long clientId, final String clientName, final Long productId,
            final String productName, final Long fieldOfficerId, final String fieldOfficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountSubStatusEnumData subStatus, final String reasonForBlock,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal nominalAnnualInterestRate,
            final EnumOptionData interestPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType,
            final boolean withdrawalFeeForTransfers, final SavingsAccountSummaryData summary,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsProductData> productOptions,
            final Collection<StaffData> fieldOfficerOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountChargeData> charges, final Collection<ChargeData> chargeOptions, final boolean allowOverdraft,
            final BigDecimal overdraftLimit, final BigDecimal minRequiredBalance, final boolean enforceMinRequiredBalance,
            final BigDecimal maxAllowedLienLimit, final boolean lienAllowd, final BigDecimal minBalanceForInterestCalculation,
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
        this.fieldOfficerId = fieldOfficerId;
        this.fieldOfficerName = fieldOfficerName;
        this.status = status;
        this.subStatus = subStatus;
        this.reasonForBlock = reasonForBlock;
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
        this.maxAllowedLienLimit = maxAllowedLienLimit;
        this.lienAllowed = lienAllowd;
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
            if (charge.isWithdrawalFee()) {
                return charge;
            }
        }
        return null;
    }

    private SavingsAccountChargeData getAnnualFee() {
        for (SavingsAccountChargeData charge : this.charges()) {
            if (charge.isAnnualFee()) {
                return charge;
            }
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

    public SavingsAccountTransactionSummaryWrapper getSavingsAccountTransactionSummaryWrapper() {
        return this.savingsAccountTransactionSummaryWrapper;
    }

    public void setExistingTransactionIds(final Set<Long> existingTransactionIds) {
        if (existingTransactionIds != null) {
            this.existingTransactionIds.addAll(existingTransactionIds);
        }
    }

    public void setExistingReversedTransactionIds(final Set<Long> existingReversedTransactionIds) {
        if (existingReversedTransactionIds != null) {
            this.existingReversedTransactionIds.addAll(existingReversedTransactionIds);
        }
    }

    public SavingsHelper getSavingsHelper() {
        return this.savingsHelper;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SavingsAccountData)) {
            return false;
        }
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

    public SavingsAccountSummaryData getSummary() {
        return this.summary;
    }

    public Set<Long> getExistingTransactionIds() {
        return this.existingTransactionIds;
    }

    public Set<Long> getExistingReversedTransactionIds() {
        return this.existingReversedTransactionIds;
    }
}
