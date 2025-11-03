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
package org.apache.fineract.test.data;

public enum LoanRescheduleErrorMessage {

    LOAN_CHARGED_OFF("Loan: %s reschedule installment is not allowed. Loan Account is Charged-off"), //
    LOAN_RESCHEDULE_DATE_NOT_IN_FUTURE("Loan Reschedule From date (%s) for Loan: %s should be in the future."), //
    LOAN_LOCKED_BY_COB("Loan is locked by the COB job. Loan ID: %s"), //
    LOAN_RESCHEDULE_NOT_ALLOWED_FROM_ZERO_TO_NEW_INTEREST_RATE("Failed data validation due to: newInterestRate."), //
    LOAN_RESCHEDULE_NOT_ALLOWED_FROM_CURRENT_INTEREST_RATE_TO_ZERO("The parameter `newInterestRate` must be greater than 0."), //
    NEW_INTEREST_RATE_IS_1_BUT_MINIMUM_IS_3("The parameter `newInterestRate` value 1.0 must not be less than the minimum value 3.0"), //
    NEW_INTEREST_RATE_IS_45_BUT_MAXIMUM_IS_20("The parameter `newInterestRate` value 45.0 must not be more than maximum value 20.0"), //
    LOAN_INTEREST_RATE_CANNOT_BE_NEGATIVE("The parameter `newInterestRate` must be greater than or equal to 0."); //

    private final String messageTemplate;

    LoanRescheduleErrorMessage(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getValue(Object... params) {
        if (params.length != getExpectedParameterCount()) {
            throw new IllegalArgumentException("Expected " + getExpectedParameterCount() + " parameters, but got " + params.length);
        }
        return String.format(this.messageTemplate, params);
    }

    public int getExpectedParameterCount() {
        // Count the number of placeholders (%s) in the message template
        return (int) messageTemplate.chars().filter(ch -> ch == '%').count();
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }
}
