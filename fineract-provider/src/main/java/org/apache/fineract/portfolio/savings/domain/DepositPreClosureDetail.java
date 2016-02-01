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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalApplicableParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalInterestParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.localeParamName;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.savings.PreClosurePenalInterestOnType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;

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