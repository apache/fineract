/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.allowWithdrawalParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.isMandatoryDepositParamName;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.infrastructure.core.api.JsonCommand;

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

    protected DepositRecurringDetail() {
        //
    }

    public static DepositRecurringDetail createFrom(final boolean isMandatoryDeposit, boolean allowWithdrawal,
            boolean adjustAdvanceTowardsFuturePayments) {

        return new DepositRecurringDetail(isMandatoryDeposit, allowWithdrawal, adjustAdvanceTowardsFuturePayments);
    }

    private DepositRecurringDetail(final boolean isMandatoryDeposit, boolean allowWithdrawal, boolean adjustAdvanceTowardsFuturePayments) {
        this.isMandatoryDeposit = isMandatoryDeposit;
        this.allowWithdrawal = allowWithdrawal;
        this.adjustAdvanceTowardsFuturePayments = adjustAdvanceTowardsFuturePayments;
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

    public DepositRecurringDetail copy() {
        return DepositRecurringDetail.createFrom(this.isMandatoryDeposit, this.allowWithdrawal, this.adjustAdvanceTowardsFuturePayments);
    }
}