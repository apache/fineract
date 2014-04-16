/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreeFromPeriodParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreePeriodApplicableParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreePeriodFrequencyTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.interestFreeToPeriodParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalApplicableParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.preClosurePenalInterestParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.localeParamName;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.portfolio.interestratechart.InterestRateChartPeriodType;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartEnumerations;
import org.mifosplatform.portfolio.savings.PreClosurePenalInterestOnType;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;

/**
 * DepositPreClosureDetail encapsulates all the details of a
 * {@link FixedDepositProduct} that are also used and persisted by a
 * {@link FixedDepositAccount}.
 */
@Embeddable
public class DepositPreClosureDetail {

    @Column(name = "interest_free_period_applicable")
    private boolean interestFreePeriodApplicable;

    @Column(name = "pre_closure_penal_applicable")
    private boolean preClosurePenalApplicable;

    @Column(name = "interest_free_from_period", nullable = true)
    private Integer interestFreeFromPeriod;

    @Column(name = "interest_free_to_period", nullable = true)
    private Integer interestFreeToPeriod;

    @Column(name = "interest_free_period_frequency_enum", nullable = true)
    private Integer interestFreePeriodFrequencyType;

    @Column(name = "pre_closure_penal_interest", scale = 6, precision = 19, nullable = true)
    private BigDecimal preClosurePenalInterest;

    @Column(name = "pre_closure_penal_interest_on_enum", nullable = true)
    private Integer preClosurePenalInterestOnType;

    public static DepositPreClosureDetail createFrom(final boolean interestFreePeriodApplicable, final Integer interestFreeFromPeriod,
            final Integer interestFreeToPeriod, final InterestRateChartPeriodType periodType, final boolean preClosurePenalApplicable,
            final BigDecimal preClosurePenalInterest, final PreClosurePenalInterestOnType preClosurePenalInterestType) {

        return new DepositPreClosureDetail(interestFreePeriodApplicable, interestFreeFromPeriod, interestFreeToPeriod, periodType,
                preClosurePenalApplicable, preClosurePenalInterest, preClosurePenalInterestType);
    }

    protected DepositPreClosureDetail() {
        //
    }

    private DepositPreClosureDetail(final boolean interestFreePeriodApplicable, final Integer interestFreeFromPeriod,
            final Integer interestFreeToPeriod, final InterestRateChartPeriodType periodType, final boolean preClosurePenalApplicable,
            final BigDecimal preClosurePenalInterest, final PreClosurePenalInterestOnType preClosurePenalInterestType) {
        this.interestFreePeriodApplicable = interestFreePeriodApplicable;
        this.interestFreeFromPeriod = interestFreeFromPeriod;
        this.interestFreeToPeriod = interestFreeToPeriod;
        this.interestFreePeriodFrequencyType = (periodType == null) ? null : periodType.getValue();
        this.preClosurePenalApplicable = preClosurePenalApplicable;
        this.preClosurePenalInterest = preClosurePenalInterest;
        this.preClosurePenalInterestOnType = (preClosurePenalInterestType == null) ? null : preClosurePenalInterestType.getValue();
    }

    protected Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(10);

        final String localeAsInput = command.locale();

        if (command.isChangeInBooleanParameterNamed(interestFreePeriodApplicableParamName, this.interestFreePeriodApplicable)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(interestFreePeriodApplicableParamName);
            actualChanges.put(interestFreePeriodApplicableParamName, newValue);
            this.interestFreePeriodApplicable = newValue;
        }

        if (this.interestFreePeriodApplicable) {

            if (command.isChangeInIntegerParameterNamed(interestFreeFromPeriodParamName, this.interestFreeFromPeriod)) {
                final Integer newValue = command.integerValueOfParameterNamed(interestFreeFromPeriodParamName);
                actualChanges.put(interestFreeFromPeriodParamName, newValue);
                this.interestFreeFromPeriod = newValue;
            }

            if (command.isChangeInIntegerParameterNamed(interestFreeToPeriodParamName, this.interestFreeToPeriod)) {
                final Integer newValue = command.integerValueOfParameterNamed(interestFreeToPeriodParamName);
                actualChanges.put(interestFreeToPeriodParamName, newValue);
                this.interestFreeToPeriod = newValue;
            }

            if (command.isChangeInIntegerParameterNamed(interestFreePeriodFrequencyTypeIdParamName, this.interestFreePeriodFrequencyType)) {
                final Integer newValue = command.integerValueOfParameterNamed(interestFreePeriodFrequencyTypeIdParamName);
                actualChanges.put(interestFreePeriodFrequencyTypeIdParamName, InterestRateChartEnumerations.periodType(newValue));
                this.interestFreePeriodFrequencyType = newValue;
            }

            if (this.interestFreeFromPeriod == null) {
                baseDataValidator.parameter(interestFreeFromPeriodParamName).value(this.interestFreeFromPeriod)
                        .cantBeBlankWhenParameterProvidedIs(interestFreePeriodApplicableParamName, this.interestFreePeriodApplicable);
            }

            if (this.interestFreeToPeriod == null) {
                baseDataValidator.parameter(interestFreeToPeriodParamName).value(this.interestFreeToPeriod)
                        .cantBeBlankWhenParameterProvidedIs(interestFreePeriodApplicableParamName, this.interestFreePeriodApplicable);
            }

            if (this.interestFreePeriodFrequencyType == null) {
                baseDataValidator.parameter(interestFreePeriodFrequencyTypeIdParamName).value(this.interestFreePeriodFrequencyType)
                        .cantBeBlankWhenParameterProvidedIs(interestFreePeriodApplicableParamName, this.interestFreePeriodApplicable);
            }
        } else { // reset if interest free period is not applicable
            this.interestFreeFromPeriod = null;
            this.interestFreeToPeriod = null;
            this.interestFreePeriodFrequencyType = null;
        }

        if (command.isChangeInBooleanParameterNamed(preClosurePenalApplicableParamName, this.preClosurePenalApplicable)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(preClosurePenalApplicableParamName);
            actualChanges.put(preClosurePenalApplicableParamName, newValue);
            this.preClosurePenalApplicable = newValue;
        }

        if (this.preClosurePenalApplicable) {
            if (command.isChangeInBigDecimalParameterNamed(preClosurePenalInterestParamName, this.preClosurePenalInterest)) {
                final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(preClosurePenalInterestParamName);
                actualChanges.put(preClosurePenalInterestParamName, newValue);
                actualChanges.put(localeParamName, localeAsInput);
                this.preClosurePenalInterest = newValue;
            }

            if (command.isChangeInIntegerParameterNamed(preClosurePenalInterestOnTypeIdParamName, this.preClosurePenalInterestOnType)) {
                final Integer newValue = command.integerValueOfParameterNamed(preClosurePenalInterestOnTypeIdParamName);
                actualChanges.put(preClosurePenalInterestOnTypeIdParamName, SavingsEnumerations.preClosurePenaltyInterestOnType(newValue));
                actualChanges.put(localeParamName, localeAsInput);
                this.preClosurePenalInterestOnType = newValue;
            }

            if (this.preClosurePenalInterest == null) {
                baseDataValidator.parameter(preClosurePenalInterestParamName).value(this.preClosurePenalInterest)
                        .cantBeBlankWhenParameterProvidedIs(preClosurePenalApplicableParamName, this.preClosurePenalApplicable);
            }

            if (this.preClosurePenalInterestOnType == null) {
                baseDataValidator.parameter(preClosurePenalInterestOnTypeIdParamName).value(this.preClosurePenalInterestOnType)
                        .cantBeBlankWhenParameterProvidedIs(preClosurePenalApplicableParamName, this.preClosurePenalApplicable);
            }
        } else { // reset if pre-closure penal interest is not applicable
            this.preClosurePenalInterest = null;
            this.preClosurePenalInterestOnType = null;
        }

        if (isFromPeriodAfterToPeriod()) {
            baseDataValidator
                    .parameter(interestFreeFromPeriodParamName)
                    .value(this.interestFreeFromPeriod)
                    .failWithCode("interest.free.from.period.is.greater.than.to.period", this.interestFreeFromPeriod,
                            this.interestFreeToPeriod);
        }

        return actualChanges;
    }

    private boolean isFromPeriodAfterToPeriod() {
        if (this.interestFreePeriodApplicable && this.interestFreeFromPeriod != null && this.interestFreeToPeriod != null) {
            if (this.interestFreeFromPeriod.compareTo(interestFreeToPeriod) > 0) { return true; }
        }
        return false;
    }

    public boolean interestFreePeriodApplicable() {
        return this.interestFreePeriodApplicable;
    }

    public boolean preClosurePenalApplicable() {
        return this.preClosurePenalApplicable;
    }

    public Integer interestFreeFromPeriod() {
        return this.interestFreeFromPeriod;
    }

    public Integer interestFreeToPeriod() {
        return this.interestFreeToPeriod;
    }

    public Integer interestFreePeriodFrequencyType() {
        return this.interestFreePeriodFrequencyType;
    }

    public BigDecimal preClosurePenalInterest() {
        return this.preClosurePenalInterest;
    }

    public Integer preClosurePenalInterestOnTypeId() {
        return this.preClosurePenalInterestOnType;
    }

    public PreClosurePenalInterestOnType preClosurePenalInterestOnType(){
        return PreClosurePenalInterestOnType.fromInt(preClosurePenalInterestOnType);
    }
    
    public DepositPreClosureDetail copy(){
        final boolean interestFreePeriodApplicable = this.interestFreePeriodApplicable;
        final Integer interestFreeFromPeriod = this.interestFreeFromPeriod;
        final Integer interestFreeToPeriod = this.interestFreeToPeriod;
        final boolean preClosurePenalApplicable = this.preClosurePenalApplicable;
        final BigDecimal preClosurePenalInterest = this.preClosurePenalInterest;
        final InterestRateChartPeriodType periodType = InterestRateChartPeriodType.fromInt(this.interestFreePeriodFrequencyType);
        final PreClosurePenalInterestOnType preClosurePenalInterestType = PreClosurePenalInterestOnType.fromInt(this.preClosurePenalInterestOnType);
        
        return DepositPreClosureDetail.createFrom(interestFreePeriodApplicable, interestFreeFromPeriod, interestFreeToPeriod, periodType, preClosurePenalApplicable, preClosurePenalInterest, preClosurePenalInterestType);
    }
}