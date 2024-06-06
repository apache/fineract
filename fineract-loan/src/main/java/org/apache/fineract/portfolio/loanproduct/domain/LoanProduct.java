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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
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
public class LoanProduct extends AbstractPersistableCustom {

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
    private Integer accountingRule;

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

    @Column(name = "instalment_amount_in_multiples_of")
    private Integer installmentAmountInMultiplesOf;

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

    public static LoanProduct assembleFromJson(final Fund fund, final String loanTransactionProcessingStrategy,
            final List<Charge> productCharges, final JsonCommand command, final AprCalculator aprCalculator, FloatingRate floatingRate,
            final List<Rate> productRates, List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules,
            List<LoanProductCreditAllocationRule> loanProductCreditAllocationRules) {

        final String name = command.stringValueOfParameterNamed("name");
        final String shortName = command.stringValueOfParameterNamed(LoanProductConstants.SHORT_NAME);
        final String description = command.stringValueOfParameterNamed("description");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");
        final Integer inMultiplesOf = command.integerValueOfParameterNamed("inMultiplesOf");

        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed("principal");
        final BigDecimal minPrincipal = command.bigDecimalValueOfParameterNamed("minPrincipal");
        final BigDecimal maxPrincipal = command.bigDecimalValueOfParameterNamed("maxPrincipal");

        final InterestMethod interestMethod = InterestMethod.fromInt(command.integerValueOfParameterNamed("interestType"));
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                .fromInt(command.integerValueOfParameterNamed("interestCalculationPeriodType"));
        final boolean allowPartialPeriodInterestCalcualtion = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME);
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.integerValueOfParameterNamed("amortizationType"));
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
                .fromInt(command.integerValueOfParameterNamed("repaymentFrequencyType"));
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.INVALID;
        BigDecimal interestRatePerPeriod = null;
        BigDecimal minInterestRatePerPeriod = null;
        BigDecimal maxInterestRatePerPeriod = null;
        BigDecimal annualInterestRate = null;
        BigDecimal interestRateDifferential = null;
        BigDecimal minDifferentialLendingRate = null;
        BigDecimal maxDifferentialLendingRate = null;
        BigDecimal defaultDifferentialLendingRate = null;
        Boolean isFloatingInterestRateCalculationAllowed = null;

        Integer minimumGapBetweenInstallments = null;
        Integer maximumGapBetweenInstallments = null;

        final Integer repaymentEvery = command.integerValueOfParameterNamed("repaymentEvery");
        final Integer numberOfRepayments = command.integerValueOfParameterNamed("numberOfRepayments");
        final Boolean isLinkedToFloatingInterestRates = command.booleanObjectValueOfParameterNamed("isLinkedToFloatingInterestRates");
        if (isLinkedToFloatingInterestRates != null && isLinkedToFloatingInterestRates) {
            interestRateDifferential = command.bigDecimalValueOfParameterNamed("interestRateDifferential");
            minDifferentialLendingRate = command.bigDecimalValueOfParameterNamed("minDifferentialLendingRate");
            maxDifferentialLendingRate = command.bigDecimalValueOfParameterNamed("maxDifferentialLendingRate");
            defaultDifferentialLendingRate = command.bigDecimalValueOfParameterNamed("defaultDifferentialLendingRate");
            isFloatingInterestRateCalculationAllowed = command
                    .booleanObjectValueOfParameterNamed("isFloatingInterestRateCalculationAllowed");
        } else {
            interestFrequencyType = PeriodFrequencyType.fromInt(command.integerValueOfParameterNamed("interestRateFrequencyType"));
            interestRatePerPeriod = command.bigDecimalValueOfParameterNamed("interestRatePerPeriod");
            minInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("minInterestRatePerPeriod");
            maxInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("maxInterestRatePerPeriod");
            annualInterestRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, numberOfRepayments,
                    repaymentEvery, repaymentFrequencyType);

        }

        final Boolean isVariableInstallmentsAllowed = command
                .booleanObjectValueOfParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName);
        if (isVariableInstallmentsAllowed != null && isVariableInstallmentsAllowed) {
            minimumGapBetweenInstallments = command.integerValueOfParameterNamed(LoanProductConstants.minimumGapBetweenInstallments);
            maximumGapBetweenInstallments = command.integerValueOfParameterNamed(LoanProductConstants.maximumGapBetweenInstallments);
        }

        final Integer minNumberOfRepayments = command.integerValueOfParameterNamed("minNumberOfRepayments");
        final Integer maxNumberOfRepayments = command.integerValueOfParameterNamed("maxNumberOfRepayments");
        final BigDecimal inArrearsTolerance = command.bigDecimalValueOfParameterNamed("inArrearsTolerance");

        // grace details
        final Integer graceOnPrincipalPayment = command.integerValueOfParameterNamed("graceOnPrincipalPayment");
        final Integer recurringMoratoriumOnPrincipalPeriods = command.integerValueOfParameterNamed("recurringMoratoriumOnPrincipalPeriods");
        final Integer graceOnInterestPayment = command.integerValueOfParameterNamed("graceOnInterestPayment");
        final Integer graceOnInterestCharged = command.integerValueOfParameterNamed("graceOnInterestCharged");
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = command
                .integerValueOfParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT);

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));
        final boolean includeInBorrowerCycle = command.booleanPrimitiveValueOfParameterNamed("includeInBorrowerCycle");

        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate closeDate = command.localDateValueOfParameterNamed("closeDate");
        final ExternalId externalId = ExternalIdFactory.produce(command.stringValueOfParameterNamedAllowingNull("externalId"));

        final LoanScheduleType loanScheduleType;
        if (command.hasParameter("loanScheduleType")) {
            loanScheduleType = LoanScheduleType.valueOf(command.stringValueOfParameterNamed("loanScheduleType"));
        } else {
            // For backward compatibility
            loanScheduleType = LoanScheduleType.CUMULATIVE;
        }

        final LoanScheduleProcessingType loanScheduleProcessingType;
        if (LoanScheduleType.PROGRESSIVE.equals(loanScheduleType) && command.hasParameter("loanScheduleProcessingType")) {
            loanScheduleProcessingType = LoanScheduleProcessingType
                    .valueOf(command.stringValueOfParameterNamed("loanScheduleProcessingType"));
        } else {
            // For backward compatibility
            loanScheduleProcessingType = LoanScheduleProcessingType.HORIZONTAL;
        }

        final boolean useBorrowerCycle = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME);
        final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations = new HashSet<>();

        if (useBorrowerCycle) {
            populateBorrowerCycleVariations(command, loanProductBorrowerCycleVariations);
        }

        final boolean multiDisburseLoan = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.MULTI_DISBURSE_LOAN_PARAMETER_NAME);
        Integer maxTrancheCount = null;
        BigDecimal outstandingLoanBalance = null;
        if (multiDisburseLoan) {
            outstandingLoanBalance = command.bigDecimalValueOfParameterNamed(LoanProductConstants.OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME);
            maxTrancheCount = command.integerValueOfParameterNamed(LoanProductConstants.MAX_TRANCHE_COUNT_PARAMETER_NAME);
        }

        final Integer graceOnArrearsAgeing = command
                .integerValueOfParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME);

        final Integer overdueDaysForNPA = command.integerValueOfParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME);

        // Interest recalculation settings
        final boolean isInterestRecalculationEnabled = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);
        final DaysInMonthType daysInMonthType = DaysInMonthType
                .fromInt(command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME));

        final DaysInYearType daysInYearType = DaysInYearType
                .fromInt(command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME));

        LoanProductInterestRecalculationDetails interestRecalculationSettings = null;

        if (isInterestRecalculationEnabled) {
            interestRecalculationSettings = LoanProductInterestRecalculationDetails.createFrom(command);
        }

        final boolean holdGuarantorFunds = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName);
        LoanProductGuaranteeDetails loanProductGuaranteeDetails = null;
        if (holdGuarantorFunds) {
            loanProductGuaranteeDetails = LoanProductGuaranteeDetails.createFrom(command);
        }

        LoanProductConfigurableAttributes loanConfigurableAttributes = null;
        if (command.parameterExists(LoanProductConstants.allowAttributeOverridesParamName)) {
            loanConfigurableAttributes = LoanProductConfigurableAttributes.createFrom(command);
        } else {
            loanConfigurableAttributes = LoanProductConfigurableAttributes.populateDefaultsForConfigurableAttributes();
        }

        BigDecimal principalThresholdForLastInstallment = command
                .bigDecimalValueOfParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName);

        if (principalThresholdForLastInstallment == null) {
            principalThresholdForLastInstallment = multiDisburseLoan
                    ? LoanProductConstants.DEFAULT_PRINCIPAL_THRESHOLD_FOR_MULTI_DISBURSE_LOAN
                    : LoanProductConstants.DEFAULT_PRINCIPAL_THRESHOLD_FOR_SINGLE_DISBURSE_LOAN;
        }
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME);
        final boolean canDefineEmiAmount = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canDefineEmiAmountParamName);
        final Integer installmentAmountInMultiplesOf = command
                .integerValueOfParameterNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName);

        final boolean syncExpectedWithDisbursementDate = command.booleanPrimitiveValueOfParameterNamed("syncExpectedWithDisbursementDate");

        final boolean canUseForTopup = command.parameterExists(LoanProductConstants.CAN_USE_FOR_TOPUP)
                && command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP);

        final boolean isEqualAmortization = command.parameterExists(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM)
                && command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM);

        BigDecimal fixedPrincipalPercentagePerInstallment = command
                .bigDecimalValueOfParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName);

        final boolean disallowExpectedDisbursements = command.parameterExists(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS)
                && command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS);

        final boolean allowApprovedDisbursedAmountsOverApplied = command
                .parameterExists(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED)
                && command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED);

        final String overAppliedCalculationType = command
                .stringValueOfParameterNamedAllowingNull(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE);

        final Integer overAppliedNumber = command.integerValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER);

        final Integer dueDaysForRepaymentEvent = command.integerValueOfParameterNamed(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT);
        final Integer overDueDaysForRepaymentEvent = command
                .integerValueOfParameterNamed(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT);

        final boolean enableDownPayment = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT);
        final BigDecimal disbursedAmountPercentageDownPayment = command
                .bigDecimalValueOfParameterNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT);
        final boolean enableAutoRepaymentForDownPayment = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT);

        final RepaymentStartDateType repaymentStartDateType = RepaymentStartDateType
                .fromInt(command.integerValueOfParameterNamed(LoanProductConstants.REPAYMENT_START_DATE_TYPE));

        final boolean enableInstallmentLevelDelinquency = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY);

        final Integer fixedLength = command.integerValueOfParameterNamed(LoanProductConstants.FIXED_LENGTH);

        return new LoanProduct(fund, loanTransactionProcessingStrategy, loanProductPaymentAllocationRules, loanProductCreditAllocationRules,
                name, shortName, description, currency, principal, minPrincipal, maxPrincipal, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, interestFrequencyType, annualInterestRate, interestMethod,
                interestCalculationPeriodMethod, allowPartialPeriodInterestCalcualtion, repaymentEvery, repaymentFrequencyType,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance, productCharges, accountingRuleType, includeInBorrowerCycle, startDate, closeDate, externalId,
                useBorrowerCycle, loanProductBorrowerCycleVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationSettings, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuarantorFunds,
                loanProductGuaranteeDetails, principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion,
                canDefineEmiAmount, installmentAmountInMultiplesOf, loanConfigurableAttributes, isLinkedToFloatingInterestRates,
                floatingRate, interestRateDifferential, minDifferentialLendingRate, maxDifferentialLendingRate,
                defaultDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed,
                minimumGapBetweenInstallments, maximumGapBetweenInstallments, syncExpectedWithDisbursementDate, canUseForTopup,
                isEqualAmortization, productRates, fixedPrincipalPercentagePerInstallment, disallowExpectedDisbursements,
                allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber, dueDaysForRepaymentEvent,
                overDueDaysForRepaymentEvent, enableDownPayment, disbursedAmountPercentageDownPayment, enableAutoRepaymentForDownPayment,
                repaymentStartDateType, enableInstallmentLevelDelinquency, loanScheduleType, loanScheduleProcessingType, fixedLength);

    }

    public void updateLoanProductInRelatedClasses() {
        if (this.isInterestRecalculationEnabled()) {
            this.productInterestRecalculationDetails.updateProduct(this);
        }
        if (this.holdGuaranteeFunds) {
            this.loanProductGuaranteeDetails.updateProduct(this);
        }
    }

    private static void populateBorrowerCycleVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assemblePrincipalVariations(command, loanProductBorrowerCycleVariations);

        assembleRepaymentVariations(command, loanProductBorrowerCycleVariations);

        assembleInterestRateVariations(command, loanProductBorrowerCycleVariations);
    }

    private static void assembleInterestRateVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVariations(command, loanProductBorrowerCycleVariations, LoanProductParamType.INTERESTRATE.getValue(),
                LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME);

    }

    private static void assembleRepaymentVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVariations(command, loanProductBorrowerCycleVariations, LoanProductParamType.REPAYMENT.getValue(),
                LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME);

    }

    private static void assemblePrincipalVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVariations(command, loanProductBorrowerCycleVariations, LoanProductParamType.PRINCIPAL.getValue(),
                LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME);
    }

    private static void assembleVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations, Integer paramType,
            String variationParameterName) {
        if (command.parameterExists(variationParameterName)) {
            final JsonArray variationArray = command.arrayOfParameterNamed(variationParameterName);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();
                    BigDecimal defaultValue = null;
                    BigDecimal minValue = null;
                    BigDecimal maxValue = null;
                    Integer cycleNumber = null;
                    Integer valueUsageCondition = null;
                    if (jsonObject.has(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).isJsonPrimitive()) {
                        defaultValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MIN_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsString())) {
                        minValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MAX_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsString())) {
                        maxValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).isJsonPrimitive()) {
                        cycleNumber = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME).isJsonPrimitive()) {
                        valueUsageCondition = jsonObject.getAsJsonPrimitive(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                                .getAsInt();
                    }
                    LoanProductBorrowerCycleVariations borrowerCycleVariations = new LoanProductBorrowerCycleVariations(cycleNumber,
                            paramType, valueUsageCondition, minValue, maxValue, defaultValue);
                    loanProductBorrowerCycleVariations.add(borrowerCycleVariations);
                    i++;
                } while (i < variationArray.size());
            }
        }
    }

    private Map<String, Object> updateBorrowerCycleVariations(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);
        List<Long> variationIds = fetchAllVariationIds();
        updateBorrowerCycleVariations(command, LoanProductParamType.PRINCIPAL.getValue(),
                LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        updateBorrowerCycleVariations(command, LoanProductParamType.INTERESTRATE.getValue(),
                LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        updateBorrowerCycleVariations(command, LoanProductParamType.REPAYMENT.getValue(),
                LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        for (Long id : variationIds) {
            this.borrowerCycleVariations.remove(fetchLoanProductBorrowerCycleVariationById(id));
        }
        return actualChanges;
    }

    private List<Long> fetchAllVariationIds() {
        List<Long> list = new ArrayList<>();
        for (LoanProductBorrowerCycleVariations cycleVariation : this.borrowerCycleVariations) {
            list.add(cycleVariation.getId());
        }
        return list;
    }

    private void updateBorrowerCycleVariations(final JsonCommand command, Integer paramType, String variationParameterName,
            final Map<String, Object> actualChanges, List<Long> variationIds) {
        if (command.parameterExists(variationParameterName)) {
            final JsonArray variationArray = command.arrayOfParameterNamed(variationParameterName);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();
                    BigDecimal defaultValue = null;
                    BigDecimal minValue = null;
                    BigDecimal maxValue = null;
                    Integer cycleNumber = null;
                    Integer valueUsageCondition = null;
                    Long id = null;
                    if (jsonObject.has(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).isJsonPrimitive()) {
                        defaultValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MIN_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsString())) {
                        minValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MAX_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsString())) {
                        maxValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).isJsonPrimitive()) {
                        cycleNumber = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME).isJsonPrimitive()) {
                        valueUsageCondition = jsonObject.getAsJsonPrimitive(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                                .getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).isJsonPrimitive() && StringUtils
                                    .isNotBlank(jsonObject.get(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).getAsString())) {
                        id = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).getAsLong();
                    }
                    LoanProductBorrowerCycleVariations borrowerCycleVariations = new LoanProductBorrowerCycleVariations(cycleNumber,
                            paramType, valueUsageCondition, minValue, maxValue, defaultValue);
                    if (id == null) {
                        borrowerCycleVariations.updateLoanProduct(this);
                        this.borrowerCycleVariations.add(borrowerCycleVariations);
                        actualChanges.put("borrowerCycleParamType", paramType);
                    } else {
                        variationIds.remove(id);
                        LoanProductBorrowerCycleVariations existingCycleVariation = fetchLoanProductBorrowerCycleVariationById(id);
                        if (!existingCycleVariation.equals(borrowerCycleVariations)) {
                            existingCycleVariation.copy(borrowerCycleVariations);
                            actualChanges.put("borrowerCycleId", id);
                        }
                    }
                    i++;
                } while (i < variationArray.size());
            }
        }
    }

    private void clearVariations() {
        this.borrowerCycleVariations.clear();
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
            final LoanScheduleProcessingType loanScheduleProcessingType, final Integer fixedLength) {
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
                enableAutoRepaymentForDownPayment, loanScheduleType, loanScheduleProcessingType, fixedLength);

        this.loanProductMinMaxConstraints = new LoanProductMinMaxConstraints(defaultMinPrincipal, defaultMaxPrincipal,
                defaultMinNominalInterestRatePerPeriod, defaultMaxNominalInterestRatePerPeriod, defaultMinNumberOfInstallments,
                defaultMaxNumberOfInstallments);

        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }
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
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
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
            if (!this.disallowExpectedDisbursements) {
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

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator, FloatingRate floatingRate) {

        final Map<String, Object> actualChanges = this.loanProductRelatedDetail.update(command, aprCalculator);
        actualChanges.putAll(loanProductMinMaxConstraints().update(command));

        final String isLinkedToFloatingInterestRates = "isLinkedToFloatingInterestRates";
        if (command.isChangeInBooleanParameterNamed(isLinkedToFloatingInterestRates, this.isLinkedToFloatingInterestRate)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isLinkedToFloatingInterestRates);
            actualChanges.put(isLinkedToFloatingInterestRates, newValue);
            this.isLinkedToFloatingInterestRate = newValue;
        }

        if (this.isLinkedToFloatingInterestRate) {
            actualChanges.putAll(loanProductFloatingRates().update(command, floatingRate));
            this.loanProductRelatedDetail.updateForFloatingInterestRates();
            this.loanProductMinMaxConstraints.updateForFloatingInterestRates();
        } else {
            this.floatingRates = null;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName,
                this.allowVariabeInstallments)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName);
            actualChanges.put(LoanProductConstants.allowVariableInstallmentsParamName, newValue);
            this.allowVariabeInstallments = newValue;
        }

        if (this.allowVariabeInstallments) {
            actualChanges.putAll(loanProductVariableInstallmentConfig().update(command));
        } else {
            this.variableInstallmentConfig = null;
        }

        final String accountingTypeParamName = "accountingRule";
        if (command.isChangeInIntegerParameterNamed(accountingTypeParamName, this.accountingRule)) {
            final Integer newValue = command.integerValueOfParameterNamed(accountingTypeParamName);
            actualChanges.put(accountingTypeParamName, newValue);
            this.accountingRule = newValue;
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String shortNameParamName = LoanProductConstants.SHORT_NAME;
        if (command.isChangeInStringParameterNamed(shortNameParamName, this.shortName)) {
            final String newValue = command.stringValueOfParameterNamed(shortNameParamName);
            actualChanges.put(shortNameParamName, newValue);
            this.shortName = newValue;
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        Long existingFundId = null;
        if (this.fund != null) {
            existingFundId = this.fund.getId();
        }
        final String fundIdParamName = "fundId";
        if (command.isChangeInLongParameterNamed(fundIdParamName, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(fundIdParamName);
            actualChanges.put(fundIdParamName, newValue);
        }

        final String transactionProcessingStrategyCodeParamName = "transactionProcessingStrategyCode";
        if (command.isChangeInStringParameterNamed(transactionProcessingStrategyCodeParamName, this.transactionProcessingStrategyCode)) {
            final String newValue = command.stringValueOfParameterNamed(transactionProcessingStrategyCodeParamName);
            actualChanges.put(transactionProcessingStrategyCodeParamName, newValue);
        }

        final String paymentAllocationParamName = "paymentAllocation";
        if (command.hasParameter(paymentAllocationParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(paymentAllocationParamName);
            if (jsonArray != null) {
                actualChanges.put(paymentAllocationParamName, command.jsonFragment(paymentAllocationParamName));
            }
        }

        final String creditAllocationParamName = "creditAllocation";
        if (command.hasParameter(creditAllocationParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(creditAllocationParamName);
            if (jsonArray != null) {
                actualChanges.put(creditAllocationParamName, command.jsonFragment(creditAllocationParamName));
            }
        }

        final String chargesParamName = "charges";
        if (command.hasParameter(chargesParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(chargesParamName);
            if (jsonArray != null) {
                actualChanges.put(chargesParamName, command.jsonFragment(chargesParamName));
            }
        }

        final String includeInBorrowerCycleParamName = "includeInBorrowerCycle";
        if (command.isChangeInBooleanParameterNamed(includeInBorrowerCycleParamName, this.includeInBorrowerCycle)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(includeInBorrowerCycleParamName);
            actualChanges.put(includeInBorrowerCycleParamName, newValue);
            this.includeInBorrowerCycle = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, this.useBorrowerCycle)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, newValue);
            this.useBorrowerCycle = newValue;
        }

        if (this.useBorrowerCycle) {
            actualChanges.putAll(updateBorrowerCycleVariations(command));
        } else {
            clearVariations();
        }
        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String localeParamName = "locale";
        final String dateFormatParamName = "dateFormat";

        final String startDateParamName = "startDate";
        if (command.isChangeInLocalDateParameterNamed(startDateParamName, getStartDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(startDateParamName);
            actualChanges.put(startDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            this.startDate = command.localDateValueOfParameterNamed(startDateParamName);
        }

        final String closeDateParamName = "closeDate";
        if (command.isChangeInLocalDateParameterNamed(closeDateParamName, getCloseDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(closeDateParamName);
            actualChanges.put(closeDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            this.closeDate = command.localDateValueOfParameterNamed(closeDateParamName);
        }

        final String externalIdTypeParamName = "externalId";
        if (command.isChangeInExternalIdParameterNamed(externalIdTypeParamName, this.externalId)) {
            final ExternalId newValue = ExternalIdFactory.produce(command.stringValueOfParameterNamed(externalIdTypeParamName));
            actualChanges.put(accountingTypeParamName, newValue);
            this.externalId = newValue;
        }
        loanProductTrancheDetails.update(command, actualChanges, localeAsInput);

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, this.overdueDaysForNPA)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            this.overdueDaysForNPA = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT,
                this.minimumDaysBetweenDisbursalAndFirstRepayment)) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT);
            actualChanges.put(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minimumDaysBetweenDisbursalAndFirstRepayment = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("syncExpectedWithDisbursementDate", this.syncExpectedWithDisbursementDate)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("syncExpectedWithDisbursementDate");
            actualChanges.put("syncExpectedWithDisbursementDate", newValue);
            this.syncExpectedWithDisbursementDate = newValue;
        }

        Long delinquencyBucketId = null;
        if (this.delinquencyBucket != null) {
            delinquencyBucketId = this.delinquencyBucket.getId();
        }
        if (command.isChangeInLongParameterNamed(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, delinquencyBucketId)) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME);
            actualChanges.put(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, newValue);
        }

        // Update interest recalculation settings
        final boolean isInterestRecalculationEnabledChanged = actualChanges
                .containsKey(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);

        if (isInterestRecalculationEnabledChanged) {
            if (this.isInterestRecalculationEnabled()) {
                this.productInterestRecalculationDetails = LoanProductInterestRecalculationDetails.createFrom(command);
                this.productInterestRecalculationDetails.updateProduct(this);
                actualChanges.put(LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
                        command.integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName));
                actualChanges.put(LoanProductConstants.rescheduleStrategyMethodParameterName,
                        command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName));
            } else {
                this.productInterestRecalculationDetails = null;
            }
        }

        if (!isInterestRecalculationEnabledChanged && this.isInterestRecalculationEnabled()) {
            this.productInterestRecalculationDetails.update(command, actualChanges, localeAsInput);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName, this.holdGuaranteeFunds)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName);
            actualChanges.put(LoanProductConstants.holdGuaranteeFundsParamName, newValue);
            this.holdGuaranteeFunds = newValue;
        }

        final String configurableAttributesChanges = LoanProductConstants.allowAttributeOverridesParamName;
        if (command.hasParameter(configurableAttributesChanges)) {
            if (!command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                    .isJsonNull()) {
                actualChanges.put(configurableAttributesChanges, command.jsonFragment(configurableAttributesChanges));

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getAmortizationBoolean()) {
                    this.loanConfigurableAttributes.setAmortizationType(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getInterestMethodBoolean()) {
                    this.loanConfigurableAttributes.setInterestType(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyCodeParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getTransactionProcessingStrategyBoolean()) {
                    this.loanConfigurableAttributes.setTransactionProcessingStrategyCode(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyCodeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getInterestCalcPeriodBoolean()) {
                    this.loanConfigurableAttributes.setInterestCalculationPeriodType(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getArrearsToleranceBoolean()) {
                    this.loanConfigurableAttributes.setInArrearsTolerance(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getRepaymentEveryBoolean()) {
                    this.loanConfigurableAttributes.setRepaymentEvery(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName)
                        .getAsBoolean() != this.loanConfigurableAttributes.getGraceOnPrincipalAndInterestPaymentBoolean()) {
                    this.loanConfigurableAttributes.setGraceOnPrincipalAndInterestPayment(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME)
                        .getAsBoolean() != this.loanConfigurableAttributes.getGraceOnArrearsAgingBoolean()) {
                    this.loanConfigurableAttributes.setGraceOnArrearsAgeing(
                            command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).getAsBoolean());
                }
            } else {
                this.loanConfigurableAttributes = LoanProductConfigurableAttributes.populateDefaultsForConfigurableAttributes();
                this.loanConfigurableAttributes.updateLoanProduct(this);
            }
        }

        if (actualChanges.containsKey(LoanProductConstants.holdGuaranteeFundsParamName)) {
            if (this.holdGuaranteeFunds) {
                this.loanProductGuaranteeDetails = LoanProductGuaranteeDetails.createFrom(command);
                this.loanProductGuaranteeDetails.updateProduct(this);
                actualChanges.put(LoanProductConstants.mandatoryGuaranteeParamName,
                        this.loanProductGuaranteeDetails.getMandatoryGuarantee());
                actualChanges.put(LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
                        this.loanProductGuaranteeDetails.getMinimumGuaranteeFromGuarantor());
                actualChanges.put(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName,
                        this.loanProductGuaranteeDetails.getMinimumGuaranteeFromOwnFunds());
            } else {
                this.loanProductGuaranteeDetails = null;
            }

        } else if (this.holdGuaranteeFunds) {
            this.loanProductGuaranteeDetails.update(command, actualChanges);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName,
                this.principalThresholdForLastInstallment)) {
            BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName);
            actualChanges.put(LoanProductConstants.principalThresholdForLastInstallmentParamName, newValue);
            this.principalThresholdForLastInstallment = newValue;
        }
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
                this.accountMovesOutOfNPAOnlyOnArrearsCompletion)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(
                    LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME);
            actualChanges.put(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME, newValue);
            this.accountMovesOutOfNPAOnlyOnArrearsCompletion = newValue;
        }
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.canDefineEmiAmountParamName, this.canDefineInstallmentAmount)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canDefineEmiAmountParamName);
            actualChanges.put(LoanProductConstants.canDefineEmiAmountParamName, newValue);
            this.canDefineInstallmentAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamedWithNullCheck(LoanProductConstants.installmentAmountInMultiplesOfParamName,
                this.installmentAmountInMultiplesOf)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName);
            actualChanges.put(LoanProductConstants.installmentAmountInMultiplesOfParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.installmentAmountInMultiplesOf = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP, this.canUseForTopup)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP);
            actualChanges.put(LoanProductConstants.CAN_USE_FOR_TOPUP, newValue);
            this.canUseForTopup = newValue;
        }

        if (command.hasParameter(LoanProductConstants.RATES_PARAM_NAME)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(LoanProductConstants.RATES_PARAM_NAME);
            if (jsonArray != null) {
                actualChanges.put(LoanProductConstants.RATES_PARAM_NAME, command.jsonFragment(LoanProductConstants.RATES_PARAM_NAME));
            }
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName,
                this.fixedPrincipalPercentagePerInstallment)) {
            BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName);
            actualChanges.put(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, newValue);
            this.fixedPrincipalPercentagePerInstallment = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS,
                this.disallowExpectedDisbursements)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS);
            actualChanges.put(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS, newValue);
            this.disallowExpectedDisbursements = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED,
                this.allowApprovedDisbursedAmountsOverApplied)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED);
            actualChanges.put(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED, newValue);
            this.allowApprovedDisbursedAmountsOverApplied = newValue;
        }

        if (command.isChangeInStringParameterNamed(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE, this.overAppliedCalculationType)) {
            final String newValue = command.stringValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE);
            actualChanges.put(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE, newValue);
            this.overAppliedCalculationType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER, this.overAppliedNumber)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER);
            actualChanges.put(LoanProductConstants.OVER_APPLIED_NUMBER, newValue);
            actualChanges.put("locale", localeAsInput);
            this.overAppliedNumber = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT, this.dueDaysForRepaymentEvent)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT);
            actualChanges.put(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT, newValue);
            actualChanges.put("locale", localeAsInput);
            this.dueDaysForRepaymentEvent = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT,
                this.overDueDaysForRepaymentEvent)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT);
            actualChanges.put(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT, newValue);
            actualChanges.put("locale", localeAsInput);
            this.overDueDaysForRepaymentEvent = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT,
                this.loanProductRelatedDetail.isEnableDownPayment())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT);
            actualChanges.put(LoanProductConstants.ENABLE_DOWN_PAYMENT, newValue);
            this.loanProductRelatedDetail.setEnableDownPayment(newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT,
                this.loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment())) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT);
            actualChanges.put(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, newValue);
            this.loanProductRelatedDetail.setDisbursedAmountPercentageForDownPayment(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT,
                this.loanProductRelatedDetail.isEnableAutoRepaymentForDownPayment())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT);
            actualChanges.put(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, newValue);
            this.loanProductRelatedDetail.setEnableAutoRepaymentForDownPayment(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.REPAYMENT_START_DATE_TYPE,
                this.repaymentStartDateType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.REPAYMENT_START_DATE_TYPE);
            actualChanges.put(LoanProductConstants.REPAYMENT_START_DATE_TYPE, newValue);
            this.repaymentStartDateType = RepaymentStartDateType.fromInt(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY,
                this.isEnableInstallmentLevelDelinquency())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY);
            actualChanges.put(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, newValue);
            this.updateEnableInstallmentLevelDelinquency(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.FIXED_LENGTH, loanProductRelatedDetail.getFixedLength())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.FIXED_LENGTH);
            actualChanges.put(LoanProductConstants.FIXED_LENGTH, newValue);
            loanProductRelatedDetail.setFixedLength(newValue);
        }

        return actualChanges;
    }

    private LoanProductFloatingRates loanProductFloatingRates() {
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
        return AccountingRuleType.NONE.getValue().equals(this.accountingRule);
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.getValue().equals(this.accountingRule);
    }

    public boolean isUpfrontAccrualAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(this.accountingRule);
    }

    public boolean isPeriodicAccrualAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(this.accountingRule);
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
        return this.loanProductTrancheDetails.outstandingLoanBalance();
    }

    public Integer maxTrancheCount() {
        return this.loanProductTrancheDetails.maxTrancheCount();
    }

    public boolean isInterestRecalculationEnabled() {
        return this.loanProductRelatedDetail.isInterestRecalculationEnabled();
    }

    public Integer getMinimumDaysBetweenDisbursalAndFirstRepayment() {
        return this.minimumDaysBetweenDisbursalAndFirstRepayment == null ? 0 : this.minimumDaysBetweenDisbursalAndFirstRepayment;
    }

    public LoanProductBorrowerCycleVariations fetchLoanProductBorrowerCycleVariationById(Long id) {
        LoanProductBorrowerCycleVariations borrowerCycleVariation = null;
        for (LoanProductBorrowerCycleVariations cycleVariation : this.borrowerCycleVariations) {
            if (id.equals(cycleVariation.getId())) {
                borrowerCycleVariation = cycleVariation;
                break;
            }
        }
        return borrowerCycleVariation;
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

    public LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy() {
        LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy = LoanPreClosureInterestCalculationStrategy.NONE;
        if (this.isInterestRecalculationEnabled()) {
            preCloseInterestCalculationStrategy = getProductInterestRecalculationDetails().preCloseInterestCalculationStrategy();
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
