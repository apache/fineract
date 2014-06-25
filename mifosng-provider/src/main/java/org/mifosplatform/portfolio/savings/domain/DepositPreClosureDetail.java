/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

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
import org.mifosplatform.portfolio.savings.PreClosurePenalInterestOnType;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;

/**
 * DepositPreClosureDetail encapsulates all the details of a
 * {@link FixedDepositProduct} that are also used and persisted by a
 * {@link FixedDepositAccount}.
 */
@Embeddable
public class DepositPreClosureDetail {

    @Column(name = "pre_closure_penal_applicable")
    private boolean preClosurePenalApplicable;

    @Column(name = "pre_closure_penal_interest", scale = 6, precision = 19, nullable = true)
    private BigDecimal preClosurePenalInterest;

    @Column(name = "pre_closure_penal_interest_on_enum", nullable = true)
    private Integer preClosurePenalInterestOnType;

    public static DepositPreClosureDetail createFrom(final boolean preClosurePenalApplicable, final BigDecimal preClosurePenalInterest,
            final PreClosurePenalInterestOnType preClosurePenalInterestType) {

        return new DepositPreClosureDetail(preClosurePenalApplicable, preClosurePenalInterest, preClosurePenalInterestType);
    }

    protected DepositPreClosureDetail() {
        //
    }

    private DepositPreClosureDetail(final boolean preClosurePenalApplicable, final BigDecimal preClosurePenalInterest,
            final PreClosurePenalInterestOnType preClosurePenalInterestType) {
        this.preClosurePenalApplicable = preClosurePenalApplicable;
        this.preClosurePenalInterest = preClosurePenalInterest;
        this.preClosurePenalInterestOnType = (preClosurePenalInterestType == null) ? null : preClosurePenalInterestType.getValue();
    }

    protected Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        final String localeAsInput = command.locale();

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

        return actualChanges;
    }

    public boolean preClosurePenalApplicable() {
        return this.preClosurePenalApplicable;
    }

    public BigDecimal preClosurePenalInterest() {
        return this.preClosurePenalInterest;
    }

    public Integer preClosurePenalInterestOnTypeId() {
        return this.preClosurePenalInterestOnType;
    }

    public PreClosurePenalInterestOnType preClosurePenalInterestOnType() {
        return PreClosurePenalInterestOnType.fromInt(preClosurePenalInterestOnType);
    }

    public DepositPreClosureDetail copy() {
        final boolean preClosurePenalApplicable = this.preClosurePenalApplicable;
        final BigDecimal preClosurePenalInterest = this.preClosurePenalInterest;
        final PreClosurePenalInterestOnType preClosurePenalInterestType = PreClosurePenalInterestOnType
                .fromInt(this.preClosurePenalInterestOnType);

        return DepositPreClosureDetail.createFrom(preClosurePenalApplicable, preClosurePenalInterest, preClosurePenalInterestType);
    }
}