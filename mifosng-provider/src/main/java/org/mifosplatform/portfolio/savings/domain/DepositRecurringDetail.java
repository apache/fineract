/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.recurringDepositFrequencyParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.recurringDepositFrequencyTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.recurringDepositTypeIdParamName;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.portfolio.savings.RecurringDepositType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;

/**
 * DepositRecurringDetail encapsulates all the details of a
 * {@link RecurringDepositProduct} that are also used and persisted by a
 * {@link RecurringDepositAccount}.
 */
@Embeddable
public class DepositRecurringDetail {

    @Column(name = "recurring_deposit_type_enum", nullable = true)
    private Integer recurringDepositType;

    @Column(name = "recurring_deposit_frequency", nullable = true)
    private Integer recurringDepositFrequency;

    @Column(name = "recurring_deposit_frequency_type_enum", nullable = true)
    private Integer recurringDepositFrequencyType;

    public static DepositRecurringDetail createFrom(final RecurringDepositType recurringDepositType,
            final Integer recurringDepositFrequency, final SavingsPeriodFrequencyType recurringDepositFrequencyType) {

        return new DepositRecurringDetail(recurringDepositType, recurringDepositFrequency, recurringDepositFrequencyType);
    }

    protected DepositRecurringDetail() {
        //
    }

    private DepositRecurringDetail(final RecurringDepositType recurringDepositType, final Integer recurringDepositFrequency,
            final SavingsPeriodFrequencyType recurringDepositFrequencyType) {
        this.recurringDepositFrequency = recurringDepositFrequency;
        this.recurringDepositFrequencyType = recurringDepositFrequencyType.getValue();
        this.recurringDepositType = recurringDepositType.getValue();
    }

    public Map<String, Object> update(final JsonCommand command, @SuppressWarnings("unused") final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(10);

        if (command.isChangeInIntegerParameterNamed(recurringDepositFrequencyParamName, this.recurringDepositFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringDepositFrequencyParamName);
            actualChanges.put(recurringDepositFrequencyParamName, newValue);
            this.recurringDepositFrequency = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(recurringDepositFrequencyTypeIdParamName, this.recurringDepositFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringDepositFrequencyTypeIdParamName);
            actualChanges.put(recurringDepositFrequencyTypeIdParamName, SavingsEnumerations.recurringDepositFrequencyType(newValue));
            this.recurringDepositFrequencyType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(recurringDepositTypeIdParamName, this.recurringDepositType)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringDepositTypeIdParamName);
            actualChanges.put(recurringDepositTypeIdParamName, SavingsEnumerations.recurringDepositType(newValue));
            this.recurringDepositType = newValue;
        }

        return actualChanges;
    }

    public Integer recurringDepositType() {
        return this.recurringDepositType;
    }

    public Integer recurringDepositFrequency() {
        return this.recurringDepositFrequency;
    }

    public Integer recurringDepositFrequencyTypeId() {
        return this.recurringDepositFrequencyType;
    }

    public SavingsPeriodFrequencyType recurringDepositFrequencyType() {
        return SavingsPeriodFrequencyType.fromInt(this.recurringDepositFrequencyType);
    }

    public DepositRecurringDetail copy() {
        final RecurringDepositType recurringDepositType = RecurringDepositType.fromInt(this.recurringDepositType);
        final Integer recurringDepositFrequency = this.recurringDepositFrequency;
        final SavingsPeriodFrequencyType recurringDepositFrequencyType = SavingsPeriodFrequencyType
                .fromInt(this.recurringDepositFrequencyType);
        return DepositRecurringDetail.createFrom(recurringDepositType, recurringDepositFrequency, recurringDepositFrequencyType);
    }
}