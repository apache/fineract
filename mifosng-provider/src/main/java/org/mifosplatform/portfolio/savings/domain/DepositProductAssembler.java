/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.chartsParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreeFromPeriodParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreePeriodApplicableParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreePeriodFrequencyTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreeToPeriodParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.minDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.minDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalApplicableParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalInterestParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.recurringDepositFrequencyParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.recurringDepositFrequencyTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.recurringDepositTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositMinAmountParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositMaxAmountParamName;
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
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.shortNameParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.mifosplatform.portfolio.interestratechart.InterestRateChartPeriodType;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartAssembler;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.savings.PreClosurePenalInterestOnType;
import org.mifosplatform.portfolio.savings.RecurringDepositType;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Component
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
                lockinPeriodFrequency, lockinPeriodFrequencyType, accountingRuleType, charges, productTermAndPreClosure, charts);

        // update product reference
        productTermAndPreClosure.updateProductReference(fixedDepositProduct);

        validateFixedDepositProductDomainRules(fixedDepositProduct);

        return fixedDepositProduct;
    }

    private void validateFixedDepositProductDomainRules(FixedDepositProduct fixedDepositProduct) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME);
        final DepositTermDetail termDetails = fixedDepositProduct.depositProductTermAndPreClosure().depositTermDetail();
        final boolean isMinTermGreaterThanMax = termDetails.isMinDepositTermGreaterThanMaxDepositTerm();
        if (isMinTermGreaterThanMax) {
            final Integer maxTerm = termDetails.maxDepositTerm();
            baseDataValidator.reset().parameter(maxDepositTermParamName).value(maxTerm)
                    .failWithCodeNoParameterAddedToErrorCode("max.term.lessthan.min.term");
        }
        fixedDepositProduct.validateCharts(baseDataValidator);
        fixedDepositProduct.validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
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
                productTermAndPreClosure, productRecurringDetail, charts);

        // update product reference
        productTermAndPreClosure.updateProductReference(recurringDepositProduct);
        productRecurringDetail.updateProductReference(recurringDepositProduct);

        validateRecurringDepositProductDomainRules(recurringDepositProduct);
        
        return recurringDepositProduct;
    }

    private void validateRecurringDepositProductDomainRules(RecurringDepositProduct recurringDepositProduct) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);
        final DepositTermDetail termDetails = recurringDepositProduct.depositProductTermAndPreClosure().depositTermDetail();
        final boolean isMinTermGreaterThanMax = termDetails.isMinDepositTermGreaterThanMaxDepositTerm();
        if (isMinTermGreaterThanMax) {
            final Integer maxTerm = termDetails.maxDepositTerm();
            baseDataValidator.reset().parameter(maxDepositTermParamName).value(maxTerm)
                    .failWithCodeNoParameterAddedToErrorCode("max.term.lessthan.min.term");
        }
        recurringDepositProduct.validateCharts(baseDataValidator);
        recurringDepositProduct.validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public DepositPreClosureDetail assemblePreClosureDetail(final JsonCommand command) {

        boolean interestFreePeriodApplicable = false;
        Integer interestFreeFromPeriod = null;
        Integer interestFreeToPeriod = null;
        InterestRateChartPeriodType interestFreePeriodFrequencyType = null;

        if (command.parameterExists(interestFreePeriodApplicableParamName)) {
            interestFreePeriodApplicable = command.booleanObjectValueOfParameterNamed(interestFreePeriodApplicableParamName);
            if (interestFreePeriodApplicable) {
                interestFreeFromPeriod = command.integerValueOfParameterNamed(interestFreeFromPeriodParamName);
                interestFreeToPeriod = command.integerValueOfParameterNamed(interestFreeToPeriodParamName);
                final Integer interestFreePreriodTypeId = command.integerValueOfParameterNamed(interestFreePeriodFrequencyTypeIdParamName);
                interestFreePeriodFrequencyType = (interestFreePreriodTypeId == null) ? null : InterestRateChartPeriodType
                        .fromInt(interestFreePreriodTypeId);
            }
        }

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

        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(interestFreePeriodApplicable, interestFreeFromPeriod,
                interestFreeToPeriod, interestFreePeriodFrequencyType, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestType);

        return preClosureDetail;
    }

    public DepositPreClosureDetail assemblePreClosureDetail(final JsonCommand command, DepositPreClosureDetail produPreClosureDetail) {

        boolean interestFreePeriodApplicable = false;
        Integer interestFreeFromPeriod = null;
        Integer interestFreeToPeriod = null;
        InterestRateChartPeriodType interestFreePeriodFrequencyType = null;
        Integer interestFreePreriodTypeId = null;

        if (command.parameterExists(interestFreePeriodApplicableParamName)) {
            interestFreePeriodApplicable = command.booleanObjectValueOfParameterNamed(interestFreePeriodApplicableParamName);
            if (interestFreePeriodApplicable) {
                if (command.parameterExists(interestFreeFromPeriodParamName)) {
                    interestFreeFromPeriod = command.integerValueOfParameterNamed(interestFreeFromPeriodParamName);
                } else {
                    interestFreeFromPeriod = produPreClosureDetail.interestFreeFromPeriod();
                }

                if (command.parameterExists(interestFreeToPeriodParamName)) {
                    interestFreeToPeriod = command.integerValueOfParameterNamed(interestFreeToPeriodParamName);
                } else {
                    interestFreeToPeriod = produPreClosureDetail.interestFreeToPeriod();
                }

                if (command.parameterExists(interestFreePeriodFrequencyTypeIdParamName)) {
                    interestFreePreriodTypeId = command.integerValueOfParameterNamed(interestFreePeriodFrequencyTypeIdParamName);
                } else {
                    interestFreePreriodTypeId = produPreClosureDetail.interestFreePeriodFrequencyType();
                }
                interestFreePeriodFrequencyType = (interestFreePreriodTypeId == null) ? null : InterestRateChartPeriodType
                        .fromInt(interestFreePreriodTypeId);
            }
        } else {
            interestFreePeriodApplicable = produPreClosureDetail.interestFreePeriodApplicable();
            interestFreeFromPeriod = produPreClosureDetail.interestFreeFromPeriod();
            interestFreeToPeriod = produPreClosureDetail.interestFreeToPeriod();
            interestFreePreriodTypeId = produPreClosureDetail.interestFreePeriodFrequencyType();
            interestFreePeriodFrequencyType = (interestFreePreriodTypeId == null) ? null : InterestRateChartPeriodType
                    .fromInt(interestFreePreriodTypeId);
        }

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

        DepositPreClosureDetail preClosureDetail1 = DepositPreClosureDetail.createFrom(interestFreePeriodApplicable,
                interestFreeFromPeriod, interestFreeToPeriod, interestFreePeriodFrequencyType, preClosurePenalApplicable,
                preClosurePenalInterest, preClosurePenalInterestType);

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
        } else if(prodDepositTermDetail != null){
            minDepositTerm = prodDepositTermDetail.minDepositTerm();
        }

        if (command.parameterExists(maxDepositTermParamName)) {
            maxDepositTerm = command.integerValueOfParameterNamed(maxDepositTermParamName);
        } else if(prodDepositTermDetail != null){
            maxDepositTerm = prodDepositTermDetail.maxDepositTerm();
        }

        if (command.parameterExists(minDepositTermTypeIdParamName)) {
            minDepositTermTypeId = command.integerValueOfParameterNamed(minDepositTermTypeIdParamName);
        } else if(prodDepositTermDetail != null){
            minDepositTermTypeId = prodDepositTermDetail.minDepositTermType();
        }

        if (command.parameterExists(maxDepositTermTypeIdParamName)) {
            maxDepositTermTypeId = command.integerValueOfParameterNamed(maxDepositTermTypeIdParamName);
        } else if(prodDepositTermDetail != null){
            maxDepositTermTypeId = prodDepositTermDetail.maxDepositTermType();
        }

        final SavingsPeriodFrequencyType minDepositTermType = (minDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(minDepositTermTypeId);

        final SavingsPeriodFrequencyType maxDepositTermType = (maxDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(maxDepositTermTypeId);

        if (command.parameterExists(inMultiplesOfDepositTermParamName)) {
            inMultiplesOfDepositTerm = command.integerValueOfParameterNamed(inMultiplesOfDepositTermParamName);
        } else if(prodDepositTermDetail != null){
            inMultiplesOfDepositTerm = prodDepositTermDetail.inMultiplesOfDepositTerm();
        }

        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            inMultiplesOfDepositTermTypeId = command.integerValueOfParameterNamed(inMultiplesOfDepositTermTypeIdParamName);
        } else if(prodDepositTermDetail != null){
            inMultiplesOfDepositTermTypeId = prodDepositTermDetail.inMultiplesOfDepositTermType();
        }

        final SavingsPeriodFrequencyType inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null
                : SavingsPeriodFrequencyType.fromInt(inMultiplesOfDepositTermTypeId);

        final DepositTermDetail depositTermDetail = DepositTermDetail.createFrom(minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType);

        return depositTermDetail;
    }

    public DepositRecurringDetail assembleRecurringDetail(final JsonCommand command) {

        RecurringDepositType recurringDepositType = null;
        final Integer recurringDepositTypeValue = command.integerValueOfParameterNamed(recurringDepositTypeIdParamName);
        if (recurringDepositTypeValue != null) {
            recurringDepositType = RecurringDepositType.fromInt(recurringDepositTypeValue);
        }

        SavingsPeriodFrequencyType recurringDepositFrequencyType = null;
        final Integer recurringDepositFrequencyTypeValue = command.integerValueOfParameterNamed(recurringDepositFrequencyTypeIdParamName);
        if (recurringDepositFrequencyTypeValue != null) {
            recurringDepositFrequencyType = SavingsPeriodFrequencyType.fromInt(recurringDepositFrequencyTypeValue);
        }

        final Integer recurringDepositFrequency = command.integerValueOfParameterNamed(recurringDepositFrequencyParamName);

        final DepositRecurringDetail depositRecurringDetail = DepositRecurringDetail.createFrom(recurringDepositType,
                recurringDepositFrequency, recurringDepositFrequencyType);

        return depositRecurringDetail;
    }

    public Set<Charge> assembleListOfSavingsProductCharges(final JsonCommand command, final String savingsProductCurrencyCode) {

        final Set<Charge> charges = new HashSet<Charge>();

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
        final Set<InterestRateChart> charts = new HashSet<InterestRateChart>();

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

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
    public DepositProductAmountDetails assembleDepositAmountDetails(final JsonCommand command) {
        
        BigDecimal minDepositAmount = null;
        if(command.parameterExists(depositMinAmountParamName)) {
                minDepositAmount = command.bigDecimalValueOfParameterNamed(depositMinAmountParamName);
        }
        
        BigDecimal maxDepositAmount = null;
        if(command.parameterExists(depositMaxAmountParamName)) {
                maxDepositAmount = command.bigDecimalValueOfParameterNamed(depositMaxAmountParamName);
        }
        
        BigDecimal depositAmount = null;
        if(command.parameterExists(depositAmountParamName)) {
                depositAmount = command.bigDecimalValueOfParameterNamed(depositAmountParamName);
        }
        
        final DepositProductAmountDetails depositRecurringDetail = new DepositProductAmountDetails(minDepositAmount, 
                        depositAmount, maxDepositAmount);

        return depositRecurringDetail;
    }
}