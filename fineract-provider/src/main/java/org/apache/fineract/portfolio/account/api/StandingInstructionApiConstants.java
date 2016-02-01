/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.portfolio.account.AccountDetailConstants;

public class StandingInstructionApiConstants {

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

    public static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            AccountDetailConstants.localeParamName, AccountDetailConstants.dateFormatParamName,
            AccountDetailConstants.fromOfficeIdParamName, AccountDetailConstants.fromClientIdParamName,
            AccountDetailConstants.fromAccountTypeParamName, AccountDetailConstants.fromAccountIdParamName,
            AccountDetailConstants.toOfficeIdParamName, AccountDetailConstants.toClientIdParamName,
            AccountDetailConstants.toAccountTypeParamName, AccountDetailConstants.toAccountIdParamName,
            AccountDetailConstants.transferTypeParamName, priorityParamName, instructionTypeParamName, statusParamName, amountParamName,
            validFromParamName, validTillParamName, recurrenceTypeParamName, recurrenceFrequencyParamName, recurrenceIntervalParamName,
            recurrenceOnMonthDayParamName, nameParamName, monthDayFormatParamName));

    public static final Set<String> UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            AccountDetailConstants.localeParamName, AccountDetailConstants.dateFormatParamName, priorityParamName,
            instructionTypeParamName, statusParamName, amountParamName, validFromParamName, validTillParamName, recurrenceTypeParamName,
            recurrenceFrequencyParamName, recurrenceIntervalParamName, recurrenceOnMonthDayParamName, monthDayFormatParamName));

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(AccountDetailConstants.idParamName,
            nameParamName, priorityParamName, instructionTypeParamName, statusParamName, AccountDetailConstants.transferTypeParamName,
            validFromParamName, validTillParamName));

}