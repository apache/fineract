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
package org.apache.fineract.portfolio.loanproduct.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException;
import org.apache.fineract.portfolio.rate.domain.Rate;

/**
 * Loan products allow for categorisation of an organisations loans into something meaningful to them.
 *
 * They provide a means of simplifying creation/maintenance of loans. They can also allow for product comparison to take
 * place when reporting.
 *
 * They allow for constraints to be added at product level.
 */
@Entity
@Getter
@Setter
@Table(name = "m_product_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE"),
        @UniqueConstraint(columnNames = { "short_name" }, name = "unq_short_name") })
public class LoanProduct extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @Column(name = "loan_transaction_strategy_code", nullable = false)
    private String transactionProcessingStrategyCode;

    @Column(name = "loan_transaction_strategy_name")
    private String transactionProcessingStrategyName;

    // TODO FINERACT-1932-Fineract modularization: Move to fineract-progressive-loan module after removing association
    // from LoanProduct entity
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LoanProductPaymentAllocationRule> paymentAllocationRules = new ArrayList<>();

    // TODO FINERACT-1932-Fineract modularization: Move to fineract-progressive-loan module after removing association
    // from LoanProduct entity
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LoanProductCreditAllocationRule> creditAllocationRules = new ArrayList<>();

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", nullable = false, unique = true)
    private String shortName;

    @Column(name = "description")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_product_loan_charge", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private List<Charge> charges;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "m_product_loan_rate", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "rate_id"))
    private List<Rate> rates;

    @Embedded
    private LoanProductRelatedDetail loanProductRelatedDetail;

    @Embedded
    private LoanProductMinMaxConstraints loanProductMinMaxConstraints;

    @Column(name = "accounting_type", nullable = false)
    private AccountingRuleType accountingRule;

    @Column(name = "include_in_borrower_cycle")
    private boolean includeInBorrowerCycle;

    @Column(name = "use_borrower_cycle")
    private boolean useBorrowerCycle;

    @Embedded
    private LoanProductTrancheDetails loanProductTrancheDetails;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "external_id", length = 100, unique = true)
    private ExternalId externalId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductBorrowerCycleVariations> borrowerCycleVariations = new HashSet<>();

    @Column(name = "overdue_days_for_npa")
    private Integer overdueDaysForNPA;

    @Column(name = "min_days_between_disbursal_and_first_repayment")
    private Integer minimumDaysBetweenDisbursalAndFirstRepayment;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductInterestRecalculationDetails productInterestRecalculationDetails;

    @Column(name = "hold_guarantee_funds")
    private boolean holdGuaranteeFunds;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductGuaranteeDetails loanProductGuaranteeDetails;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true)
    private LoanProductConfigurableAttributes loanConfigurableAttributes;

    @Column(name = "principal_threshold_for_last_installment", scale = 2, precision = 5, nullable = false)
    private BigDecimal principalThresholdForLastInstallment;

    @Column(name = "account_moves_out_of_npa_only_on_arrears_completion")
    private boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;

    @Column(name = "can_define_fixed_emi_amount")
    private boolean canDefineInstallmentAmount;

    @Column(name = "is_linked_to_floating_interest_rates", nullable = false)
    private boolean isLinkedToFloatingInterestRate;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductFloatingRates floatingRates;

    @Column(name = "allow_variabe_installments", nullable = false)
    private boolean allowVariabeInstallments;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanProductVariableInstallmentConfig variableInstallmentConfig;

    @Column(name = "sync_expected_with_disbursement_date")
    private boolean syncExpectedWithDisbursementDate;

    @Column(name = "can_use_for_topup", nullable = false)
    private boolean canUseForTopup = false;

    @Column(name = "fixed_principal_percentage_per_installment", scale = 2, precision = 5)
    private BigDecimal fixedPrincipalPercentagePerInstallment;

    @Column(name = "disallow_expected_disbursements", nullable = false)
    private boolean disallowExpectedDisbursements;

    @Column(name = "allow_approved_disbursed_amounts_over_applied", nullable = false)
    private boolean allowApprovedDisbursedAmountsOverApplied;

    @Column(name = "over_applied_calculation_type")
    private String overAppliedCalculationType;

    @Column(name = "over_applied_number")
    private Integer overAppliedNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delinquency_bucket_id", nullable = true)
    private DelinquencyBucket delinquencyBucket;

    @Column(name = "enable_installment_level_delinquency", nullable = false)
    private boolean enableInstallmentLevelDelinquency = false;

    @Column(name = "due_days_for_repayment_event")
    private Integer dueDaysForRepaymentEvent;

    @Column(name = "overdue_days_for_repayment_event")
    private Integer overDueDaysForRepaymentEvent;

    @Column(name = "repayment_start_date_type_enum", nullable = false)
    private RepaymentStartDateType repaymentStartDateType;

    public void updateLoanProductInRelatedClasses() {
        if (this.isInterestRecalculationEnabled()) {
            this.productInterestRecalculationDetails.updateProduct(this);
        }
        if (this.holdGuaranteeFunds) {
            this.loanProductGuaranteeDetails.updateProduct(this);
        }
    }

    public LoanProduct() {
        this.loanProductRelatedDetail = null;
        this.loanProductMinMaxConstraints = null;
    }

    public LoanProduct(final Fund fund, final String transactionProcessingStrategyCode,
            final List<LoanProductPaymentAllocationRule> paymentAllocationRules,
            final List<LoanProductCreditAllocationRule> creditAllocationRules, final String name, final String shortName,
            final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultMinPrincipal, final BigDecimal defaultMaxPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final BigDecimal defaultMinNominalInterestRatePerPeriod,
            final BigDecimal defaultMaxNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean considerPartialPeriodInterest,
            final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments,
            final Integer defaultMinNumberOfInstallments, final Integer defaultMaxNumberOfInstallments,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final AmortizationMethod amortizationMethod,
            final BigDecimal inArrearsTolerance, final List<Charge> charges, final AccountingRuleType accountingRuleType,
            final boolean includeInBorrowerCycle, final LocalDate startDate, final LocalDate closeDate, final ExternalId externalId,
            final boolean useBorrowerCycle, final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations,
            final boolean multiDisburseLoan, final Integer maxTrancheCount, final BigDecimal outstandingLoanBalance,
            final Integer graceOnArrearsAgeing, final Integer overdueDaysForNPA, final DaysInMonthType daysInMonthType,
            final DaysInYearType daysInYearType, final boolean isInterestRecalculationEnabled,
            final LoanProductInterestRecalculationDetails productInterestRecalculationDetails,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final boolean holdGuarantorFunds,
            final LoanProductGuaranteeDetails loanProductGuaranteeDetails, final BigDecimal principalThresholdForLastInstallment,
            final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion, final boolean canDefineEmiAmount,
            final Integer installmentAmountInMultiplesOf, final LoanProductConfigurableAttributes loanProductConfigurableAttributes,
            Boolean isLinkedToFloatingInterestRates, FloatingRate floatingRate, BigDecimal interestRateDifferential,
            BigDecimal minDifferentialLendingRate, BigDecimal maxDifferentialLendingRate, BigDecimal defaultDifferentialLendingRate,
            Boolean isFloatingInterestRateCalculationAllowed, final Boolean isVariableInstallmentsAllowed,
            final Integer minimumGapBetweenInstallments, final Integer maximumGapBetweenInstallments,
            final boolean syncExpectedWithDisbursementDate, final boolean canUseForTopup, final boolean isEqualAmortization,
            final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment, final boolean disallowExpectedDisbursements,
            final boolean allowApprovedDisbursedAmountsOverApplied, final String overAppliedCalculationType,
            final Integer overAppliedNumber, final Integer dueDaysForRepaymentEvent, final Integer overDueDaysForRepaymentEvent,
            final boolean enableDownPayment, final BigDecimal disbursedAmountPercentageForDownPayment,
            final boolean enableAutoRepaymentForDownPayment, final RepaymentStartDateType repaymentStartDateType,
            final boolean enableInstallmentLevelDelinquency, final LoanScheduleType loanScheduleType,
            final LoanScheduleProcessingType loanScheduleProcessingType, final Integer fixedLength,
            final boolean enableAccrualActivityPosting, final List<LoanSupportedInterestRefundTypes> supportedInterestRefundTypes,
            final LoanChargeOffBehaviour chargeOffBehaviour, final boolean isInterestRecognitionOnDisbursementDate,
            final DaysInYearCustomStrategyType daysInYearCustomStrategy, final boolean enableIncomeCapitalization,
            final LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType,
            final LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy, final LoanCapitalizedIncomeType capitalizedIncomeType,
            final boolean enableBuyDownFee, final LoanBuyDownFeeCalculationType buyDownFeeCalculationType,
            final LoanBuyDownFeeStrategy buyDownFeeStrategy, final LoanBuyDownFeeIncomeType buyDownFeeIncomeType) {
        this.fund = fund;
        this.transactionProcessingStrategyCode = transactionProcessingStrategyCode;

        this.paymentAllocationRules = paymentAllocationRules;
        if (this.paymentAllocationRules != null) {
            for (LoanProductPaymentAllocationRule loanProductPaymentAllocationRule : this.paymentAllocationRules) {
                loanProductPaymentAllocationRule.setLoanProduct(this);
            }
        }

        this.creditAllocationRules = creditAllocationRules;
        if (this.creditAllocationRules != null) {
            for (LoanProductCreditAllocationRule loanProductCreditAllocationRule : this.creditAllocationRules) {
                loanProductCreditAllocationRule.setLoanProduct(this);
            }
        }

        this.name = name.trim();
        this.shortName = shortName.trim();
        if (StringUtils.isNotBlank(description)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }

        if (charges != null) {
            this.charges = charges;
        }

        this.isLinkedToFloatingInterestRate = isLinkedToFloatingInterestRates != null && isLinkedToFloatingInterestRates;
        if (isLinkedToFloatingInterestRate) {
            this.floatingRates = new LoanProductFloatingRates(floatingRate, this, interestRateDifferential, minDifferentialLendingRate,
                    maxDifferentialLendingRate, defaultDifferentialLendingRate, isFloatingInterestRateCalculationAllowed);
        }

        this.allowVariabeInstallments = isVariableInstallmentsAllowed != null && isVariableInstallmentsAllowed;

        if (allowVariabeInstallments) {
            this.variableInstallmentConfig = new LoanProductVariableInstallmentConfig(this, minimumGapBetweenInstallments,
                    maximumGapBetweenInstallments);
        }

        this.loanProductRelatedDetail = new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod,
                interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod,
                considerPartialPeriodInterest, repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance, graceOnArrearsAgeing, daysInMonthType.getValue(), daysInYearType.getValue(),
                isInterestRecalculationEnabled, isEqualAmortization, enableDownPayment, disbursedAmountPercentageForDownPayment,
                enableAutoRepaymentForDownPayment, loanScheduleType, loanScheduleProcessingType, fixedLength, enableAccrualActivityPosting,
                supportedInterestRefundTypes, chargeOffBehaviour, isInterestRecognitionOnDisbursementDate, daysInYearCustomStrategy,
                enableIncomeCapitalization, capitalizedIncomeCalculationType, capitalizedIncomeStrategy, capitalizedIncomeType,
                installmentAmountInMultiplesOf, enableBuyDownFee, buyDownFeeCalculationType, buyDownFeeStrategy, buyDownFeeIncomeType);

        this.loanProductMinMaxConstraints = new LoanProductMinMaxConstraints(defaultMinPrincipal, defaultMaxPrincipal,
                defaultMinNominalInterestRatePerPeriod, defaultMaxNominalInterestRatePerPeriod, defaultMinNumberOfInstallments,
                defaultMaxNumberOfInstallments);

        this.accountingRule = accountingRuleType;
        this.includeInBorrowerCycle = includeInBorrowerCycle;
        this.useBorrowerCycle = useBorrowerCycle;

        this.startDate = startDate;
        this.closeDate = closeDate;

        this.externalId = externalId;
        this.borrowerCycleVariations = loanProductBorrowerCycleVariations;
        for (LoanProductBorrowerCycleVariations borrowerCycleVariations : this.borrowerCycleVariations) {
            borrowerCycleVariations.updateLoanProduct(this);
        }
        if (loanProductConfigurableAttributes != null) {
            this.loanConfigurableAttributes = loanProductConfigurableAttributes;
            loanConfigurableAttributes.updateLoanProduct(this);
        }

        this.loanProductTrancheDetails = new LoanProductTrancheDetails(multiDisburseLoan, maxTrancheCount, outstandingLoanBalance);
        this.overdueDaysForNPA = overdueDaysForNPA;
        this.productInterestRecalculationDetails = productInterestRecalculationDetails;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = minimumDaysBetweenDisbursalAndFirstRepayment;
        this.holdGuaranteeFunds = holdGuarantorFunds;
        this.loanProductGuaranteeDetails = loanProductGuaranteeDetails;
        this.principalThresholdForLastInstallment = principalThresholdForLastInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion;
        this.canDefineInstallmentAmount = canDefineEmiAmount;
        this.syncExpectedWithDisbursementDate = syncExpectedWithDisbursementDate;
        this.canUseForTopup = canUseForTopup;
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;

        this.disallowExpectedDisbursements = disallowExpectedDisbursements;
        this.allowApprovedDisbursedAmountsOverApplied = allowApprovedDisbursedAmountsOverApplied;
        this.overAppliedCalculationType = overAppliedCalculationType;
        this.overAppliedNumber = overAppliedNumber;

        if (rates != null) {
            this.rates = rates;
        }

        this.dueDaysForRepaymentEvent = dueDaysForRepaymentEvent;
        this.overDueDaysForRepaymentEvent = overDueDaysForRepaymentEvent;
        this.repaymentStartDateType = repaymentStartDateType;

        this.enableInstallmentLevelDelinquency = enableInstallmentLevelDelinquency;
        validateLoanProductPreSave();
    }

    public void validateLoanProductPreSave() {
        if (this.paymentAllocationRules != null && paymentAllocationRules.size() > 0
                && !transactionProcessingStrategyCode.equals("advanced-payment-allocation-strategy")) {
            throw new LoanProductGeneralRuleException(
                    "payment_allocation.must.not.be.provided.when.allocation.strategy.is.not.advanced-payment-strategy",
                    "In case '" + transactionProcessingStrategyCode + "' payment strategy, payment_allocation must not be provided");
        }

        if (this.creditAllocationRules != null && creditAllocationRules.size() > 0
                && !transactionProcessingStrategyCode.equals("advanced-payment-allocation-strategy")) {
            throw new LoanProductGeneralRuleException(
                    "creditAllocation.must.not.be.provided.when.allocation.strategy.is.not.advanced-payment-strategy",
                    "In case '" + transactionProcessingStrategyCode + "' payment strategy, creditAllocation must not be provided");
        }

        if (this.disallowExpectedDisbursements) {
            if (!this.isMultiDisburseLoan()) {
                throw new LoanProductGeneralRuleException("allowMultipleDisbursals.not.set.disallowExpectedDisbursements.cant.be.set",
                        "Allow Multiple Disbursals Not Set - Disallow Expected Disbursals Can't Be Set");
            }
        }

        if (this.allowApprovedDisbursedAmountsOverApplied) {
            if (this.isMultiDisburseLoan() && !this.disallowExpectedDisbursements) {
                throw new LoanProductGeneralRuleException(
                        "disallowExpectedDisbursements.not.set.allowApprovedDisbursedAmountsOverApplied.cant.be.set",
                        "Disallow Expected Disbursals Not Set - Allow Approved / Disbursed Amounts Over Applied Can't Be Set");
            }
        }

        if (this.overAppliedCalculationType == null || this.overAppliedCalculationType.isEmpty()) {
            if (this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException(
                        "allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedCalculationType.is.mandatory",
                        "Allow Approved / Disbursed Amounts Over Applied is Set - Over Applied Calculation Type is Mandatory");
            }

        } else {
            if (!this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException(
                        "allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedCalculationType.cant.be.entered",
                        "Allow Approved / Disbursed Amounts Over Applied is Not Set - Over Applied Calculation Type Can't Be Entered");
            }

            List<String> overAppliedCalculationTypeAllowedValues = Arrays.asList("percentage", "flat");
            if (!overAppliedCalculationTypeAllowedValues.contains(this.overAppliedCalculationType)) {
                throw new LoanProductGeneralRuleException("overAppliedCalculationType.must.be.percentage.or.flat",
                        "Over Applied Calculation Type Must Be 'percentage' or 'flat'");
            }
        }

        if (this.overAppliedNumber != null) {
            if (!this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException(
                        "allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedNumber.cant.be.entered",
                        "Allow Approved / Disbursed Amounts Over Applied is Not Set - Over Applied Number Can't Be Entered");
            }
        } else {
            if (this.allowApprovedDisbursedAmountsOverApplied) {
                throw new LoanProductGeneralRuleException("allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedNumber.is.mandatory",
                        "Allow Approved / Disbursed Amounts Over Applied is Set - Over Applied Number is Mandatory");
            }
        }

        if (this.getLoanProductRelatedDetail().isInterestRecognitionOnDisbursementDate()
                && this.getLoanProductRelatedDetail().getLoanScheduleType().equals(LoanScheduleType.CUMULATIVE)) {
            throw new LoanProductGeneralRuleException(
                    "interestRecognitionOnDisbursementDate.is.only.supported.for.progressive.loan.schedule.type",
                    "interestRecognitionOnDisbursementDate is only supported for progressive loan schedule type");

        }

        if (this.getLoanProductRelatedDetail().getDaysInYearCustomStrategy() != null
                && this.getLoanProductRelatedDetail().getLoanScheduleType().equals(LoanScheduleType.CUMULATIVE)) {
            throw new LoanProductGeneralRuleException("days.in.year.custom.strategy.is.only.supported.for.progressive.loan.schedule.type",
                    "daysInYearCustomStrategy is only supported for progressive loan schedule type");
        }

        if (this.getLoanProductRelatedDetail().getDaysInYearCustomStrategy() != null
                && !this.getLoanProductRelatedDetail().getDaysInYearType().equals(DaysInYearType.ACTUAL.getValue())) {
            throw new LoanProductGeneralRuleException("days.in.year.custom.strategy.is.only.applicable.for.actual.days.in.year.type",
                    "daysInYearCustomStrategy is only applicable for ACTUAL days in year type");
        }

    }

    public MonetaryCurrency getCurrency() {
        return this.loanProductRelatedDetail.getCurrency();
    }

    public boolean hasCurrencyCodeOf(final String currencyCode) {
        return this.loanProductRelatedDetail.hasCurrencyCodeOf(currencyCode);
    }

    public boolean update(final List<Charge> newProductCharges) {
        if (newProductCharges == null) {
            return false;
        }

        boolean updated = false;
        if (this.charges != null) {
            final Set<Charge> currentSetOfCharges = new HashSet<>(this.charges);
            final Set<Charge> newSetOfCharges = new HashSet<>(newProductCharges);

            if (!currentSetOfCharges.equals(newSetOfCharges)) {
                updated = true;
                this.charges = newProductCharges;
            }
        } else {
            updated = true;
            this.charges = newProductCharges;
        }
        return updated;
    }

    public boolean updateRates(final List<Rate> newProductRates) {
        if (newProductRates == null) {
            return false;
        }

        boolean updated = false;
        if (this.rates != null) {
            final Set<Rate> currentSetOfCharges = new HashSet<>(this.rates);
            final Set<Rate> newSetOfCharges = new HashSet<>(newProductRates);

            if (!currentSetOfCharges.equals(newSetOfCharges)) {
                updated = true;
                this.rates = newProductRates;
            }
        } else {
            updated = true;
            this.rates = newProductRates;
        }
        return updated;
    }

    public LoanProductFloatingRates loanProductFloatingRates() {
        this.floatingRates = this.floatingRates == null ? new LoanProductFloatingRates(null, this, null, null, null, null, false)
                : this.floatingRates;
        return this.floatingRates;
    }

    public LoanProductVariableInstallmentConfig loanProductVariableInstallmentConfig() {
        this.variableInstallmentConfig = this.variableInstallmentConfig == null ? new LoanProductVariableInstallmentConfig(this, null, null)
                : this.variableInstallmentConfig;
        return this.variableInstallmentConfig;
    }

    public boolean isAccountingDisabled() {
        return AccountingRuleType.NONE.equals(this.accountingRule);
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.equals(this.accountingRule);
    }

    public boolean isUpfrontAccrualAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_UPFRONT.equals(this.accountingRule);
    }

    public boolean isPeriodicAccrualAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_PERIODIC.equals(this.accountingRule);
    }

    public Money getPrincipalAmount() {
        return this.loanProductRelatedDetail.getPrincipal();
    }

    public Money getMinPrincipalAmount() {
        return Money.of(this.loanProductRelatedDetail.getCurrency(), loanProductMinMaxConstraints().getMinPrincipal());
    }

    public Money getMaxPrincipalAmount() {
        return Money.of(this.loanProductRelatedDetail.getCurrency(), loanProductMinMaxConstraints().getMaxPrincipal());
    }

    public BigDecimal getNominalInterestRatePerPeriod() {
        return this.loanProductRelatedDetail.getNominalInterestRatePerPeriod();
    }

    public PeriodFrequencyType getInterestPeriodFrequencyType() {
        return this.loanProductRelatedDetail.getInterestPeriodFrequencyType();
    }

    public BigDecimal getMinNominalInterestRatePerPeriod() {
        return loanProductMinMaxConstraints().getMinNominalInterestRatePerPeriod();
    }

    public BigDecimal getMaxNominalInterestRatePerPeriod() {
        return loanProductMinMaxConstraints().getMaxNominalInterestRatePerPeriod();
    }

    public Integer getNumberOfRepayments() {
        return this.loanProductRelatedDetail.getNumberOfRepayments();
    }

    public Integer getMinNumberOfRepayments() {
        return loanProductMinMaxConstraints().getMinNumberOfRepayments();
    }

    public Integer getMaxNumberOfRepayments() {
        return loanProductMinMaxConstraints().getMaxNumberOfRepayments();
    }

    public LoanProductMinMaxConstraints loanProductMinMaxConstraints() {
        // If all min and max fields are null then loanProductMinMaxConstraints
        // initialising to null
        // Reset LoanProductMinMaxConstraints with null values.
        this.loanProductMinMaxConstraints = this.loanProductMinMaxConstraints == null
                ? new LoanProductMinMaxConstraints(null, null, null, null, null, null)
                : this.loanProductMinMaxConstraints;
        return this.loanProductMinMaxConstraints;
    }

    public boolean isMultiDisburseLoan() {
        return this.loanProductTrancheDetails.isMultiDisburseLoan();
    }

    public BigDecimal outstandingLoanBalance() {
        return this.loanProductTrancheDetails.getOutstandingLoanBalance();
    }

    public Integer maxTrancheCount() {
        return this.loanProductTrancheDetails.getMaxTrancheCount();
    }

    public boolean isInterestRecalculationEnabled() {
        return this.loanProductRelatedDetail.isInterestRecalculationEnabled();
    }

    public Integer getMinimumDaysBetweenDisbursalAndFirstRepayment() {
        return this.minimumDaysBetweenDisbursalAndFirstRepayment == null ? 0 : this.minimumDaysBetweenDisbursalAndFirstRepayment;
    }

    public Map<String, BigDecimal> fetchBorrowerCycleVariationsForCycleNumber(final Integer cycleNumber) {
        Map<String, BigDecimal> borrowerCycleVariations = new HashMap<>();
        borrowerCycleVariations.put(LoanProductConstants.PRINCIPAL, this.loanProductRelatedDetail.getPrincipal().getAmount());
        borrowerCycleVariations.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD,
                this.loanProductRelatedDetail.getNominalInterestRatePerPeriod());
        if (this.loanProductRelatedDetail.getNumberOfRepayments() != null) {
            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                    BigDecimal.valueOf(this.loanProductRelatedDetail.getNumberOfRepayments()));
        }

        if (this.loanProductMinMaxConstraints != null) {
            borrowerCycleVariations.put(LoanProductConstants.MIN_PRINCIPAL, this.loanProductMinMaxConstraints.getMinPrincipal());
            borrowerCycleVariations.put(LoanProductConstants.MAX_PRINCIPAL, this.loanProductMinMaxConstraints.getMaxPrincipal());
            borrowerCycleVariations.put(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD,
                    this.loanProductMinMaxConstraints.getMinNominalInterestRatePerPeriod());
            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                    this.loanProductMinMaxConstraints.getMaxNominalInterestRatePerPeriod());

            if (this.loanProductMinMaxConstraints.getMinNumberOfRepayments() != null) {
                borrowerCycleVariations.put(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS,
                        BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMinNumberOfRepayments()));
            }

            if (this.loanProductMinMaxConstraints.getMaxNumberOfRepayments() != null) {
                borrowerCycleVariations.put(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS,
                        BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMaxNumberOfRepayments()));
            }
        }
        if (cycleNumber > 0) {
            Integer principalCycleUsed = 0;
            Integer interestCycleUsed = 0;
            Integer repaymentCycleUsed = 0;
            for (LoanProductBorrowerCycleVariations cycleVariation : this.borrowerCycleVariations) {
                if (cycleVariation.getBorrowerCycleNumber().equals(cycleNumber)
                        && cycleVariation.getValueConditionType().equals(LoanProductValueConditionType.EQUAL)) {
                    switch (cycleVariation.getParamType()) {
                        case PRINCIPAL -> {
                            borrowerCycleVariations.put(LoanProductConstants.PRINCIPAL, cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.MIN_PRINCIPAL, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.MAX_PRINCIPAL, cycleVariation.getMaxValue());
                            principalCycleUsed = cycleVariation.getBorrowerCycleNumber();
                        }
                        case INTERESTRATE -> {
                            borrowerCycleVariations.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD, cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD, cycleVariation.getMaxValue());
                            interestCycleUsed = cycleVariation.getBorrowerCycleNumber();
                        }
                        case REPAYMENT -> {
                            borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                                    cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS, cycleVariation.getMaxValue());
                            repaymentCycleUsed = cycleVariation.getBorrowerCycleNumber();
                        }
                        case INVALID -> {

                        }
                    }
                } else if (cycleVariation.getBorrowerCycleNumber() < cycleNumber
                        && cycleVariation.getValueConditionType().equals(LoanProductValueConditionType.GREATERTHAN)) {
                    switch (cycleVariation.getParamType()) {
                        case PRINCIPAL:
                            if (principalCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.PRINCIPAL, cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.MIN_PRINCIPAL, cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.MAX_PRINCIPAL, cycleVariation.getMaxValue());
                                principalCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                        break;
                        case INTERESTRATE:
                            if (interestCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.MIN_INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getMaxValue());
                                interestCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                        break;
                        case REPAYMENT:
                            if (repaymentCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.MAX_INTEREST_RATE_PER_PERIOD,
                                        cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.MIN_NUMBER_OF_REPAYMENTS, cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.MAX_NUMBER_OF_REPAYMENTS, cycleVariation.getMaxValue());
                                repaymentCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                        break;
                        default:
                        break;
                    }
                }
            }
        }
        return borrowerCycleVariations;
    }

    public DaysInMonthType fetchDaysInMonthType() {
        return this.loanProductRelatedDetail.fetchDaysInMonthType();
    }

    public DaysInYearType fetchDaysInYearType() {
        return this.loanProductRelatedDetail.fetchDaysInYearType();
    }

    public boolean isArrearsBasedOnOriginalSchedule() {
        boolean isBasedOnOriginalSchedule = false;
        if (getProductInterestRecalculationDetails() != null) {
            isBasedOnOriginalSchedule = getProductInterestRecalculationDetails().isArrearsBasedOnOriginalSchedule();
        }
        return isBasedOnOriginalSchedule;
    }

    public LoanPreCloseInterestCalculationStrategy preCloseInterestCalculationStrategy() {
        LoanPreCloseInterestCalculationStrategy preCloseInterestCalculationStrategy = LoanPreCloseInterestCalculationStrategy.NONE;
        if (this.isInterestRecalculationEnabled()) {
            preCloseInterestCalculationStrategy = getProductInterestRecalculationDetails().getPreCloseInterestCalculationStrategy();
        }
        return preCloseInterestCalculationStrategy;
    }

    public Collection<FloatingRatePeriodData> fetchInterestRates(final FloatingRateDTO floatingRateDTO) {
        Collection<FloatingRatePeriodData> applicableRates = new ArrayList<>(1);
        if (isLinkedToFloatingInterestRate()) {
            applicableRates = getFloatingRates().fetchInterestRates(floatingRateDTO);
        }
        return applicableRates;
    }

    public boolean isEqualAmortization() {
        return loanProductRelatedDetail.isEqualAmortization();
    }

    public RepaymentStartDateType getRepaymentStartDateType() {
        return this.repaymentStartDateType == null ? RepaymentStartDateType.INVALID : this.repaymentStartDateType;
    }

    public void updateEnableInstallmentLevelDelinquency(boolean enableInstallmentLevelDelinquency) {
        this.enableInstallmentLevelDelinquency = enableInstallmentLevelDelinquency;
    }

}
