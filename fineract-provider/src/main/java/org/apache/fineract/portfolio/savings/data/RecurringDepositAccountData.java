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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a Recurring Deposit account.
 */
public class RecurringDepositAccountData extends DepositAccountData {

    // additional fields
    private final boolean preClosurePenalApplicable;
    private final BigDecimal preClosurePenalInterest;
    private final EnumOptionData preClosurePenalInterestOnType;
    private final Integer minDepositTerm;
    private final Integer maxDepositTerm;
    private final EnumOptionData minDepositTermType;
    private final EnumOptionData maxDepositTermType;
    private final Integer inMultiplesOfDepositTerm;
    private final EnumOptionData inMultiplesOfDepositTermType;
    private final BigDecimal depositAmount;
    private final BigDecimal maturityAmount;
    private final LocalDate maturityDate;
    private final Integer depositPeriod;
    private final EnumOptionData depositPeriodFrequency;
    private final BigDecimal mandatoryRecommendedDepositAmount;
    private final BigDecimal totalOverdueAmount;
    private final Integer noOfOverdueInstallments;
    private final boolean isMandatoryDeposit;
    private final boolean allowWithdrawal;
    private final boolean adjustAdvanceTowardsFuturePayments;
    private final LocalDate expectedFirstDepositOnDate;
    private final boolean isCalendarInherited;
    private final Integer recurringFrequency;
    private final EnumOptionData recurringFrequencyType;

    // used for account close
    private final EnumOptionData onAccountClosure;

    private final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions;
    private final Collection<EnumOptionData> periodFrequencyTypeOptions;
    private final Collection<SavingsAccountData> savingsAccounts;

    // for account close
    private final Collection<EnumOptionData> onAccountClosureOptions;
    private final Collection<PaymentTypeData> paymentTypeOptions;

    //import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private LocalDate submittedOnDate;
    private Long depositPeriodFrequencyId;

    public static RecurringDepositAccountData importInstance(Long clientId,Long productId,Long fieldOfficerId,
            LocalDate submittedOnDate,
            EnumOptionData interestCompoundingPeriodTypeEnum,EnumOptionData interestPostingPeriodTypeEnum,
            EnumOptionData interestCalculationTypeEnum,EnumOptionData interestCalculationDaysInYearTypeEnum,
            Integer lockinPeriodFrequency,EnumOptionData lockinPeriodFrequencyTypeEnum,BigDecimal depositAmount,
            Integer depositPeriod,Long depositPeriodFrequencyId,LocalDate expectedFirstDepositOnDate,
            Integer recurringFrequency,EnumOptionData recurringFrequencyTypeEnum,boolean isCalendarInherited,
            boolean isMandatoryDeposit,boolean allowWithdrawal,boolean adjustAdvanceTowardsFuturePayments,
            String externalId,Collection<SavingsAccountChargeData> charges,Integer rowIndex,String locale, String dateFormat){

        return new RecurringDepositAccountData(clientId, productId, fieldOfficerId, submittedOnDate,
                interestCompoundingPeriodTypeEnum,interestPostingPeriodTypeEnum,interestCalculationTypeEnum,
                interestCalculationDaysInYearTypeEnum, lockinPeriodFrequency, lockinPeriodFrequencyTypeEnum,
                depositAmount, depositPeriod, depositPeriodFrequencyId, expectedFirstDepositOnDate,
                recurringFrequency, recurringFrequencyTypeEnum, isCalendarInherited, isMandatoryDeposit,
                allowWithdrawal, adjustAdvanceTowardsFuturePayments, externalId,charges, rowIndex,locale,dateFormat);
    }
    private RecurringDepositAccountData(Long clientId,Long productId,Long fieldofficerId,LocalDate submittedOnDate,
            EnumOptionData interestCompoundingPeriodType,EnumOptionData interestPostingPeriodType,
            EnumOptionData interestCalculationType,EnumOptionData interestCalculationDaysInYearType,
            Integer lockinPeriodFrequency,EnumOptionData lockinPeriodFrequencyType,BigDecimal depositAmount,
            Integer depositPeriod,Long depositPeriodFrequencyId,LocalDate expectedFirstDepositOnDate,
            Integer recurringFrequency,EnumOptionData recurringFrequencyType,boolean isCalendarInherited,
            boolean isMandatoryDeposit,boolean allowWithdrawal,boolean adjustAdvanceTowardsFuturePayments,
            String externalId,Collection<SavingsAccountChargeData> charges,Integer rowIndex,String locale, String dateFormat) {
        super(clientId,productId,fieldofficerId,interestCompoundingPeriodType,interestPostingPeriodType,interestCalculationType,
                interestCalculationDaysInYearType,lockinPeriodFrequency,lockinPeriodFrequencyType, externalId,charges);

        this.preClosurePenalApplicable = false;
        this.preClosurePenalInterest = null;
        this.preClosurePenalInterestOnType = null;
        this.minDepositTerm = null;
        this.maxDepositTerm = null;
        this.minDepositTermType = null;
        this.maxDepositTermType = null;
        this.inMultiplesOfDepositTerm = null;
        this.inMultiplesOfDepositTermType = null;
        this.depositAmount = null;
        this.maturityAmount = null;
        this.maturityDate = null;
        this.depositPeriod = depositPeriod;
        this.depositPeriodFrequency = null;
        this.mandatoryRecommendedDepositAmount = depositAmount;
        this.totalOverdueAmount = null;
        this.noOfOverdueInstallments = null;
        this.isMandatoryDeposit = isMandatoryDeposit;
        this.allowWithdrawal = allowWithdrawal;
        this.adjustAdvanceTowardsFuturePayments = adjustAdvanceTowardsFuturePayments;
        this.expectedFirstDepositOnDate = expectedFirstDepositOnDate;
        this.isCalendarInherited = isCalendarInherited;
        this.recurringFrequency = recurringFrequency;
        this.recurringFrequencyType = recurringFrequencyType;
        this.onAccountClosure = null;
        this.preClosurePenalInterestOnTypeOptions = null;
        this.periodFrequencyTypeOptions = null;
        this.savingsAccounts = null;
        this.onAccountClosureOptions = null;
        this.paymentTypeOptions = null;
        this.rowIndex = rowIndex;
        this.dateFormat= dateFormat;
        this.locale=locale;
        this.submittedOnDate=submittedOnDate;
        this.depositPeriodFrequencyId=depositPeriodFrequencyId;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public static RecurringDepositAccountData instance(final DepositAccountData depositAccountData,
            final boolean preClosurePenalApplicable, final BigDecimal preClosurePenalInterest,
            final EnumOptionData preClosurePenalInterestOnType, final Integer minDepositTerm, final Integer maxDepositTerm,
            final EnumOptionData minDepositTermType, final EnumOptionData maxDepositTermType, final Integer inMultiplesOfDepositTerm,
            final EnumOptionData inMultiplesOfDepositTermType, final BigDecimal depositAmount, final BigDecimal maturityAmount,
            final LocalDate maturityDate, final Integer depositPeriod, final EnumOptionData depositPeriodFrequency,
            final BigDecimal mandatoryRecommendedDepositAmount, final EnumOptionData onAccountClosure,
            final LocalDate expectedFirstDepositOnDate, final BigDecimal totalOverdueAmount, final Integer noOfOverdueInstallments,
            final boolean isMandatoryDeposit, final boolean allowWithdrawal, final boolean adjustAdvanceTowardsFuturePayments,
            final boolean isCalendarInherited) {

        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;

        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.FIXED_DEPOSIT.getValue());
        final Collection<EnumOptionData> onAccountClosureOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        final Collection<SavingsAccountData> savingsAccountDatas = null;
        final Integer recurringFrequency = null;
        final EnumOptionData recurringFrequencyType = null;

        return new RecurringDepositAccountData(depositAccountData.id, depositAccountData.accountNo, depositAccountData.externalId,
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
                depositPeriod, depositPeriodFrequency, mandatoryRecommendedDepositAmount, periodFrequencyTypeOptions, depositType,
                onAccountClosure, onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas, expectedFirstDepositOnDate,
                totalOverdueAmount, noOfOverdueInstallments, isMandatoryDeposit, allowWithdrawal, adjustAdvanceTowardsFuturePayments,
                isCalendarInherited, recurringFrequency, recurringFrequencyType, depositAccountData.withHoldTax,
                depositAccountData.taxGroup);
    }

    public static RecurringDepositAccountData withInterestChartAndRecurringDetails(final RecurringDepositAccountData account,
            final DepositAccountInterestRateChartData accountChart, final Integer recurringFrequency,
            final EnumOptionData recurringFrequencyType) {
        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
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
                account.depositPeriod, account.depositPeriodFrequency, account.mandatoryRecommendedDepositAmount,
                account.periodFrequencyTypeOptions, account.depositType, account.onAccountClosure, account.onAccountClosureOptions,
                account.paymentTypeOptions, account.savingsAccounts, account.expectedFirstDepositOnDate, account.totalOverdueAmount,
                account.noOfOverdueInstallments, account.isMandatoryDeposit, account.allowWithdrawal,
                account.adjustAdvanceTowardsFuturePayments, account.isCalendarInherited, recurringFrequency, recurringFrequencyType,
                account.withHoldTax, account.taxGroup);
    }

    public static RecurringDepositAccountData withTemplateOptions(final RecurringDepositAccountData account,
            final RecurringDepositAccountData template, final Collection<SavingsAccountTransactionData> transactions,
            final Collection<SavingsAccountChargeData> charges) {

        if (template == null) {
            final Collection<DepositProductData> productOptions = null;
            final Collection<StaffData> fieldOfficerOptions = null;
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
            final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
            final Collection<ChargeData> chargeOptions = null;

            final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
            final Collection<EnumOptionData> periodFrequencyTypeOptions = null;

            return withTemplateOptions(account, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                    interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                    lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, transactions, charges, chargeOptions,
                    preClosurePenalInterestOnTypeOptions, periodFrequencyTypeOptions);
        }

        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
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
                account.depositPeriod, account.depositPeriodFrequency, account.mandatoryRecommendedDepositAmount,
                template.periodFrequencyTypeOptions, account.depositType, account.onAccountClosure, account.onAccountClosureOptions,
                account.paymentTypeOptions, account.savingsAccounts, account.expectedFirstDepositOnDate, account.totalOverdueAmount,
                account.noOfOverdueInstallments, account.isMandatoryDeposit, account.allowWithdrawal,
                account.adjustAdvanceTowardsFuturePayments, account.isCalendarInherited, account.recurringFrequency,
                account.recurringFrequencyType, account.withHoldTax, account.taxGroup);

    }

    public static RecurringDepositAccountData withTemplateOptions(final RecurringDepositAccountData account,
            final Collection<DepositProductData> productOptions, final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsAccountChargeData> charges,
            final Collection<ChargeData> chargeOptions, final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions,
            final Collection<EnumOptionData> periodFrequencyTypeOptions) {

        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.depositProductId, account.depositProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.minBalanceForInterestCalculation,
                account.summary, transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges, chargeOptions, account.accountChart,
                account.chartTemplate, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, account.minDepositTerm,
                account.maxDepositTerm, account.minDepositTermType, account.maxDepositTermType, account.inMultiplesOfDepositTerm,
                account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount, account.maturityDate,
                account.depositPeriod, account.depositPeriodFrequency, account.mandatoryRecommendedDepositAmount,
                periodFrequencyTypeOptions, account.depositType, account.onAccountClosure, account.onAccountClosureOptions,
                account.paymentTypeOptions, account.savingsAccounts, account.expectedFirstDepositOnDate, account.totalOverdueAmount,
                account.noOfOverdueInstallments, account.isMandatoryDeposit, account.allowWithdrawal,
                account.adjustAdvanceTowardsFuturePayments, account.isCalendarInherited, account.recurringFrequency,
                account.recurringFrequencyType, account.withHoldTax, account.taxGroup);
    }

    public static RecurringDepositAccountData withClientTemplate(final Long clientId, final String clientName, final Long groupId,
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
        final BigDecimal mandatoryRecommendedDepositAmount = null;
        final EnumOptionData onAccountClosure = null;

        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;

        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.RECURRING_DEPOSIT.getValue());
        final Collection<EnumOptionData> onAccountClosureOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        final Collection<SavingsAccountData> savingsAccountDatas = null;
        final LocalDate expectedFirstDepositOnDate = null;

        final BigDecimal totalOverdueAmount = null;
        final Integer noOfOverdueInstallments = null;
        final boolean isMandatoryDeposit = false;
        final boolean allowWithdrawal = false;
        final boolean adjustAdvanceTowardsFuturePayments = false;

        final boolean isCalendarInherited = false;
        final Integer recurringFrequency = null;
        final EnumOptionData recurringFrequencyType = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;

        return new RecurringDepositAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, minBalanceForInterestCalculation, summary,
                transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, preClosurePenalApplicable,
                preClosurePenalInterest, preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm,
                maxDepositTerm, minDepositTermType, maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType,
                depositAmount, maturityAmount, maturityDate, depositPeriod, depositPeriodFrequency, mandatoryRecommendedDepositAmount,
                periodFrequencyTypeOptions, depositType, onAccountClosure, onAccountClosureOptions, paymentTypeOptions,
                savingsAccountDatas, expectedFirstDepositOnDate, totalOverdueAmount, noOfOverdueInstallments, isMandatoryDeposit,
                allowWithdrawal, adjustAdvanceTowardsFuturePayments, isCalendarInherited, recurringFrequency, recurringFrequencyType,
                withHoldTax, taxGroup);
    }

    public static RecurringDepositAccountData preClosureDetails(final Long accountId, final BigDecimal maturityAmount,
            final Collection<EnumOptionData> onAccountClosureOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<SavingsAccountData> savingsAccountDatas) {

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
        final BigDecimal mandatoryRecommendedDepositAmount = null;
        final EnumOptionData onAccountClosure = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;
        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.RECURRING_DEPOSIT.getValue());

        final Long groupId = null;
        final String groupName = null;
        final Long clientId = null;
        final String clientName = null;
        final LocalDate expectedFirstDepositOnDate = null;

        final BigDecimal totalOverdueAmount = null;
        final Integer noOfOverdueInstallments = null;
        final boolean isMandatoryDeposit = false;
        final boolean allowWithdrawal = false;
        final boolean adjustAdvanceTowardsFuturePayments = false;
        final boolean isCalendarInherited = false;
        final Integer recurringFrequency = null;
        final EnumOptionData recurringFrequencyType = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;

        return new RecurringDepositAccountData(accountId, accountNo, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, minBalanceForInterestCalculation, summary,
                transactions, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, preClosurePenalApplicable,
                preClosurePenalInterest, preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm,
                maxDepositTerm, minDepositTermType, maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType,
                depositAmount, maturityAmount, maturityDate, depositPeriod, depositPeriodFrequency, mandatoryRecommendedDepositAmount,
                periodFrequencyTypeOptions, depositType, onAccountClosure, onAccountClosureOptions, paymentTypeOptions,
                savingsAccountDatas, expectedFirstDepositOnDate, totalOverdueAmount, noOfOverdueInstallments, isMandatoryDeposit,
                allowWithdrawal, adjustAdvanceTowardsFuturePayments, isCalendarInherited, recurringFrequency, recurringFrequencyType,
                withHoldTax, taxGroup);
    }

    public static RecurringDepositAccountData withClosureTemplateDetails(final RecurringDepositAccountData account,
            final Collection<EnumOptionData> onAccountClosureOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<SavingsAccountData> savingsAccountDatas) {

        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
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
                account.depositPeriod, account.depositPeriodFrequency, account.mandatoryRecommendedDepositAmount,
                account.periodFrequencyTypeOptions, account.depositType, account.onAccountClosure, onAccountClosureOptions,
                paymentTypeOptions, savingsAccountDatas, account.expectedFirstDepositOnDate, account.totalOverdueAmount,
                account.noOfOverdueInstallments, account.isMandatoryDeposit, account.allowWithdrawal,
                account.adjustAdvanceTowardsFuturePayments, account.isCalendarInherited, account.recurringFrequency,
                account.recurringFrequencyType, account.withHoldTax, account.taxGroup);
    }

    private RecurringDepositAccountData(final Long id, final String accountNo, final String externalId, final Long groupId,
            final String groupName, final Long clientId, final String clientName, final Long productId, final String productName,
            final Long fieldofficerId, final String fieldofficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal nominalAnnualInterestRate,
            final EnumOptionData interestPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final boolean withdrawalFeeForTransfers,
            final BigDecimal minBalanceForInterestCalculation, final SavingsAccountSummaryData summary,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<DepositProductData> productOptions,
            final Collection<StaffData> fieldOfficerOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
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
            final BigDecimal mandatoryRecommendedDepositAmount, final Collection<EnumOptionData> periodFrequencyTypeOptions,
            final EnumOptionData depositType, final EnumOptionData onAccountClosure,
            final Collection<EnumOptionData> onAccountClosureOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<SavingsAccountData> savingsAccountDatas, final LocalDate expectedFirstDepositOnDate,
            final BigDecimal totalOverdueAmount, final Integer noOfOverdueInstallments, final boolean isMandatoryDeposit,
            final boolean allowWithdrawal, final boolean adjustAdvanceTowardsFuturePayments, final boolean isCalendarInherited,
            final Integer recurringFrequency, final EnumOptionData recurringFrequencyType, final boolean withHoldTax,
            final TaxGroupData taxGroup) {

        super(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName, fieldofficerId,
                fieldofficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType, interestPostingPeriodType,
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
        this.expectedFirstDepositOnDate = expectedFirstDepositOnDate;
        this.mandatoryRecommendedDepositAmount = mandatoryRecommendedDepositAmount;
        this.totalOverdueAmount = totalOverdueAmount;
        this.noOfOverdueInstallments = noOfOverdueInstallments;
        this.isMandatoryDeposit = isMandatoryDeposit;
        this.allowWithdrawal = allowWithdrawal;
        this.adjustAdvanceTowardsFuturePayments = adjustAdvanceTowardsFuturePayments;

        this.isCalendarInherited = isCalendarInherited;
        this.recurringFrequency = recurringFrequency;
        this.recurringFrequencyType = recurringFrequencyType;

        this.preClosurePenalInterestOnTypeOptions = preClosurePenalInterestOnTypeOptions;
        this.periodFrequencyTypeOptions = periodFrequencyTypeOptions;
        this.onAccountClosure = onAccountClosure;
        this.savingsAccounts = savingsAccountDatas;

        // account close template options
        this.onAccountClosureOptions = onAccountClosureOptions;
        this.paymentTypeOptions = paymentTypeOptions;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final RecurringDepositAccountData rhs = (RecurringDepositAccountData) obj;
        return new EqualsBuilder().append(this.id, rhs.id).append(this.accountNo, rhs.accountNo).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.id).append(this.accountNo).toHashCode();
    }

}