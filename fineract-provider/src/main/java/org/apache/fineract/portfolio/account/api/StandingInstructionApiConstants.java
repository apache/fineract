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
package org.apache.fineract.portfolio.account.api;

public final class StandingInstructionApiConstants {

    private StandingInstructionApiConstants() {

    }

    public static final String STANDING_INSTRUCTION_RESOURCE_NAME = "standinginstruction";

    public static final String nameParamName = "name";
    public static final String priorityParamName = "priority";
    public static final String instructionTypeParamName = "instructionType";
    public static final String statusParamName = "status";
    public static final String amountParamName = "amount";
    public static final String validFromParamName = "validFrom";
    public static final String validTillParamName = "validTill";
    public static final String recurrenceTypeParamName = "recurrenceType";
    public static final String recurrenceFrequencyParamName = "recurrenceFrequency";
    public static final String recurrenceIntervalParamName = "recurrenceInterval";
    public static final String recurrenceOnMonthDayParamName = "recurrenceOnMonthDay";
    public static final String monthDayFormatParamName = "monthDayFormat";

    public static final String INVALID_MONTH_DAY_FORMAT_ERROR_CODE = "invalid.month.day.format";
    public static final String BEFORE_FIRST_EXECUTION_DATE_ERROR_CODE = "must.not.be.before.first.execution.date";
    public static final String AMOUNT_NOT_ALLOWED_FOR_DUES_ERROR_CODE = "not.allowed.for.dues.instruction";
    public static final String CANNOT_TRANSFER_TO_SAME_ACCOUNT_ERROR_CODE = "transfer.to.same.account.not.allowed";
    public static final String INSTRUCTION_TYPE_DUES_NOT_ALLOWED_FOR_ACCOUNT_TRANSFER_ERROR_CODE = "dues.not.allowed.for.account.transfer";
    public static final String RECURRENCE_AS_PER_DUES_NOT_ALLOWED_FOR_SAVINGS_ERROR_CODE = "as.per.dues.not.allowed.for.account.transfer";
    public static final String NOT_A_VALID_ACCOUNT_TRANSFER_ERROR_CODE = "not.a.valid.account.transfer";
    public static final String ACCOUNT_TRANSFER_NOT_ALLOWED_FOR_LOAN_ERROR_CODE = "account.transfer.is.not.allowed.for.loan.accounts";
    public static final String RECURRENCE_AS_PER_DUES_NOT_ALLOWED_WITH_FIXED_INSTRUCTION_ERROR_CODE = "as.per.dues.not.allowed.with.fixed.amount";
    public static final String NOT_A_VALID_LOAN_REPAYMENT_ERROR_CODE = "is.not.a.valid.loan.repayment";
    public static final String MUST_BE_BEFORE_EXISTING_VALID_TILL_ERROR_CODE = "must.be.before.existing.valid.till";
    public static final String CANNOT_BE_BEFORE_LAST_RUN_DATE_ERROR_CODE = "cannot.be.before.last.run.date";
}
