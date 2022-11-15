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

package org.apache.fineract.portfolio.savings.request;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.allowWithdrawalParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.isCalendarInheritedParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.isMandatoryDepositParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.mandatoryRecommendedDepositAmountParamName;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.api.JsonCommand;

public class RecurringAccountDetailReq {

    private BigDecimal recurringDepositAmount;
    private boolean recurringDepositAmountSet = false;
    private boolean isMandatoryDeposit;
    private boolean isMandatoryDepositSet = false;
    private boolean allowWithdrawal;
    private boolean allowWithdrawalSet = false;
    private boolean adjustAdvanceTowardsFuturePayments;
    private boolean adjustAdvanceTowardsFuturePaymentsSet = false;
    private boolean isCalendarInherited;
    private boolean isCalendarInheritedSet = false;

    public static RecurringAccountDetailReq instance(JsonCommand command) {
        RecurringAccountDetailReq instance = new RecurringAccountDetailReq();

        instance.recurringDepositAmount = command.bigDecimalValueOfParameterNamed(mandatoryRecommendedDepositAmountParamName);
        if (command.parameterExists(isMandatoryDepositParamName)) {
            instance.isMandatoryDeposit = command.booleanObjectValueOfParameterNamed(isMandatoryDepositParamName);
            instance.isMandatoryDepositSet = true;
        }
        if (command.parameterExists(allowWithdrawalParamName)) {
            instance.allowWithdrawal = command.booleanObjectValueOfParameterNamed(allowWithdrawalParamName);
            instance.allowWithdrawalSet = true;
        }
        if (command.parameterExists(adjustAdvanceTowardsFuturePaymentsParamName)) {
            instance.adjustAdvanceTowardsFuturePayments = command
                    .booleanObjectValueOfParameterNamed(adjustAdvanceTowardsFuturePaymentsParamName);
        }
        if (command.parameterExists(isCalendarInheritedParamName)) {
            instance.isCalendarInherited = command.booleanObjectValueOfParameterNamed(isCalendarInheritedParamName);
        } else {
            instance.isCalendarInherited = false;
        }

        return instance;
    }

    public BigDecimal getRecurringDepositAmount() {
        return recurringDepositAmount;
    }

    public void setRecurringDepositAmount(BigDecimal recurringDepositAmount) {
        this.recurringDepositAmount = recurringDepositAmount;
    }

    public boolean isRecurringDepositAmountSet() {
        return recurringDepositAmountSet;
    }

    public void setRecurringDepositAmountSet(boolean recurringDepositAmountSet) {
        this.recurringDepositAmountSet = recurringDepositAmountSet;
    }

    public boolean isMandatoryDeposit() {
        return isMandatoryDeposit;
    }

    public void setMandatoryDeposit(boolean mandatoryDeposit) {
        isMandatoryDeposit = mandatoryDeposit;
    }

    public boolean isMandatoryDepositSet() {
        return isMandatoryDepositSet;
    }

    public void setMandatoryDepositSet(boolean mandatoryDepositSet) {
        isMandatoryDepositSet = mandatoryDepositSet;
    }

    public boolean isAllowWithdrawal() {
        return allowWithdrawal;
    }

    public void setAllowWithdrawal(boolean allowWithdrawal) {
        this.allowWithdrawal = allowWithdrawal;
    }

    public boolean isAllowWithdrawalSet() {
        return allowWithdrawalSet;
    }

    public void setAllowWithdrawalSet(boolean allowWithdrawalSet) {
        this.allowWithdrawalSet = allowWithdrawalSet;
    }

    public boolean isAdjustAdvanceTowardsFuturePayments() {
        return adjustAdvanceTowardsFuturePayments;
    }

    public void setAdjustAdvanceTowardsFuturePayments(boolean adjustAdvanceTowardsFuturePayments) {
        this.adjustAdvanceTowardsFuturePayments = adjustAdvanceTowardsFuturePayments;
    }

    public boolean isAdjustAdvanceTowardsFuturePaymentsSet() {
        return adjustAdvanceTowardsFuturePaymentsSet;
    }

    public void setAdjustAdvanceTowardsFuturePaymentsSet(boolean adjustAdvanceTowardsFuturePaymentsSet) {
        this.adjustAdvanceTowardsFuturePaymentsSet = adjustAdvanceTowardsFuturePaymentsSet;
    }

    public boolean isCalendarInherited() {
        return isCalendarInherited;
    }

    public void setCalendarInherited(boolean calendarInherited) {
        isCalendarInherited = calendarInherited;
    }

    public boolean isCalendarInheritedSet() {
        return isCalendarInheritedSet;
    }

    public void setCalendarInheritedSet(boolean calendarInheritedSet) {
        isCalendarInheritedSet = calendarInheritedSet;
    }
}
