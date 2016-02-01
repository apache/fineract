/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.savings.DepositsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;

@Entity
@DiscriminatorValue("300")
public class RecurringDepositProduct extends FixedDepositProduct {

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private DepositProductRecurringDetail recurringDetail;

    protected RecurringDepositProduct() {
        super();
    }

    public static RecurringDepositProduct createNew(final String name, final String shortName, final String description,
            final MonetaryCurrency currency, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final Integer lockinPeriodFrequency,
            final SavingsPeriodFrequencyType lockinPeriodFrequencyType, final AccountingRuleType accountingRuleType,
            final Set<Charge> charges, final DepositProductTermAndPreClosure productTermAndPreClosure,
            final DepositProductRecurringDetail recurringDetail, final Set<InterestRateChart> charts,
            BigDecimal minBalanceForInterestCalculation) {

        final BigDecimal minRequiredOpeningBalance = null;
        final boolean withdrawalFeeApplicableForTransfer = false;
        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = null;

        return new RecurringDepositProduct(name, shortName, description, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, accountingRuleType, charges,
                productTermAndPreClosure, recurringDetail, charts, allowOverdraft, overdraftLimit, minBalanceForInterestCalculation);
    }

    protected RecurringDepositProduct(final String name, final String shortName, final String description, final MonetaryCurrency currency,
            final BigDecimal interestRate, final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final AccountingRuleType accountingRuleType, final Set<Charge> charges,
            final DepositProductTermAndPreClosure productTermAndPreClosure, final DepositProductRecurringDetail recurringDetail,
            final Set<InterestRateChart> charts, final boolean allowOverdraft, final BigDecimal overdraftLimit,
            final BigDecimal minBalanceForInterestCalculation) {

        super(name, shortName, description, currency, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, accountingRuleType, charges, productTermAndPreClosure,
                charts, allowOverdraft, overdraftLimit, minBalanceForInterestCalculation);

        this.recurringDetail = recurringDetail;
    }

    @Override
    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);

        actualChanges.putAll(this.update(command, baseDataValidator));

        validateDomainRules(baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

        return actualChanges;
    }

    @Override
    protected Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        actualChanges.putAll(super.update(command, baseDataValidator));

        if (this.recurringDetail != null) {
            actualChanges.putAll(this.recurringDetail.update(command));
        }

        return actualChanges;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public DepositProductRecurringDetail depositRecurringDetail() {
        return this.recurringDetail;
    }

    @Override
    public void validateDomainRules() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);

        validateDomainRules(baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateDomainRules(final DataValidatorBuilder baseDataValidator) {
        final DepositTermDetail termDetails = this.depositProductTermAndPreClosure().depositTermDetail();
        final boolean isMinTermGreaterThanMax = termDetails.isMinDepositTermGreaterThanMaxDepositTerm();
        if (isMinTermGreaterThanMax) {
            final Integer maxTerm = termDetails.maxDepositTerm();
            baseDataValidator.reset().parameter(maxDepositTermParamName).value(maxTerm)
                    .failWithCodeNoParameterAddedToErrorCode("max.term.lessthan.min.term");
        }

        if (this.charts != null) {
            validateCharts(baseDataValidator);
        } else if (this.nominalAnnualInterestRate == null || this.nominalAnnualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            baseDataValidator.reset().parameter(DepositsApiConstants.nominalAnnualInterestRateParamName).value(nominalAnnualInterestRate)
                    .failWithCodeNoParameterAddedToErrorCode("interest.chart.or.nominal.interest.rate.required");
        }

        this.validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);
    }
}