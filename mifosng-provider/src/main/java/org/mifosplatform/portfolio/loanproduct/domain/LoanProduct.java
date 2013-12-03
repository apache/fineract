/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

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
        @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE") })
public class LoanProduct extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
    private LoanTransactionProcessingStrategy transactionProcessingStrategy;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany
    @JoinTable(name = "m_product_loan_charge", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private List<Charge> charges;

    @Embedded
    private final LoanProductRelatedDetail loanProductRelatedDetail;

    @Embedded
    private LoanProductMinMaxConstraints loanProductMinMaxConstraints;

    @Column(name = "accounting_type", nullable = false)
    private Integer accountingRule;

    @Column(name = "include_in_borrower_cycle")
    private boolean includeInBorrowerCycle;

    @Column(name = "use_borrower_cycle")
    private boolean useBorrowerCycle;

    @Column(name = "start_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "close_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date closeDate;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProduct", orphanRemoval = true)
    private Set<LoanProductBorrowerCycleVariations> borrowerCycleVariations = new HashSet<LoanProductBorrowerCycleVariations>();

    public static LoanProduct assembleFromJson(final Fund fund, final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy,
            final List<Charge> productCharges, final JsonCommand command, final AprCalculator aprCalculator) {

        final String name = command.stringValueOfParameterNamed("name");
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
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.integerValueOfParameterNamed("amortizationType"));
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command
                .integerValueOfParameterNamed("repaymentFrequencyType"));
        final PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.fromInt(command
                .integerValueOfParameterNamed("interestRateFrequencyType"));
        final BigDecimal interestRatePerPeriod = command.bigDecimalValueOfParameterNamed("interestRatePerPeriod");
        final BigDecimal minInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("minInterestRatePerPeriod");
        final BigDecimal maxInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("maxInterestRatePerPeriod");
        final BigDecimal annualInterestRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod);

        final Integer repaymentEvery = command.integerValueOfParameterNamed("repaymentEvery");
        final Integer numberOfRepayments = command.integerValueOfParameterNamed("numberOfRepayments");
        final Integer minNumberOfRepayments = command.integerValueOfParameterNamed("minNumberOfRepayments");
        final Integer maxNumberOfRepayments = command.integerValueOfParameterNamed("maxNumberOfRepayments");
        final BigDecimal inArrearsTolerance = command.bigDecimalValueOfParameterNamed("inArrearsTolerance");

        // grace details
        final Integer graceOnPrincipalPayment = command.integerValueOfParameterNamed("graceOnPrincipalPayment");
        final Integer graceOnInterestPayment = command.integerValueOfParameterNamed("graceOnInterestPayment");
        final Integer graceOnInterestCharged = command.integerValueOfParameterNamed("graceOnInterestCharged");

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));
        final boolean includeInBorrowerCycle = command.booleanPrimitiveValueOfParameterNamed("includeInBorrowerCycle");

        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate closeDate = command.localDateValueOfParameterNamed("closeDate");
        final String externalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        final boolean useBorrowerCycle = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.useBorrowerCycleParameterName);
        final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations = new HashSet<LoanProductBorrowerCycleVariations>();

        if (useBorrowerCycle) {
            populateBorrowerCyclevariations(command, loanProductBorrowerCycleVariations);
        }

        return new LoanProduct(fund, loanTransactionProcessingStrategy, name, description, currency, principal, minPrincipal, maxPrincipal,
                interestRatePerPeriod, minInterestRatePerPeriod, maxInterestRatePerPeriod, interestFrequencyType, annualInterestRate,
                interestMethod, interestCalculationPeriodMethod, repaymentEvery, repaymentFrequencyType, numberOfRepayments,
                minNumberOfRepayments, maxNumberOfRepayments, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged,
                amortizationMethod, inArrearsTolerance, productCharges, accountingRuleType, includeInBorrowerCycle, startDate, closeDate,
                externalId, useBorrowerCycle, loanProductBorrowerCycleVariations);
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
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(20);
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
        List<Long> list = new ArrayList<Long>();
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
            Set<LoanProductBorrowerCycleVariations> remove = new HashSet<LoanProductBorrowerCycleVariations>();
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
            final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultMinPrincipal, final BigDecimal defaultMaxPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final BigDecimal defaultMinNominalInterestRatePerPeriod,
            final BigDecimal defaultMaxNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repayEvery,
            final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments,
            final Integer defaultMinNumberOfInstallments, final Integer defaultMaxNumberOfInstallments,
            final Integer graceOnPrincipalPayment, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance, final List<Charge> charges,
            final AccountingRuleType accountingRuleType, final boolean includeInBorrowerCycle, final LocalDate startDate,
            final LocalDate closeDate, final String externalId, final boolean useBorrowerCycle,
            final Set<LoanProductBorrowerCycleVariations> loanProductBorrowerCycleVariations) {
        this.fund = fund;
        this.transactionProcessingStrategy = transactionProcessingStrategy;
        this.name = name.trim();
        if (StringUtils.isNotBlank(description)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }

        if (charges != null) {
            this.charges = charges;
        }

        this.loanProductRelatedDetail = new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod,
                interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery,
                repaymentFrequencyType, defaultNumberOfInstallments, graceOnPrincipalPayment, graceOnInterestPayment,
                graceOnInterestCharged, amortizationMethod, inArrearsTolerance);

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

    public boolean hasCurrencyCodeOf(final String currencyCode) {
        return this.loanProductRelatedDetail.hasCurrencyCodeOf(currencyCode);
    }

    public boolean update(final List<Charge> newProductCharges) {
        if (newProductCharges == null) { return false; }

        boolean updated = false;
        if (this.charges != null) {
            final Set<Charge> currentSetOfCharges = new HashSet<Charge>(this.charges);
            final Set<Charge> newSetOfCharges = new HashSet<Charge>(newProductCharges);

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

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = this.loanProductRelatedDetail.update(command, aprCalculator);
        actualChanges.putAll(loanProductMinMaxConstraints().update(command));

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

        return actualChanges;
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.getValue().equals(this.accountingRule);
    }

    public boolean isAccrualBasedAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_BASED.getValue().equals(this.accountingRule);
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
        Map<String, BigDecimal> borrowerCycleVariations = new HashMap<String, BigDecimal>();
        borrowerCycleVariations.put(LoanProductConstants.principal, this.loanProductRelatedDetail.getPrincipal().getAmount());
        borrowerCycleVariations.put(LoanProductConstants.minPrincipal, this.loanProductMinMaxConstraints.getMinPrincipal());
        borrowerCycleVariations.put(LoanProductConstants.maxPrincipal, this.loanProductMinMaxConstraints.getMaxPrincipal());
        borrowerCycleVariations.put(LoanProductConstants.interestRatePerPeriod,
                this.loanProductRelatedDetail.getNominalInterestRatePerPeriod());
        borrowerCycleVariations.put(LoanProductConstants.minInterestRatePerPeriod,
                this.loanProductMinMaxConstraints.getMinNominalInterestRatePerPeriod());
        borrowerCycleVariations.put(LoanProductConstants.maxInterestRatePerPeriod,
                this.loanProductMinMaxConstraints.getMaxNominalInterestRatePerPeriod());
        borrowerCycleVariations.put(LoanProductConstants.numberOfRepayments,
                BigDecimal.valueOf(this.loanProductRelatedDetail.getNumberOfRepayments()));
        borrowerCycleVariations.put(LoanProductConstants.minNumberOfRepayments,
                BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMinNumberOfRepayments()));
        borrowerCycleVariations.put(LoanProductConstants.maxNumberOfRepayments,
                BigDecimal.valueOf(this.loanProductMinMaxConstraints.getMaxNumberOfRepayments()));
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
                        && cycleVariation.getValueConditionType().equals(LoanProductValueConditionType.GRETERTHAN)) {
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
}