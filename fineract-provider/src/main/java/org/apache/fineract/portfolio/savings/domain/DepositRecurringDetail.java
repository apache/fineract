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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.allowWithdrawalParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.isMandatoryDepositParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.recurringFrequencyParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.recurringFrequencyTypeParamName;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;

/**
 * DepositRecurringDetail encapsulates all the details of a
 * {@link RecurringDepositProduct} that are also used and persisted by a
 * {@link RecurringDepositAccount}.
 */
@Embeddable
public class DepositRecurringDetail {

    @Column(name = "is_mandatory", nullable = true)
    private boolean isMandatoryDeposit;

    @Column(name = "allow_withdrawal", nullable = true)
    private boolean allowWithdrawal;

    @Column(name = "adjust_advance_towards_future_payments", nullable = true)
    private boolean adjustAdvanceTowardsFuturePayments;
    
    @Column(name = "recurring_frequency", nullable = true)
    private Integer recurringFrequency;

    @Column(name = "recurring_frequency_type_enum", nullable = true)
    private Integer recurringFrequencyType;
    
    protected DepositRecurringDetail() {
        //
    }

    public static DepositRecurringDetail createFrom(final boolean isMandatoryDeposit, boolean allowWithdrawal,
            boolean adjustAdvanceTowardsFuturePayments, Integer recurringFrequency, SavingsPeriodFrequencyType recurringFrequencyType) {

        return new DepositRecurringDetail(isMandatoryDeposit, allowWithdrawal, adjustAdvanceTowardsFuturePayments, recurringFrequency, recurringFrequencyType);
    }

    private DepositRecurringDetail(final boolean isMandatoryDeposit, boolean allowWithdrawal, boolean adjustAdvanceTowardsFuturePayments, 
            Integer recurringFrequency, SavingsPeriodFrequencyType recurringFrequencyType) {
        this.isMandatoryDeposit = isMandatoryDeposit;
        this.allowWithdrawal = allowWithdrawal;
        this.adjustAdvanceTowardsFuturePayments = adjustAdvanceTowardsFuturePayments;
        this.recurringFrequency = recurringFrequency;
        this.recurringFrequencyType = (recurringFrequencyType == null) ? null : recurringFrequencyType.getValue();
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        if (command.isChangeInBooleanParameterNamed(isMandatoryDepositParamName, this.isMandatoryDeposit)) {
            final boolean newValue = command.booleanObjectValueOfParameterNamed(isMandatoryDepositParamName);
            actualChanges.put(isMandatoryDepositParamName, newValue);
            this.isMandatoryDeposit = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(allowWithdrawalParamName, this.allowWithdrawal)) {
            final boolean newValue = command.booleanObjectValueOfParameterNamed(allowWithdrawalParamName);
            actualChanges.put(allowWithdrawalParamName, newValue);
            this.allowWithdrawal = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(adjustAdvanceTowardsFuturePaymentsParamName, this.adjustAdvanceTowardsFuturePayments)) {
            final boolean newValue = command.booleanObjectValueOfParameterNamed(adjustAdvanceTowardsFuturePaymentsParamName);
            actualChanges.put(adjustAdvanceTowardsFuturePaymentsParamName, newValue);
            this.adjustAdvanceTowardsFuturePayments = newValue;
        }
        
        if (command.isChangeInIntegerParameterNamed(recurringFrequencyParamName, this.recurringFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringFrequencyParamName);
            actualChanges.put(recurringFrequencyParamName, newValue);
            this.recurringFrequency = newValue;
        }
        if (command.isChangeInIntegerParameterNamed(recurringFrequencyTypeParamName, this.recurringFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringFrequencyTypeParamName);
            actualChanges.put(recurringFrequencyTypeParamName, SavingsEnumerations.depositTermFrequencyType(newValue));
            this.recurringFrequencyType = newValue;
        }

        return actualChanges;
    }

    public boolean isMandatoryDeposit() {
        return this.isMandatoryDeposit;
    }

    public boolean allowWithdrawal() {
        return this.allowWithdrawal;
    }

    public boolean adjustAdvanceTowardsFuturePayments() {
        return this.adjustAdvanceTowardsFuturePayments;
    }
    
    public Integer recurringFrequency() {
      return this.recurringFrequency;
    }
    
    public Integer recurringFrequencyType() {
      return this.recurringFrequencyType;
    }

    public DepositRecurringDetail copy() {
        return DepositRecurringDetail.createFrom(this.isMandatoryDeposit, this.allowWithdrawal, this.adjustAdvanceTowardsFuturePayments, 
                this.recurringFrequency, SavingsPeriodFrequencyType.fromInt(this.recurringFrequencyType));
    }
}