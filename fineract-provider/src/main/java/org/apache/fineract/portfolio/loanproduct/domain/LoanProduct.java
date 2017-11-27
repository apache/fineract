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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Loan products allow for categorisation of an organisations loans into
 * something meaningful to them.
 * 
 * They provide a means of simplifying creation/maintenance of loans. They can
 * also allow for product comparison to take place when reporting.
 * 
 * They allow for constraints to be added at product level.
 */
@Entity
@Table(name = "m_product_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE"),
        @UniqueConstraint(columnNames = { "short_name" }, name = "unq_short_name") })
public class LoanProduct extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
    private LoanTransactionProcessingStrategy transactionProcessingStrategy;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", nullable = false, unique = true)
    private String shortName;

    @Column(name = "description")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_product_loan_charge", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private List<Charge> charges;

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
    private LoanProductTrancheDetails loanProducTrancheDetails;

    @Column(name = "start_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "close_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date closeDate;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<LoanProductBorrowerCycleVariations> borrowerCycleVariations = new HashSet<>();

    @Column(name = "overdue_days_for_npa", nullable = true)
    private Integer overdueDaysForNPA;

    @Column(name = "min_days_between_disbursal_and_first_repayment", nullable = true)
    private Integer minimumDaysBetweenDisbursalAndFirstRepayment;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch=FetchType.EAGER)
    private LoanProductInterestRecalculationDetails productInterestRecalculationDetails;

    @Column(name = "hold_guarantee_funds")
    private boolean holdGuaranteeFunds;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch=FetchType.EAGER)
    private LoanProductGuaranteeDetails loanProductGuaranteeDetails;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true)
    private LoanProductConfigurableAttributes loanConfigurableAttributes;

    @Column(name = "principal_threshold_for_last_installment", scale = 2, precision = 5, nullable = false)
    private BigDecimal principalThresholdForLastInstallment;

    @Column(name = "account_moves_out_of_npa_only_on_arrears_completion")
    private boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;

    @Column(name = "can_define_fixed_emi_amount")
    private boolean canDefineInstallmentAmount;

    @Column(name = "instalment_amount_in_multiples_of", nullable = true)
    private Integer installmentAmountInMultiplesOf;

    @Column(name = "is_linked_to_floating_interest_rates", nullable = false)
    private boolean isLinkedToFloatingInterestRate;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch=FetchType.EAGER)
    private LoanProductFloatingRates floatingRates;

    @Column(name = "allow_variabe_installments", nullable = false)
    private boolean allowVariabeInstallments;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loanProduct", optional = true, orphanRemoval = true, fetch=FetchType.EAGER)
    private LoanProductVariableInstallmentConfig variableInstallmentConfig;
    
    @Column(name = "sync_expected_with_disbursement_date")
    private boolean syncExpectedWithDisbursementDate;


    @Column(name = "can_use_for_topup", nullable = false)
    private boolean canUseForTopup = false;
    
    @Column(name = "is_equal_amortization", nullable = false)
    private boolean isEqualAmortization = false;

    public static LoanProduct assembleFromJson(final Fund fund, final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy,
            final List<Charge> productCharges, final JsonCommand command, final AprCalculator aprCalculator, FloatingRate floatingRate) {

        final String name = command.stringValueOfParameterNamed("name");
        final String shortName = command.stringValueOfParameterNamed(LoanProductConstants.shortName);
        final String description = command.stringValueOfParameterNamed("description");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");
        final Integer inMultiplesOf = command.integerValueOfParameterNamed("inMultiplesOf");

        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed("principal");
        final BigDecimal minPrincipal = command.bigDecimalValueOfParameterNamed("minPrincipal");
        final BigDecimal maxPrincipal = command.bigDecimalValueOfParameterNamed("maxPrincipal");

        final InterestMethod interestMethod = InterestMethod.fromInt(command.integerValueOfParameterNamed("interestType"));
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command
                .integerValueOfParameterNamed("interestCalculationPeriodType"));
        final boolean allowPartialPeriodInterestCalcualtion = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName);
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.integerValueOfParameterNamed("amortizationType"));
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command
                .integerValueOfParameterNamed("repaymentFrequencyType"));
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
            annualInterestRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod);
        }

        final Boolean isVariableInstallmentsAllowed = command
                .booleanObjectValueOfParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName);
        if (isVariableInstallmentsAllowed != null && isVariableInstallmentsAllowed) {
            minimumGapBetweenInstallments = command.integerValueOfParameterNamed(LoanProductConstants.minimumGapBetweenInstallments);
            maximumGapBetweenInstallments = command.integerValueOfParameterNamed(LoanProductConstants.maximumGapBetweenInstallments);
        }

        final Integer repaymentEvery = command.integerValueOfParameterNamed("repaymentEvery");
        final Integer numberOfRepayments = command.integerValueOfParameterNamed("numberOfRepayments");
        final Integer minNumberOfRepayments = command.integerValueOfParameterNamed("minNumberOfRepayments");
        final Integer maxNumberOfRepayments = command.integerValueOfParameterNamed("maxNumberOfRepayments");
        final BigDecimal inArrearsTolerance = command.bigDecimalValueOfParameterNamed("inArrearsTolerance");

        // grace details
        final Integer graceOnPrincipalPayment = command.integerValueOfParameterNamed("graceOnPrincipalPayment");
        final Integer recurringMoratoriumOnPrincipalPeriods = command.integerValueOfParameterNamed("recurringMoratoriumOnPrincipalPeriods");
        final Integer graceOnInterestPayment = command.integerValueOfParameterNamed("graceOnInterestPayment");
        final Integer graceOnInterestCharged = command.integerValueOfParameterNamed("graceOnInterestCharged");
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = command
                .integerValueOfParameterNamed(LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment);

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));
        final boolean includeInBorrowerCycle = command.booleanPrimitiveValueOfParameterNamed("includeInBorrowerCycle");

        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate closeDate = command.localDateValueOfParameterNamed("closeDate");
        final String externalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final boolean useBorrowerCycle = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.useBorrowerCycleParameterName);
        final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations = new HashSet<>();

        if (useBorrowerCycle) {
            populateBorrowerCyclevariations(command, loanProductBorrowerCycleVariations);
        }

        final boolean multiDisburseLoan = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.multiDisburseLoanParameterName);
        Integer maxTrancheCount = null;
        BigDecimal outstandingLoanBalance = null;
        if (multiDisburseLoan) {
            outstandingLoanBalance = command.bigDecimalValueOfParameterNamed(LoanProductConstants.outstandingLoanBalanceParameterName);
            maxTrancheCount = command.integerValueOfParameterNamed(LoanProductConstants.maxTrancheCountParameterName);
        }

        final Integer graceOnArrearsAgeing = command.integerValueOfParameterNamed(LoanProductConstants.graceOnArrearsAgeingParameterName);

        final Integer overdueDaysForNPA = command.integerValueOfParameterNamed(LoanProductConstants.overdueDaysForNPAParameterName);

        // Interest recalculation settings
        final boolean isInterestRecalculationEnabled = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isInterestRecalculationEnabledParameterName);
        final DaysInMonthType daysInMonthType = DaysInMonthType.fromInt(command
                .integerValueOfParameterNamed(LoanProductConstants.daysInMonthTypeParameterName));

        final DaysInYearType daysInYearType = DaysInYearType.fromInt(command
                .integerValueOfParameterNamed(LoanProductConstants.daysInYearTypeParameterName));

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
            principalThresholdForLastInstallment = multiDisburseLoan ? LoanProductConstants.DEFAULT_PRINCIPAL_THRESHOLD_FOR_MULTI_DISBURSE_LOAN
                    : LoanProductConstants.DEFAULT_PRINCIPAL_THRESHOLD_FOR_SINGLE_DISBURSE_LOAN;
        }
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName);
        final boolean canDefineEmiAmount = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canDefineEmiAmountParamName);
        final Integer installmentAmountInMultiplesOf = command
                .integerValueOfParameterNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName);

        final boolean syncExpectedWithDisbursementDate = command.booleanPrimitiveValueOfParameterNamed("syncExpectedWithDisbursementDate");
        
        
		final boolean canUseForTopup = command.parameterExists(LoanProductConstants.canUseForTopup)
				? command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canUseForTopup)
				: false;
				
        final boolean isEqualAmortization = command.parameterExists(LoanProductConstants.isEqualAmortizationParam) ? command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isEqualAmortizationParam) : false;

        return new LoanProduct(fund, loanTransactionProcessingStrategy, name, shortName, description, currency, principal, minPrincipal,
                maxPrincipal, interestRatePerPeriod, minInterestRatePerPeriod, maxInterestRatePerPeriod, interestFrequencyType,
                annualInterestRate, interestMethod, interestCalculationPeriodMethod, allowPartialPeriodInterestCalcualtion, repaymentEvery,
                repaymentFrequencyType, numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods,
                graceOnInterestPayment, graceOnInterestCharged, amortizationMethod, inArrearsTolerance, productCharges, accountingRuleType,
                includeInBorrowerCycle, startDate, closeDate, externalId, useBorrowerCycle, loanProductBorrowerCycleVariations,
                multiDisburseLoan, maxTrancheCount, outstandingLoanBalance, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType,
                daysInYearType, isInterestRecalculationEnabled, interestRecalculationSettings,
                minimumDaysBetweenDisbursalAndFirstRepayment, holdGuarantorFunds, loanProductGuaranteeDetails,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineEmiAmount,
                installmentAmountInMultiplesOf, loanConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRate,
                interestRateDifferential, minDifferentialLendingRate, maxDifferentialLendingRate, defaultDifferentialLendingRate,
                isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGapBetweenInstallments,
                maximumGapBetweenInstallments, syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization);

    }

    public void updateLoanProductInRelatedClasses() {
        if (this.isInterestRecalculationEnabled()) {
            this.productInterestRecalculationDetails.updateProduct(this);
        }
        if (this.holdGuaranteeFunds) {
            this.loanProductGuaranteeDetails.updateProduct(this);
        }
    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void populateBorrowerCyclevariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assemblePrincipalVariations(command, loanProductBorrowerCycleVariations);

        assembleRepaymentVariations(command, loanProductBorrowerCycleVariations);

        assembleInterestRateVariations(command, loanProductBorrowerCycleVariations);
    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void assembleInterestRateVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVaritions(command, loanProductBorrowerCycleVariations, LoanProductParamType.INTERESTRATE.getValue(),
                LoanProductConstants.interestRateVariationsForBorrowerCycleParameterName);

    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void assembleRepaymentVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVaritions(command, loanProductBorrowerCycleVariations, LoanProductParamType.REPAYMENT.getValue(),
                LoanProductConstants.numberOfRepaymentVariationsForBorrowerCycleParameterName);

    }

    /**
     * @param command
     * @param loanProductBorrowerCycleVariations
     */
    private static void assemblePrincipalVariations(final JsonCommand command,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        assembleVaritions(command, loanProductBorrowerCycleVariations, LoanProductParamType.PRINCIPAL.getValue(),
                LoanProductConstants.principalVariationsForBorrowerCycleParameterName);
    }

    private static void assembleVaritions(final JsonCommand command,
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
                    if (jsonObject.has(LoanProductConstants.defaultValueParameterName)
                            && jsonObject.get(LoanProductConstants.defaultValueParameterName).isJsonPrimitive()) {
                        defaultValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.defaultValueParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.minValueParameterName)
                            && jsonObject.get(LoanProductConstants.minValueParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanProductConstants.minValueParameterName).getAsString()))) {
                        minValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.minValueParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.maxValueParameterName)
                            && jsonObject.get(LoanProductConstants.maxValueParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanProductConstants.maxValueParameterName).getAsString()))) {
                        maxValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.maxValueParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.borrowerCycleNumberParamName)
                            && jsonObject.get(LoanProductConstants.borrowerCycleNumberParamName).isJsonPrimitive()) {
                        cycleNumber = jsonObject.getAsJsonPrimitive(LoanProductConstants.borrowerCycleNumberParamName).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.valueConditionTypeParamName)
                            && jsonObject.get(LoanProductConstants.valueConditionTypeParamName).isJsonPrimitive()) {
                        valueUsageCondition = jsonObject.getAsJsonPrimitive(LoanProductConstants.valueConditionTypeParamName).getAsInt();
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
        updateBorrowerCycleVaritions(command, LoanProductParamType.PRINCIPAL.getValue(),
                LoanProductConstants.principalVariationsForBorrowerCycleParameterName, actualChanges, variationIds);
        updateBorrowerCycleVaritions(command, LoanProductParamType.INTERESTRATE.getValue(),
                LoanProductConstants.interestRateVariationsForBorrowerCycleParameterName, actualChanges, variationIds);
        updateBorrowerCycleVaritions(command, LoanProductParamType.REPAYMENT.getValue(),
                LoanProductConstants.numberOfRepaymentVariationsForBorrowerCycleParameterName, actualChanges, variationIds);
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

    private void updateBorrowerCycleVaritions(final JsonCommand command, Integer paramType, String variationParameterName,
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
                    if (jsonObject.has(LoanProductConstants.defaultValueParameterName)
                            && jsonObject.get(LoanProductConstants.defaultValueParameterName).isJsonPrimitive()) {
                        defaultValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.defaultValueParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.minValueParameterName)
                            && jsonObject.get(LoanProductConstants.minValueParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanProductConstants.minValueParameterName).getAsString()))) {
                        minValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.minValueParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.maxValueParameterName)
                            && jsonObject.get(LoanProductConstants.maxValueParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanProductConstants.maxValueParameterName).getAsString()))) {
                        maxValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.maxValueParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.borrowerCycleNumberParamName)
                            && jsonObject.get(LoanProductConstants.borrowerCycleNumberParamName).isJsonPrimitive()) {
                        cycleNumber = jsonObject.getAsJsonPrimitive(LoanProductConstants.borrowerCycleNumberParamName).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.valueConditionTypeParamName)
                            && jsonObject.get(LoanProductConstants.valueConditionTypeParamName).isJsonPrimitive()) {
                        valueUsageCondition = jsonObject.getAsJsonPrimitive(LoanProductConstants.valueConditionTypeParamName).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.borrowerCycleIdParameterName)
                            && jsonObject.get(LoanProductConstants.borrowerCycleIdParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanProductConstants.borrowerCycleIdParameterName).getAsString()))) {
                        id = jsonObject.getAsJsonPrimitive(LoanProductConstants.borrowerCycleIdParameterName).getAsLong();
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

    private void clearVariations(LoanProductParamType paramType, boolean clearAll) {
        if (clearAll) {
            this.borrowerCycleVariations.clear();
        } else {
            Set<LoanProductBorrowerCycleVariations> remove = new HashSet<>();
            for (LoanProductBorrowerCycleVariations borrowerCycleVariations : this.borrowerCycleVariations) {
                if (paramType.equals(borrowerCycleVariations.getParamType())) {
                    remove.add(borrowerCycleVariations);
                }
            }
            this.borrowerCycleVariations.removeAll(remove);
        }
    }

    protected LoanProduct() {
        this.loanProductRelatedDetail = null;
        this.loanProductMinMaxConstraints = null;
    }

    public LoanProduct(final Fund fund, final LoanTransactionProcessingStrategy transactionProcessingStrategy, final String name,
            final String shortName, final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultMinPrincipal, final BigDecimal defaultMaxPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final BigDecimal defaultMinNominalInterestRatePerPeriod,
            final BigDecimal defaultMaxNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean considerPartialPeriodInterest,
            final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments,
            final Integer defaultMinNumberOfInstallments, final Integer defaultMaxNumberOfInstallments,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance, final List<Charge> charges, final AccountingRuleType accountingRuleType,
            final boolean includeInBorrowerCycle, final LocalDate startDate, final LocalDate closeDate, final String externalId, final boolean useBorrowerCycle,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations, final boolean multiDisburseLoan, final Integer maxTrancheCount, final BigDecimal outstandingLoanBalance,
            final Integer graceOnArrearsAgeing, final Integer overdueDaysForNPA, final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType,
            final boolean isInterestRecalculationEnabled,
            final LoanProductInterestRecalculationDetails productInterestRecalculationDetails,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final boolean holdGuarantorFunds,
            final LoanProductGuaranteeDetails loanProductGuaranteeDetails, final BigDecimal principalThresholdForLastInstallment,
            final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion, final boolean canDefineEmiAmount,
            final Integer installmentAmountInMultiplesOf, final LoanProductConfigurableAttributes loanProductConfigurableAttributes,
            Boolean isLinkedToFloatingInterestRates, FloatingRate floatingRate, BigDecimal interestRateDifferential,
            BigDecimal minDifferentialLendingRate, BigDecimal maxDifferentialLendingRate, BigDecimal defaultDifferentialLendingRate,
            Boolean isFloatingInterestRateCalculationAllowed, final Boolean isVariableInstallmentsAllowed,
            final Integer minimumGapBetweenInstallments, final Integer maximumGapBetweenInstallments,
            final boolean syncExpectedWithDisbursementDate, final boolean canUseForTopup, final boolean isEqualAmortization) {
        this.fund = fund;
        this.transactionProcessingStrategy = transactionProcessingStrategy;
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

        this.isLinkedToFloatingInterestRate = isLinkedToFloatingInterestRates == null ? false : isLinkedToFloatingInterestRates;
        if (isLinkedToFloatingInterestRate) {
            this.floatingRates = new LoanProductFloatingRates(floatingRate, this, interestRateDifferential, minDifferentialLendingRate,
                    maxDifferentialLendingRate, defaultDifferentialLendingRate, isFloatingInterestRateCalculationAllowed);
        }

        this.allowVariabeInstallments = isVariableInstallmentsAllowed == null ? false : isVariableInstallmentsAllowed;

        if (allowVariabeInstallments) {
            this.variableInstallmentConfig = new LoanProductVariableInstallmentConfig(this, minimumGapBetweenInstallments,
                    maximumGapBetweenInstallments);
        }

        this.loanProductRelatedDetail = new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod,
                interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod,
                considerPartialPeriodInterest, repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods,
                graceOnInterestPayment, graceOnInterestCharged, amortizationMethod, inArrearsTolerance, graceOnArrearsAgeing,
                daysInMonthType.getValue(), daysInYearType.getValue(), isInterestRecalculationEnabled, isEqualAmortization);

        this.loanProductRelatedDetail.validateRepaymentPeriodWithGraceSettings();

        this.loanProductMinMaxConstraints = new LoanProductMinMaxConstraints(defaultMinPrincipal, defaultMaxPrincipal,
                defaultMinNominalInterestRatePerPeriod, defaultMaxNominalInterestRatePerPeriod, defaultMinNumberOfInstallments,
                defaultMaxNumberOfInstallments);

        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }
        this.includeInBorrowerCycle = includeInBorrowerCycle;
        this.useBorrowerCycle = useBorrowerCycle;

        if (startDate != null) {
            this.startDate = startDate.toDateTimeAtStartOfDay().toDate();
        }

        if (closeDate != null) {
            this.closeDate = closeDate.toDateTimeAtStartOfDay().toDate();
        }

        this.externalId = externalId;
        this.borrowerCycleVariations = loanProductBorrowerCycleVariations;
        for (LoanProductBorrowerCycleVariations borrowerCycleVariations : this.borrowerCycleVariations) {
            borrowerCycleVariations.updateLoanProduct(this);
        }
        if (loanProductConfigurableAttributes != null) {
            this.loanConfigurableAttributes = loanProductConfigurableAttributes;
            loanConfigurableAttributes.updateLoanProduct(this);
        }

        this.loanProducTrancheDetails = new LoanProductTrancheDetails(multiDisburseLoan, maxTrancheCount, outstandingLoanBalance);
        this.overdueDaysForNPA = overdueDaysForNPA;
        this.productInterestRecalculationDetails = productInterestRecalculationDetails;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = minimumDaysBetweenDisbursalAndFirstRepayment;
        this.holdGuaranteeFunds = holdGuarantorFunds;
        this.loanProductGuaranteeDetails = loanProductGuaranteeDetails;
        this.principalThresholdForLastInstallment = principalThresholdForLastInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion;
        this.canDefineInstallmentAmount = canDefineEmiAmount;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.syncExpectedWithDisbursementDate = 
        		syncExpectedWithDisbursementDate;
        this.canUseForTopup = canUseForTopup;
        this.isEqualAmortization = isEqualAmortization;
    }

    public MonetaryCurrency getCurrency() {
        return this.loanProductRelatedDetail.getCurrency();
    }

    public void update(final Fund fund) {
        this.fund = fund;
    }

    public void update(final LoanTransactionProcessingStrategy strategy) {
        this.transactionProcessingStrategy = strategy;
    }

    public LoanTransactionProcessingStrategy getRepaymentStrategy() {
        return this.transactionProcessingStrategy;
    }

    public boolean hasCurrencyCodeOf(final String currencyCode) {
        return this.loanProductRelatedDetail.hasCurrencyCodeOf(currencyCode);
    }

    public boolean update(final List<Charge> newProductCharges) {
        if (newProductCharges == null) { return false; }

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

    public Integer getAccountingType() {
        return this.accountingRule;
    }

    public List<Charge> getLoanProductCharges() {
        return this.charges;
    }

    public void update(final LoanProductConfigurableAttributes loanConfigurableAttributes) {
        this.loanConfigurableAttributes = loanConfigurableAttributes;
    }

    public LoanProductConfigurableAttributes getLoanProductConfigurableAttributes() {
        return this.loanConfigurableAttributes;
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

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName, this.allowVariabeInstallments)) {
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

        final String shortNameParamName = LoanProductConstants.shortName;
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

        Long existingStrategyId = null;
        if (this.transactionProcessingStrategy != null) {
            existingStrategyId = this.transactionProcessingStrategy.getId();
        }
        final String transactionProcessingStrategyParamName = "transactionProcessingStrategyId";
        if (command.isChangeInLongParameterNamed(transactionProcessingStrategyParamName, existingStrategyId)) {
            final Long newValue = command.longValueOfParameterNamed(transactionProcessingStrategyParamName);
            actualChanges.put(transactionProcessingStrategyParamName, newValue);
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

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.useBorrowerCycleParameterName, this.useBorrowerCycle)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.useBorrowerCycleParameterName);
            actualChanges.put(LoanProductConstants.useBorrowerCycleParameterName, newValue);
            this.useBorrowerCycle = newValue;
        }

        if (this.useBorrowerCycle) {
            actualChanges.putAll(updateBorrowerCycleVariations(command));
        } else {
            clearVariations(null, true);
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

            final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
            if (newValue != null) {
                this.startDate = newValue.toDate();
            } else {
                this.startDate = null;
            }
        }

        final String closeDateParamName = "closeDate";
        if (command.isChangeInLocalDateParameterNamed(closeDateParamName, getCloseDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(closeDateParamName);
            actualChanges.put(closeDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(closeDateParamName);
            if (newValue != null) {
                this.closeDate = newValue.toDate();
            } else {
                this.closeDate = null;
            }
        }

        final String externalIdTypeParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdTypeParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdTypeParamName);
            actualChanges.put(accountingTypeParamName, newValue);
            this.externalId = newValue;
        }
        loanProducTrancheDetails.update(command, actualChanges, localeAsInput);

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.overdueDaysForNPAParameterName, this.overdueDaysForNPA)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.overdueDaysForNPAParameterName);
            actualChanges.put(LoanProductConstants.overdueDaysForNPAParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.overdueDaysForNPA = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment,
                this.minimumDaysBetweenDisbursalAndFirstRepayment)) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment);
            actualChanges.put(LoanProductConstants.minimumDaysBetweenDisbursalAndFirstRepayment, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minimumDaysBetweenDisbursalAndFirstRepayment = newValue;
        }
        
        if(command.isChangeInBooleanParameterNamed("syncExpectedWithDisbursementDate"
        		, this.syncExpectedWithDisbursementDate)){
        	final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("syncExpectedWithDisbursementDate");
        	actualChanges.put("syncExpectedWithDisbursementDate", newValue);
        	this.syncExpectedWithDisbursementDate = newValue;
        }

        /**
         * Update interest recalculation settings
         */
        final boolean isInterestRecalculationEnabledChanged = actualChanges
                .containsKey(LoanProductConstants.isInterestRecalculationEnabledParameterName);

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
            if (!command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName).isJsonNull()) {
                actualChanges.put(configurableAttributesChanges, command.jsonFragment(configurableAttributesChanges));

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName).getAsBoolean() != this.loanConfigurableAttributes
                        .getAmortizationBoolean()) {
                    this.loanConfigurableAttributes.setAmortizationType(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName).getAsBoolean() != this.loanConfigurableAttributes
                        .getInterestMethodBoolean()) {
                    this.loanConfigurableAttributes.setInterestType(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyIdParamName).getAsBoolean() != this.loanConfigurableAttributes
                        .getTransactionProcessingStrategyBoolean()) {
                    this.loanConfigurableAttributes.setTransactionProcessingStrategyId(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyIdParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName).getAsBoolean() != this.loanConfigurableAttributes
                        .getInterestCalcPeriodBoolean()) {
                    this.loanConfigurableAttributes.setInterestCalculationPeriodType(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName).getAsBoolean() != this.loanConfigurableAttributes
                        .getArrearsToleranceBoolean()) {
                    this.loanConfigurableAttributes.setInArrearsTolerance(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName).getAsBoolean() != this.loanConfigurableAttributes
                        .getRepaymentEveryBoolean()) {
                    this.loanConfigurableAttributes.setRepaymentEvery(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName).getAsBoolean() != this.loanConfigurableAttributes
                        .getGraceOnPrincipalAndInterestPaymentBoolean()) {
                    this.loanConfigurableAttributes.setGraceOnPrincipalAndInterestPayment(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.graceOnArrearsAgeingParameterName).getAsBoolean() != this.loanConfigurableAttributes
                        .getGraceOnArrearsAgingBoolean()) {
                    this.loanConfigurableAttributes.setGraceOnArrearsAgeing(command.parsedJson().getAsJsonObject()
                            .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                            .getAsJsonPrimitive(LoanProductConstants.graceOnArrearsAgeingParameterName).getAsBoolean());
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
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName,
                this.accountMovesOutOfNPAOnlyOnArrearsCompletion)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName);
            actualChanges.put(LoanProductConstants.accountMovesOutOfNPAOnlyOnArrearsCompletionParamName, newValue);
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

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.canUseForTopup, this.canUseForTopup)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canUseForTopup);
            actualChanges.put(LoanProductConstants.canUseForTopup, newValue);
            this.canUseForTopup = newValue;
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

    public boolean isAccrualBasedAccountingEnabled() {
        return isUpfrontAccrualAccountingEnabled() || isPeriodicAccrualAccountingEnabled();
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
        this.loanProductMinMaxConstraints = this.loanProductMinMaxConstraints == null ? new LoanProductMinMaxConstraints(null, null, null,
                null, null, null) : this.loanProductMinMaxConstraints;
        return this.loanProductMinMaxConstraints;
    }

    public boolean isIncludeInBorrowerCycle() {
        return this.includeInBorrowerCycle;
    }

    public LocalDate getStartDate() {
        LocalDate startLocalDate = null;
        if (this.startDate != null) {
            startLocalDate = LocalDate.fromDateFields(this.startDate);
        }
        return startLocalDate;
    }

    public LocalDate getCloseDate() {
        LocalDate closeLocalDate = null;
        if (this.closeDate != null) {
            closeLocalDate = LocalDate.fromDateFields(this.closeDate);
        }
        return closeLocalDate;
    }

    public String productName() {
        return this.name;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public boolean useBorrowerCycle() {
        return this.useBorrowerCycle;
    }

    public boolean isMultiDisburseLoan() {
        return this.loanProducTrancheDetails.isMultiDisburseLoan();
    }

    public BigDecimal outstandingLoanBalance() {
        return this.loanProducTrancheDetails.outstandingLoanBalance();
    }

    public Integer maxTrancheCount() {
        return this.loanProducTrancheDetails.maxTrancheCount();
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
    
    public boolean syncExpectedWithDisbursementDate() {
		return syncExpectedWithDisbursementDate;
	}

	public void setSyncExpectedWithDisbursementDate(boolean syncExpectedWithDisbursementDate) {
		this.syncExpectedWithDisbursementDate = syncExpectedWithDisbursementDate;
	}

	public Map<String, BigDecimal> fetchBorrowerCycleVariationsForCycleNumber(final Integer cycleNumber) {
        Map<String, BigDecimal> borrowerCycleVariations = new HashMap<>();
        borrowerCycleVariations.put(LoanProductConstants.principal, this.loanProductRelatedDetail.getPrincipal().getAmount());
        borrowerCycleVariations.put(LoanProductConstants.interestRatePerPeriod,
                this.loanProductRelatedDetail.getNominalInterestRatePerPeriod());
        if (this.loanProductRelatedDetail.getNumberOfRepayments() != null) {
            borrowerCycleVariations.put(LoanProductConstants.numberOfRepayments,
                    BigDecimal.valueOf(this.loanProductRelatedDetail.getNumberOfRepayments()));
        }

        if (this.loanProductMinMaxConstraints != null) {
            borrowerCycleVariations.put(LoanProductConstants.minPrincipal, this.loanProductMinMaxConstraints.getMinPrincipal());
            borrowerCycleVariations.put(LoanProductConstants.maxPrincipal, this.loanProductMinMaxConstraints.getMaxPrincipal());
            borrowerCycleVariations.put(LoanProductConstants.minInterestRatePerPeriod,
                    this.loanProductMinMaxConstraints.getMinNominalInterestRatePerPeriod());
            borrowerCycleVariations.put(LoanProductConstants.maxInterestRatePerPeriod,
                    this.loanProductMinMaxConstraints.getMaxNominalInterestRatePerPeriod());

            if (this.loanProductMinMaxConstraints.getMinNumberOfRepayments() != null) {
                borrowerCycleVariations.put(LoanProductConstants.minNumberOfRepayments,
                        BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMinNumberOfRepayments()));
            }

            if (this.loanProductMinMaxConstraints.getMaxNumberOfRepayments() != null) {
                borrowerCycleVariations.put(LoanProductConstants.maxNumberOfRepayments,
                        BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMaxNumberOfRepayments()));
            }
        }
        if (cycleNumber > 0) {
            Integer principalCycleUsed = 0;
            Integer interestCycleUsed = 0;
            Integer repaymentCycleUsed = 0;
            for (LoanProductBorrowerCycleVariations cycleVariation : this.borrowerCycleVariations) {
                if (cycleVariation.getBorrowerCycleNumber() == cycleNumber
                        && cycleVariation.getValueConditionType().equals(LoanProductValueConditionType.EQUAL)) {
                    switch (cycleVariation.getParamType()) {
                        case PRINCIPAL:
                            borrowerCycleVariations.put(LoanProductConstants.principal, cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.minPrincipal, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.maxPrincipal, cycleVariation.getMaxValue());
                            principalCycleUsed = cycleVariation.getBorrowerCycleNumber();
                        break;
                        case INTERESTRATE:
                            borrowerCycleVariations.put(LoanProductConstants.interestRatePerPeriod, cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.minInterestRatePerPeriod, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.maxInterestRatePerPeriod, cycleVariation.getMaxValue());
                            interestCycleUsed = cycleVariation.getBorrowerCycleNumber();
                        break;
                        case REPAYMENT:
                            borrowerCycleVariations.put(LoanProductConstants.numberOfRepayments, cycleVariation.getDefaultValue());
                            borrowerCycleVariations.put(LoanProductConstants.minNumberOfRepayments, cycleVariation.getMinValue());
                            borrowerCycleVariations.put(LoanProductConstants.maxNumberOfRepayments, cycleVariation.getMaxValue());
                            repaymentCycleUsed = cycleVariation.getBorrowerCycleNumber();
                        break;
                        default:
                        break;
                    }
                } else if (cycleVariation.getBorrowerCycleNumber() < cycleNumber
                        && cycleVariation.getValueConditionType().equals(LoanProductValueConditionType.GREATERTHAN)) {
                    switch (cycleVariation.getParamType()) {
                        case PRINCIPAL:
                            if (principalCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.principal, cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.minPrincipal, cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.maxPrincipal, cycleVariation.getMaxValue());
                                principalCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                        break;
                        case INTERESTRATE:
                            if (interestCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.interestRatePerPeriod, cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.minInterestRatePerPeriod, cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.maxInterestRatePerPeriod, cycleVariation.getMaxValue());
                                interestCycleUsed = cycleVariation.getBorrowerCycleNumber();
                            }
                        break;
                        case REPAYMENT:
                            if (repaymentCycleUsed < cycleVariation.getBorrowerCycleNumber()) {
                                borrowerCycleVariations.put(LoanProductConstants.numberOfRepayments, cycleVariation.getDefaultValue());
                                borrowerCycleVariations.put(LoanProductConstants.minNumberOfRepayments, cycleVariation.getMinValue());
                                borrowerCycleVariations.put(LoanProductConstants.maxNumberOfRepayments, cycleVariation.getMaxValue());
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

    public LoanProductInterestRecalculationDetails getProductInterestRecalculationDetails() {
        return this.productInterestRecalculationDetails;
    }

    public boolean isHoldGuaranteeFundsEnabled() {
        return this.holdGuaranteeFunds;
    }

    public LoanProductGuaranteeDetails getLoanProductGuaranteeDetails() {
        return this.loanProductGuaranteeDetails;
    }

    public String getShortName() {
        return this.shortName;
    }

    public BigDecimal getPrincipalThresholdForLastInstallment() {
        return this.principalThresholdForLastInstallment;
    }

    public boolean isArrearsBasedOnOriginalSchedule() {
        boolean isBasedOnOriginalSchedule = false;
        if (getProductInterestRecalculationDetails() != null) {
            isBasedOnOriginalSchedule = getProductInterestRecalculationDetails().isArrearsBasedOnOriginalSchedule();
        }
        return isBasedOnOriginalSchedule;
    }

    public boolean canDefineInstallmentAmount() {
        return this.canDefineInstallmentAmount;
    }

    public Integer getInstallmentAmountInMultiplesOf() {
        return this.installmentAmountInMultiplesOf;
    }

    public LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy() {
        LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy = LoanPreClosureInterestCalculationStrategy.NONE;
        if (this.isInterestRecalculationEnabled()) {
            preCloseInterestCalculationStrategy = getProductInterestRecalculationDetails().preCloseInterestCalculationStrategy();
        }
        return preCloseInterestCalculationStrategy;
    }

    public LoanProductRelatedDetail getLoanProductRelatedDetail() {
        return loanProductRelatedDetail;
    }

    public boolean isLinkedToFloatingInterestRate() {
        return this.isLinkedToFloatingInterestRate;
    }

    public LoanProductFloatingRates getFloatingRates() {
        return this.floatingRates;
    }

    public Collection<FloatingRatePeriodData> fetchInterestRates(final FloatingRateDTO floatingRateDTO) {
        Collection<FloatingRatePeriodData> applicableRates = new ArrayList<>(1);
        if (isLinkedToFloatingInterestRate()) {
            applicableRates = getFloatingRates().fetchInterestRates(floatingRateDTO);
        }
        return applicableRates;
    }

    public boolean allowVariabeInstallments() {
        return this.allowVariabeInstallments;
    }

    public boolean canUseForTopup(){
        return this.canUseForTopup;
    }

    public boolean isEqualAmortization() {
        return isEqualAmortization;
    }

    public void setEqualAmortization(boolean isEqualAmortization) {
        this.isEqualAmortization = isEqualAmortization;
    }

}