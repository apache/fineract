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
import java.time.LocalDate;
import java.util.Collection;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;

/**
 * Immutable data object representing a Fixed Deposit account.
 */
@Getter
public final class FixedDepositAccountData extends DepositAccountData {

    private boolean preClosurePenalApplicable;
    private BigDecimal preClosurePenalInterest;
    private EnumOptionData preClosurePenalInterestOnType;
    private Integer minDepositTerm;
    private Integer maxDepositTerm;
    private EnumOptionData minDepositTermType;
    private EnumOptionData maxDepositTermType;
    private Integer inMultiplesOfDepositTerm;
    private EnumOptionData inMultiplesOfDepositTermType;
    private BigDecimal depositAmount;
    private BigDecimal maturityAmount;
    private LocalDate maturityDate;
    private Integer depositPeriod;
    private EnumOptionData depositPeriodFrequency;
    private BigDecimal activationCharge;
    private Long transferToSavingsId;

    // used for account close
    private EnumOptionData onAccountClosure;

    private final PortfolioAccountData linkedAccount;
    private final Boolean transferInterestToSavings;
    private final PortfolioAccountData transferToSavingsAccount;

    private Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions;
    private Collection<EnumOptionData> periodFrequencyTypeOptions;
    private Collection<SavingsAccountData> savingsAccounts;

    // for account close
    private Collection<EnumOptionData> onAccountClosureOptions;
    private Collection<PaymentTypeData> paymentTypeOptions;
    private final Collection<EnumOptionData> maturityInstructionOptions;

    // import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private LocalDate submittedOnDate;
    private Long depositPeriodFrequencyId;

    public static FixedDepositAccountData importInstance(Long clientId, Long productId, Long fieldOfficerId, LocalDate submittedOnDate,
            EnumOptionData interestCompoundingPeriodTypeEnum, EnumOptionData interestPostingPeriodTypeEnum,
            EnumOptionData interestCalculationTypeEnum, EnumOptionData interestCalculationDaysInYearTypeEnum, Integer lockinPeriodFrequency,
            EnumOptionData lockinPeriodFrequencyTypeEnum, BigDecimal depositAmount, Integer depositPeriod, Long depositPeriodFrequencyId,
            String externalId, Collection<SavingsAccountChargeData> charges, Integer rowIndex, String locale, String dateFormat) {

        return new FixedDepositAccountData(clientId, productId, fieldOfficerId, submittedOnDate, interestCompoundingPeriodTypeEnum,
                interestPostingPeriodTypeEnum, interestCalculationTypeEnum, interestCalculationDaysInYearTypeEnum, lockinPeriodFrequency,
                lockinPeriodFrequencyTypeEnum, depositAmount, depositPeriod, depositPeriodFrequencyId, externalId, charges, rowIndex,
                locale, dateFormat);
    }

    private FixedDepositAccountData(Long clientId, Long productId, Long fieldofficerId, LocalDate submittedOnDate,
            EnumOptionData interestCompoundingPeriodType, EnumOptionData interestPostingPeriodType, EnumOptionData interestCalculationType,
            EnumOptionData interestCalculationDaysInYearType, Integer lockinPeriodFrequency, EnumOptionData lockinPeriodFrequencyType,
            BigDecimal depositAmount, Integer depositPeriod, Long depositPeriodFrequencyId, String externalId,
            Collection<SavingsAccountChargeData> charges, Integer rowIndex, String locale, String dateFormat) {
        super(clientId, productId, fieldofficerId, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, lockinPeriodFrequency, lockinPeriodFrequencyType, externalId, charges);
        this.preClosurePenalApplicable = false;
        this.preClosurePenalInterest = null;
        this.preClosurePenalInterestOnType = null;
        this.minDepositTerm = null;
        this.maxDepositTerm = null;
        this.minDepositTermType = null;
        this.maxDepositTermType = null;
        this.inMultiplesOfDepositTerm = null;
        this.inMultiplesOfDepositTermType = null;
        this.depositAmount = depositAmount;
        this.maturityAmount = null;
        this.maturityDate = null;
        this.depositPeriod = depositPeriod;
        this.depositPeriodFrequency = null;
        this.activationCharge = null;
        this.onAccountClosure = null;
        this.linkedAccount = null;
        this.transferToSavingsAccount = null;
        this.transferInterestToSavings = null;
        this.preClosurePenalInterestOnTypeOptions = null;
        this.periodFrequencyTypeOptions = null;
        this.savingsAccounts = null;
        this.onAccountClosureOptions = null;
        this.paymentTypeOptions = null;
        this.rowIndex = rowIndex;
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.submittedOnDate = submittedOnDate;
        this.depositPeriodFrequencyId = depositPeriodFrequencyId;
        this.maturityInstructionOptions = null;
    }

    public static FixedDepositAccountData instance(final DepositAccountData depositAccountData, final boolean preClosurePenalApplicable,
            final BigDecimal preClosurePenalInterest, final EnumOptionData preClosurePenalInterestOnType, final Integer minDepositTerm,
            final Integer maxDepositTerm, final EnumOptionData minDepositTermType, final EnumOptionData maxDepositTermType,
            final Integer inMultiplesOfDepositTerm, final EnumOptionData inMultiplesOfDepositTermType, final BigDecimal depositAmount,
            final BigDecimal maturityAmount, final LocalDate maturityDate, final Integer depositPeriod,
            final EnumOptionData depositPeriodFrequency, final EnumOptionData onAccountClosure, final Boolean transferInterestToSavings,
            final Long transferToSavingsId) {

        final PortfolioAccountData linkedAccount = null;
        final PortfolioAccountData transferToSavingsAccount = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> maturityInstructionOptions = null;

        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.FIXED_DEPOSIT.getValue());
        final Collection<EnumOptionData> onAccountClosureOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        final Collection<SavingsAccountData> savingsAccountDatas = null;

        return new FixedDepositAccountData(depositAccountData.id, depositAccountData.accountNo, depositAccountData.externalId,
                depositAccountData.groupId, depositAccountData.groupName, depositAccountData.clientId, depositAccountData.clientName,
                depositAccountData.depositProductId, depositAccountData.depositProductName, depositAccountData.fieldOfficerId,
                depositAccountData.fieldOfficerName, depositAccountData.status, depositAccountData.timeline, depositAccountData.currency,
                depositAccountData.nominalAnnualInterestRate, depositAccountData.interestCompoundingPeriodType,
                depositAccountData.interestPostingPeriodType, depositAccountData.interestCalculationType,
                depositAccountData.interestCalculationDaysInYearType, depositAccountData.minRequiredOpeningBalance,
                depositAccountData.lockinPeriodFrequency, depositAccountData.lockinPeriodFrequencyType,
                depositAccountData.withdrawalFeeForTransfers, depositAccountData.minBalanceForInterestCalculation,
                depositAccountData.summary, depositAccountData.transactions, depositAccountData.productOptions,
                depositAccountData.fieldOfficerOptions, depositAccountData.interestCompoundingPeriodTypeOptions,
                depositAccountData.interestPostingPeriodTypeOptions, depositAccountData.interestCalculationTypeOptions,
                depositAccountData.interestCalculationDaysInYearTypeOptions, depositAccountData.lockinPeriodFrequencyTypeOptions,
                depositAccountData.withdrawalFeeTypeOptions, depositAccountData.charges, depositAccountData.chargeOptions,
                depositAccountData.accountChart, depositAccountData.chartTemplate, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate,
                depositPeriod, depositPeriodFrequency, periodFrequencyTypeOptions, depositType, onAccountClosure, onAccountClosureOptions,
                paymentTypeOptions, savingsAccountDatas, linkedAccount, transferInterestToSavings, depositAccountData.withHoldTax,
                depositAccountData.taxGroup, maturityInstructionOptions, transferToSavingsId, transferToSavingsAccount);
    }

    public static FixedDepositAccountData withInterestChart(final FixedDepositAccountData account,
            final DepositAccountInterestRateChartData accountChart) {
        return new FixedDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.depositProductId, account.depositProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.minBalanceForInterestCalculation,
                account.summary, account.transactions, account.productOptions, account.fieldOfficerOptions,
                account.interestCompoundingPeriodTypeOptions, account.interestPostingPeriodTypeOptions,
                account.interestCalculationTypeOptions, account.interestCalculationDaysInYearTypeOptions,
                account.lockinPeriodFrequencyTypeOptions, account.withdrawalFeeTypeOptions, account.charges, account.chargeOptions,
                accountChart, account.chartTemplate, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, account.preClosurePenalInterestOnTypeOptions, account.minDepositTerm,
                account.maxDepositTerm, account.minDepositTermType, account.maxDepositTermType, account.inMultiplesOfDepositTerm,
                account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount, account.maturityDate,
                account.depositPeriod, account.depositPeriodFrequency, account.periodFrequencyTypeOptions, account.depositType,
                account.onAccountClosure, account.onAccountClosureOptions, account.paymentTypeOptions, account.savingsAccounts,
                account.linkedAccount, account.transferInterestToSavings, account.withHoldTax, account.taxGroup,
                account.maturityInstructionOptions, account.transferToSavingsId, account.transferToSavingsAccount);
    }

    public static FixedDepositAccountData associationsAndTemplate(final FixedDepositAccountData account, FixedDepositAccountData template,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsAccountChargeData> charges,
            final PortfolioAccountData linkedAccount, PortfolioAccountData transferToSavingsAccount) {

        if (template == null) {
            template = account;
        }

        return new FixedDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.depositProductId, account.depositProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.minBalanceForInterestCalculation,
                account.summary, transactions, template.productOptions, template.fieldOfficerOptions,
                template.interestCompoundingPeriodTypeOptions, template.interestPostingPeriodTypeOptions,
                template.interestCalculationTypeOptions, template.interestCalculationDaysInYearTypeOptions,
                template.lockinPeriodFrequencyTypeOptions, template.withdrawalFeeTypeOptions, charges, template.chargeOptions,
                account.accountChart, account.chartTemplate, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, template.preClosurePenalInterestOnTypeOptions, account.minDepositTerm,
                account.maxDepositTerm, account.minDepositTermType, account.maxDepositTermType, account.inMultiplesOfDepositTerm,
                account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount, account.maturityDate,
                account.depositPeriod, account.depositPeriodFrequency, template.periodFrequencyTypeOptions, account.depositType,
                account.onAccountClosure, account.onAccountClosureOptions, account.paymentTypeOptions, template.savingsAccounts,
                linkedAccount, account.transferInterestToSavings, account.withHoldTax, account.taxGroup, account.maturityInstructionOptions,
                account.transferToSavingsId, transferToSavingsAccount);
    }

    public static FixedDepositAccountData withTemplateOptions(final FixedDepositAccountData account,
            final Collection<DepositProductData> productOptions, final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsAccountChargeData> charges,
            final Collection<ChargeData> chargeOptions, final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions,
            final Collection<EnumOptionData> periodFrequencyTypeOptions, final Collection<SavingsAccountData> savingsAccounts,
            final Collection<EnumOptionData> maturityInstructionOptions) {

        return new FixedDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.depositProductId, account.depositProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.minBalanceForInterestCalculation,
                account.summary, transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, account.accountChart,
                account.chartTemplate, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, account.minDepositTerm, account.maxDepositTerm,
                account.minDepositTermType, account.maxDepositTermType, account.inMultiplesOfDepositTerm,
                account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount, account.maturityDate,
                account.depositPeriod, account.depositPeriodFrequency, periodFrequencyTypeOptions, account.depositType,
                account.onAccountClosure, account.onAccountClosureOptions, account.paymentTypeOptions, savingsAccounts,
                account.linkedAccount, account.transferInterestToSavings, account.withHoldTax, account.taxGroup, maturityInstructionOptions,
                account.transferToSavingsId, account.transferToSavingsAccount);
    }

    public static FixedDepositAccountData withClientTemplate(final Long clientId, final String clientName, final Long groupId,
            final String groupName) {

        final Long id = null;
        final String accountNo = null;
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
        final boolean withdrawalFeeForTransfers = false;
        final BigDecimal minBalanceForInterestCalculation = null;
        final SavingsAccountSummaryData summary = null;
        final Collection<SavingsAccountTransactionData> transactions = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;

        final Collection<DepositProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;

        final DepositAccountInterestRateChartData accountChart = null;
        final DepositAccountInterestRateChartData chartTemplate = null;
        final boolean preClosurePenalApplicable = false;
        final BigDecimal preClosurePenalInterest = null;
        final EnumOptionData preClosurePenalInterestOnType = null;
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final BigDecimal depositAmount = null;
        final BigDecimal maturityAmount = null;
        final LocalDate maturityDate = null;
        final Integer depositPeriod = null;
        final EnumOptionData depositPeriodFrequency = null;
        final EnumOptionData onAccountClosure = null;
        final PortfolioAccountData linkedAccount = null;
        final PortfolioAccountData transferToSavingsAccount = null;
        final Boolean transferInterestToSavings = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;

        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.FIXED_DEPOSIT.getValue());
        final Collection<EnumOptionData> onAccountClosureOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        final Collection<SavingsAccountData> savingsAccountDatas = null;
        final Collection<EnumOptionData> maturityInstructionOptions = null;
        final Long transferToSavingsId = null;

        return new FixedDepositAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, minBalanceForInterestCalculation, summary,
                transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, preClosurePenalApplicable,
                preClosurePenalInterest, preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm,
                maxDepositTerm, minDepositTermType, maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType,
                depositAmount, maturityAmount, maturityDate, depositPeriod, depositPeriodFrequency, periodFrequencyTypeOptions, depositType,
                onAccountClosure, onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas, linkedAccount,
                transferInterestToSavings, withHoldTax, taxGroup, maturityInstructionOptions, transferToSavingsId,
                transferToSavingsAccount);
    }

    public static FixedDepositAccountData preClosureDetails(final Long accountId, BigDecimal maturityAmount,
            final Collection<EnumOptionData> onAccountClosureOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<SavingsAccountData> savingsAccountDatas) {

        final Long groupId = null;
        final String groupName = null;
        final Long clientId = null;
        final String clientName = null;
        final String accountNo = null;
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
        final boolean withdrawalFeeForTransfers = false;
        final BigDecimal minBalanceForInterestCalculation = null;
        final SavingsAccountSummaryData summary = null;
        final Collection<SavingsAccountTransactionData> transactions = null;
        final Collection<DepositProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;

        final DepositAccountInterestRateChartData accountChart = null;
        final DepositAccountInterestRateChartData chartTemplate = null;
        final boolean preClosurePenalApplicable = false;
        final BigDecimal preClosurePenalInterest = null;
        final EnumOptionData preClosurePenalInterestOnType = null;
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final BigDecimal depositAmount = null;
        final LocalDate maturityDate = null;
        final Integer depositPeriod = null;
        final EnumOptionData depositPeriodFrequency = null;
        final EnumOptionData onAccountClosure = null;
        final Boolean transferInterestToSavings = null;

        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;

        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.FIXED_DEPOSIT.getValue());
        final PortfolioAccountData linkedAccount = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;
        final Collection<EnumOptionData> maturityInstructionOptions = null;
        final Long transferToSavingsId = null;
        final PortfolioAccountData transferToSavingsAccount = null;

        return new FixedDepositAccountData(accountId, accountNo, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, minBalanceForInterestCalculation, summary,
                transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, preClosurePenalApplicable,
                preClosurePenalInterest, preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm,
                maxDepositTerm, minDepositTermType, maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType,
                depositAmount, maturityAmount, maturityDate, depositPeriod, depositPeriodFrequency, periodFrequencyTypeOptions, depositType,
                onAccountClosure, onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas, linkedAccount,
                transferInterestToSavings, withHoldTax, taxGroup, maturityInstructionOptions, transferToSavingsId,
                transferToSavingsAccount);
    }

    public static FixedDepositAccountData withClosureTemplateDetails(final FixedDepositAccountData account,
            final Collection<EnumOptionData> onAccountClosureOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<SavingsAccountData> savingsAccountDatas) {

        return new FixedDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.depositProductId, account.depositProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.minBalanceForInterestCalculation,
                account.summary, account.transactions, account.productOptions, account.fieldOfficerOptions,
                account.interestCompoundingPeriodTypeOptions, account.interestPostingPeriodTypeOptions,
                account.interestCalculationTypeOptions, account.interestCalculationDaysInYearTypeOptions,
                account.lockinPeriodFrequencyTypeOptions, account.withdrawalFeeTypeOptions, account.charges, account.chargeOptions,
                account.accountChart, account.chartTemplate, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, account.preClosurePenalInterestOnTypeOptions, account.minDepositTerm,
                account.maxDepositTerm, account.minDepositTermType, account.maxDepositTermType, account.inMultiplesOfDepositTerm,
                account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount, account.maturityDate,
                account.depositPeriod, account.depositPeriodFrequency, account.periodFrequencyTypeOptions, account.depositType,
                account.onAccountClosure, onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas, account.linkedAccount,
                account.transferInterestToSavings, account.withHoldTax, account.taxGroup, account.maturityInstructionOptions,
                account.transferToSavingsId, account.transferToSavingsAccount);

    }

    private FixedDepositAccountData(final Long id, final String accountNo, final String externalId, final Long groupId,
            final String groupName, final Long clientId, final String clientName, final Long productId, final String productName,
            final Long fieldofficerId, final String fieldofficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal nominalAnnualInterestRate,
            final EnumOptionData interestPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType,
            final boolean withdrawalFeeForTransfers, final BigDecimal minBalanceForInterestCalculation,
            final SavingsAccountSummaryData summary, final Collection<SavingsAccountTransactionData> transactions,
            final Collection<DepositProductData> productOptions, final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountChargeData> charges, final Collection<ChargeData> chargeOptions,
            final DepositAccountInterestRateChartData accountChart, final DepositAccountInterestRateChartData chartTemplate,
            final boolean preClosurePenalApplicable, final BigDecimal preClosurePenalInterest,
            final EnumOptionData preClosurePenalInterestOnType, final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions,
            final Integer minDepositTerm, final Integer maxDepositTerm, final EnumOptionData minDepositTermType,
            final EnumOptionData maxDepositTermType, final Integer inMultiplesOfDepositTerm,
            final EnumOptionData inMultiplesOfDepositTermType, final BigDecimal depositAmount, final BigDecimal maturityAmount,
            final LocalDate maturityDate, final Integer depositPeriod, final EnumOptionData depositPeriodFrequency,
            final Collection<EnumOptionData> periodFrequencyTypeOptions, final EnumOptionData depositType,
            final EnumOptionData onAccountClosure, final Collection<EnumOptionData> onAccountClosureOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<SavingsAccountData> savingsAccountDatas,
            final PortfolioAccountData linkedAccount, final Boolean transferInterestToSavings, final boolean withHoldTax,
            final TaxGroupData taxGroup, final Collection<EnumOptionData> maturityInstructionOptions, final Long transferToSavingsId,
            final PortfolioAccountData transferToSavingsAccount) {

        super(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName, fieldofficerId, fieldofficerName,
                status, timeline, currency, nominalAnnualInterestRate, interestPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions, productOptions, fieldOfficerOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges,
                chargeOptions, accountChart, chartTemplate, depositType, minBalanceForInterestCalculation, withHoldTax, taxGroup);

        this.preClosurePenalApplicable = preClosurePenalApplicable;
        this.preClosurePenalInterest = preClosurePenalInterest;
        this.preClosurePenalInterestOnType = preClosurePenalInterestOnType;
        this.minDepositTerm = minDepositTerm;
        this.maxDepositTerm = maxDepositTerm;
        this.minDepositTermType = minDepositTermType;
        this.maxDepositTermType = maxDepositTermType;
        this.inMultiplesOfDepositTerm = inMultiplesOfDepositTerm;
        this.inMultiplesOfDepositTermType = inMultiplesOfDepositTermType;
        this.depositAmount = depositAmount;
        this.maturityAmount = maturityAmount;
        this.maturityDate = maturityDate;
        this.depositPeriod = depositPeriod;
        this.depositPeriodFrequency = depositPeriodFrequency;
        this.onAccountClosure = onAccountClosure;
        this.linkedAccount = linkedAccount;
        this.transferToSavingsAccount = transferToSavingsAccount;
        this.transferInterestToSavings = transferInterestToSavings;

        // template
        this.preClosurePenalInterestOnTypeOptions = preClosurePenalInterestOnTypeOptions;
        this.periodFrequencyTypeOptions = periodFrequencyTypeOptions;

        // account close template options
        this.onAccountClosureOptions = onAccountClosureOptions;
        this.paymentTypeOptions = paymentTypeOptions;
        this.savingsAccounts = savingsAccountDatas;
        this.maturityInstructionOptions = maturityInstructionOptions;
        this.transferToSavingsId = transferToSavingsId;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FixedDepositAccountData)) {
            return false;
        }
        final FixedDepositAccountData rhs = (FixedDepositAccountData) obj;
        return new EqualsBuilder().append(this.id, rhs.id).append(this.accountNo, rhs.accountNo).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.id).append(this.accountNo).toHashCode();
    }

    public void setActivationCharge(BigDecimal activationCharge) {
        this.activationCharge = activationCharge;
    }
}
