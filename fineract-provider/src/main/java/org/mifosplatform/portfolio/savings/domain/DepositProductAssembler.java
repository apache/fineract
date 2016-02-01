/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.allowWithdrawalParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.chartsParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositMaxAmountParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositMinAmountParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.isMandatoryDepositParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.minDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.minDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalApplicableParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalInterestParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargesParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.idParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.shortNameParamName;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartAssembler;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.savings.PreClosurePenalInterestOnType;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class DepositProductAssembler {

    private final ChargeRepositoryWrapper chargeRepository;
    private final InterestRateChartAssembler chartAssembler;

    @Autowired
    public DepositProductAssembler(final ChargeRepositoryWrapper chargeRepository, final InterestRateChartAssembler chartAssembler) {
        this.chargeRepository = chargeRepository;
        this.chartAssembler = chartAssembler;
    }

    public FixedDepositProduct assembleFixedDepositProduct(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed(nameParamName);
        final String shortName = command.stringValueOfParameterNamed(shortNameParamName);
        final String description = command.stringValueOfParameterNamed(descriptionParamName);

        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
        final Integer inMultiplesOf = command.integerValueOfParameterNamed(inMultiplesOfParamName);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);

        BigDecimal interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        }

        SavingsPostingInterestPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(interestPostingPeriodTypeValue);
        }

        SavingsInterestCalculationType interestCalculationType = null;
        final Integer interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        if (interestCalculationTypeValue != null) {
            interestCalculationType = SavingsInterestCalculationType.fromInt(interestCalculationTypeValue);
        }

        SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType = null;
        final Integer interestCalculationDaysInYearTypeValue = command
                .integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        if (interestCalculationDaysInYearTypeValue != null) {
            interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(interestCalculationDaysInYearTypeValue);
        }

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamedDefaultToNullIfZero(lockinPeriodFrequencyParamName);
        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        final BigDecimal minBalanceForInterestCalculation = command
                .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minBalanceForInterestCalculationParamName);

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));

        final DepositPreClosureDetail preClosureDetail = this.assemblePreClosureDetail(command);
        final DepositTermDetail depositTermDetail = this.assembleDepositTermDetail(command);
        final DepositProductAmountDetails depositProductAmountDetails = this.assembleDepositAmountDetails(command);
        final DepositProductTermAndPreClosure productTermAndPreClosure = DepositProductTermAndPreClosure.createNew(preClosureDetail,
                depositTermDetail, depositProductAmountDetails, null);

        // Savings product charges
        final Set<Charge> charges = assembleListOfSavingsProductCharges(command, currencyCode);
        // Interest rate charts
        final Set<InterestRateChart> charts = assembleListOfCharts(command, currency.getCode());
        if (interestRate == null) {
            interestRate = BigDecimal.ZERO;
        }
        FixedDepositProduct fixedDepositProduct = FixedDepositProduct.createNew(name, shortName, description, currency, interestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                lockinPeriodFrequency, lockinPeriodFrequencyType, accountingRuleType, charges, productTermAndPreClosure, charts,
                minBalanceForInterestCalculation);

        // update product reference
        productTermAndPreClosure.updateProductReference(fixedDepositProduct);

        fixedDepositProduct.validateDomainRules();

        return fixedDepositProduct;
    }

    public RecurringDepositProduct assembleRecurringDepositProduct(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed(nameParamName);
        final String shortName = command.stringValueOfParameterNamed(shortNameParamName);
        final String description = command.stringValueOfParameterNamed(descriptionParamName);

        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
        final Integer inMultiplesOf = command.integerValueOfParameterNamed(inMultiplesOfParamName);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);

        BigDecimal interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        }

        SavingsPostingInterestPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(interestPostingPeriodTypeValue);
        }

        SavingsInterestCalculationType interestCalculationType = null;
        final Integer interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        if (interestCalculationTypeValue != null) {
            interestCalculationType = SavingsInterestCalculationType.fromInt(interestCalculationTypeValue);
        }

        SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType = null;
        final Integer interestCalculationDaysInYearTypeValue = command
                .integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        if (interestCalculationDaysInYearTypeValue != null) {
            interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(interestCalculationDaysInYearTypeValue);
        }

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamedDefaultToNullIfZero(lockinPeriodFrequencyParamName);
        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        final BigDecimal minBalanceForInterestCalculation = command
                .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minBalanceForInterestCalculationParamName);

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));

        final DepositPreClosureDetail preClosureDetail = this.assemblePreClosureDetail(command);
        final DepositTermDetail depositTermDetail = this.assembleDepositTermDetail(command);
        final DepositProductAmountDetails depositProductAmountDetails = this.assembleDepositAmountDetails(command);
        final DepositProductTermAndPreClosure productTermAndPreClosure = DepositProductTermAndPreClosure.createNew(preClosureDetail,
                depositTermDetail, depositProductAmountDetails, null);
        final DepositRecurringDetail recurringDetail = this.assembleRecurringDetail(command);
        final DepositProductRecurringDetail productRecurringDetail = DepositProductRecurringDetail.createNew(recurringDetail, null);

        // Savings product charges
        final Set<Charge> charges = assembleListOfSavingsProductCharges(command, currencyCode);
        // Interest rate charts
        final Set<InterestRateChart> charts = assembleListOfCharts(command, currency.getCode());

        if (interestRate == null) {
            interestRate = BigDecimal.ZERO;
        }

        RecurringDepositProduct recurringDepositProduct = RecurringDepositProduct.createNew(name, shortName, description, currency,
                interestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, lockinPeriodFrequency, lockinPeriodFrequencyType, accountingRuleType, charges,
                productTermAndPreClosure, productRecurringDetail, charts, minBalanceForInterestCalculation);

        // update product reference
        productTermAndPreClosure.updateProductReference(recurringDepositProduct);
        productRecurringDetail.updateProductReference(recurringDepositProduct);

        recurringDepositProduct.validateDomainRules();

        return recurringDepositProduct;
    }

    public DepositPreClosureDetail assemblePreClosureDetail(final JsonCommand command) {

        boolean preClosurePenalApplicable = false;
        BigDecimal preClosurePenalInterest = null;
        PreClosurePenalInterestOnType preClosurePenalInterestType = null;

        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            preClosurePenalApplicable = command.booleanObjectValueOfParameterNamed(preClosurePenalApplicableParamName);
            if (preClosurePenalApplicable) {
                preClosurePenalInterest = command.bigDecimalValueOfParameterNamed(preClosurePenalInterestParamName);
                final Integer preClosurePenalInterestOnTypeId = command
                        .integerValueOfParameterNamed(preClosurePenalInterestOnTypeIdParamName);
                preClosurePenalInterestType = preClosurePenalInterestOnTypeId == null ? null : PreClosurePenalInterestOnType
                        .fromInt(preClosurePenalInterestOnTypeId);
            }
        }

        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestType);

        return preClosureDetail;
    }

    public DepositPreClosureDetail assemblePreClosureDetail(final JsonCommand command, DepositPreClosureDetail produPreClosureDetail) {
        boolean preClosurePenalApplicable = false;
        BigDecimal preClosurePenalInterest = null;
        PreClosurePenalInterestOnType preClosurePenalInterestType = null;
        Integer preClosurePenalInterestOnTypeId = null;
        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            preClosurePenalApplicable = command.booleanObjectValueOfParameterNamed(preClosurePenalApplicableParamName);
            if (preClosurePenalApplicable) {
                if (command.parameterExists(preClosurePenalInterestParamName)) {
                    preClosurePenalInterest = command.bigDecimalValueOfParameterNamed(preClosurePenalInterestParamName);
                } else {
                    preClosurePenalInterest = produPreClosureDetail.preClosurePenalInterest();
                }

                if (command.parameterExists(preClosurePenalInterestParamName)) {
                    preClosurePenalInterestOnTypeId = command.integerValueOfParameterNamed(preClosurePenalInterestOnTypeIdParamName);
                } else {
                    preClosurePenalInterestOnTypeId = produPreClosureDetail.preClosurePenalInterestOnTypeId();
                }
            }
        } else {
            preClosurePenalApplicable = produPreClosureDetail.preClosurePenalApplicable();
            preClosurePenalInterest = produPreClosureDetail.preClosurePenalInterest();
            preClosurePenalInterestOnTypeId = produPreClosureDetail.preClosurePenalInterestOnTypeId();
        }

        preClosurePenalInterestType = preClosurePenalInterestOnTypeId == null ? null : PreClosurePenalInterestOnType
                .fromInt(preClosurePenalInterestOnTypeId);

        DepositPreClosureDetail preClosureDetail1 = DepositPreClosureDetail.createFrom(preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestType);

        return preClosureDetail1;
    }

    public DepositTermDetail assembleDepositTermDetail(final JsonCommand command) {

        final Integer minDepositTerm = command.integerValueOfParameterNamed(minDepositTermParamName);
        final Integer maxDepositTerm = command.integerValueOfParameterNamed(maxDepositTermParamName);
        final Integer minDepositTermTypeId = command.integerValueOfParameterNamed(minDepositTermTypeIdParamName);
        final SavingsPeriodFrequencyType minDepositTermType = (minDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(minDepositTermTypeId);
        final Integer maxDepositTermTypeId = command.integerValueOfParameterNamed(maxDepositTermTypeIdParamName);
        final SavingsPeriodFrequencyType maxDepositTermType = (maxDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(maxDepositTermTypeId);
        final Integer inMultiplesOfDepositTerm = command.integerValueOfParameterNamed(inMultiplesOfDepositTermParamName);
        final Integer inMultiplesOfDepositTermTypeId = command.integerValueOfParameterNamed(inMultiplesOfDepositTermTypeIdParamName);
        final SavingsPeriodFrequencyType inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null
                : SavingsPeriodFrequencyType.fromInt(inMultiplesOfDepositTermTypeId);

        final DepositTermDetail depositTermDetail = DepositTermDetail.createFrom(minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType);

        return depositTermDetail;
    }

    public DepositTermDetail assembleDepositTermDetail(final JsonCommand command, final DepositTermDetail prodDepositTermDetail) {

        Integer minDepositTerm = null;
        Integer maxDepositTerm = null;
        Integer minDepositTermTypeId = null;
        Integer maxDepositTermTypeId = null;
        Integer inMultiplesOfDepositTerm = null;
        Integer inMultiplesOfDepositTermTypeId = null;

        if (command.parameterExists(minDepositTermParamName)) {
            minDepositTerm = command.integerValueOfParameterNamed(minDepositTermParamName);
        } else if (prodDepositTermDetail != null) {
            minDepositTerm = prodDepositTermDetail.minDepositTerm();
        }

        if (command.parameterExists(maxDepositTermParamName)) {
            maxDepositTerm = command.integerValueOfParameterNamed(maxDepositTermParamName);
        } else if (prodDepositTermDetail != null) {
            maxDepositTerm = prodDepositTermDetail.maxDepositTerm();
        }

        if (command.parameterExists(minDepositTermTypeIdParamName)) {
            minDepositTermTypeId = command.integerValueOfParameterNamed(minDepositTermTypeIdParamName);
        } else if (prodDepositTermDetail != null) {
            minDepositTermTypeId = prodDepositTermDetail.minDepositTermType();
        }

        if (command.parameterExists(maxDepositTermTypeIdParamName)) {
            maxDepositTermTypeId = command.integerValueOfParameterNamed(maxDepositTermTypeIdParamName);
        } else if (prodDepositTermDetail != null) {
            maxDepositTermTypeId = prodDepositTermDetail.maxDepositTermType();
        }

        final SavingsPeriodFrequencyType minDepositTermType = (minDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(minDepositTermTypeId);

        final SavingsPeriodFrequencyType maxDepositTermType = (maxDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(maxDepositTermTypeId);

        if (command.parameterExists(inMultiplesOfDepositTermParamName)) {
            inMultiplesOfDepositTerm = command.integerValueOfParameterNamed(inMultiplesOfDepositTermParamName);
        } else if (prodDepositTermDetail != null) {
            inMultiplesOfDepositTerm = prodDepositTermDetail.inMultiplesOfDepositTerm();
        }

        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            inMultiplesOfDepositTermTypeId = command.integerValueOfParameterNamed(inMultiplesOfDepositTermTypeIdParamName);
        } else if (prodDepositTermDetail != null) {
            inMultiplesOfDepositTermTypeId = prodDepositTermDetail.inMultiplesOfDepositTermType();
        }

        final SavingsPeriodFrequencyType inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null
                : SavingsPeriodFrequencyType.fromInt(inMultiplesOfDepositTermTypeId);

        final DepositTermDetail depositTermDetail = DepositTermDetail.createFrom(minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType);

        return depositTermDetail;
    }

    public DepositRecurringDetail assembleRecurringDetail(final JsonCommand command) {

        Boolean isMandatoryDeposit = command.booleanObjectValueOfParameterNamed(isMandatoryDepositParamName);
        Boolean allowWithdrawal = command.booleanObjectValueOfParameterNamed(allowWithdrawalParamName);
        Boolean adjustAdvanceTowardsFuturePayments = command
                .booleanObjectValueOfParameterNamed(adjustAdvanceTowardsFuturePaymentsParamName);

        if (isMandatoryDeposit == null) isMandatoryDeposit = false;
        if (allowWithdrawal == null) allowWithdrawal = false;
        if (adjustAdvanceTowardsFuturePayments == null) adjustAdvanceTowardsFuturePayments = false;

        final DepositRecurringDetail depositRecurringDetail = DepositRecurringDetail.createFrom(isMandatoryDeposit, allowWithdrawal,
                adjustAdvanceTowardsFuturePayments);

        return depositRecurringDetail;
    }

    public Set<Charge> assembleListOfSavingsProductCharges(final JsonCommand command, final String savingsProductCurrencyCode) {

        final Set<Charge> charges = new HashSet<>();

        if (command.parameterExists(chargesParamName)) {
            final JsonArray chargesArray = command.arrayOfParameterNamed(chargesParamName);
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has(idParamName)) {
                        final Long id = jsonObject.get(idParamName).getAsLong();

                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);

                        if (!charge.isSavingsCharge()) {
                            final String errorMessage = "Charge with identifier " + charge.getId()
                                    + " cannot be applied to Savings product.";
                            throw new ChargeCannotBeAppliedToException("savings.product", errorMessage, charge.getId());
                        }

                        if (!savingsProductCurrencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Savings Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.savings.product", errorMessage);
                        }
                        charges.add(charge);
                    }
                }
            }
        }

        return charges;
    }

    private Set<InterestRateChart> assembleListOfCharts(JsonCommand command, String currencyCode) {
        final Set<InterestRateChart> charts = new HashSet<>();

        if (command.parameterExists(chartsParamName)) {
            final JsonArray chartsArray = command.arrayOfParameterNamed(chartsParamName);
            if (chartsArray != null) {
                for (int i = 0; i < chartsArray.size(); i++) {
                    final JsonObject interstRateChartElement = chartsArray.get(i).getAsJsonObject();
                    InterestRateChart chart = this.chartAssembler.assembleFrom(interstRateChartElement, currencyCode);
                    charts.add(chart);
                }
            }
        }
        return charts;
    }

    public DepositProductAmountDetails assembleDepositAmountDetails(final JsonCommand command) {

        BigDecimal minDepositAmount = null;
        if (command.parameterExists(depositMinAmountParamName)) {
            minDepositAmount = command.bigDecimalValueOfParameterNamed(depositMinAmountParamName);
        }

        BigDecimal maxDepositAmount = null;
        if (command.parameterExists(depositMaxAmountParamName)) {
            maxDepositAmount = command.bigDecimalValueOfParameterNamed(depositMaxAmountParamName);
        }

        BigDecimal depositAmount = null;
        if (command.parameterExists(depositAmountParamName)) {
            depositAmount = command.bigDecimalValueOfParameterNamed(depositAmountParamName);
        }

        final DepositProductAmountDetails depositRecurringDetail = new DepositProductAmountDetails(minDepositAmount, depositAmount,
                maxDepositAmount);

        return depositRecurringDetail;
    }
}