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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RescheduleLoansApiConstants {

    public final static String ENTITY_NAME = "RESCHEDULELOAN";

    public static final String LOAN_RESCHEDULE_REASON = "LoanRescheduleReason";
    
    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // create action request parameters
    public static final String loanIdParamName = "loanId";
    public static final String graceOnPrincipalParamName = "graceOnPrincipal";
    public static final String recurringMoratoriumOnPrincipalPeriodsParamName = "recurringMoratoriumOnPrincipalPeriods";
    public static final String graceOnInterestParamName = "graceOnInterest";
    public static final String extraTermsParamName = "extraTerms";
    public static final String rescheduleFromDateParamName = "rescheduleFromDate";
    public static final String recalculateInterestParamName = "recalculateInterest";
    public static final String newInterestRateParamName = "newInterestRate";
    public static final String rescheduleReasonIdParamName = "rescheduleReasonId";
    public static final String rescheduleReasonCommentParamName = "rescheduleReasonComment";
    public static final String submittedOnDateParamName = "submittedOnDate";
    public static final String adjustedDueDateParamName = "adjustedDueDate";
    public static final String resheduleForMultiDisbursementNotSupportedErrorCode = "loan.reschedule.multidisbursement.error.code";
    public static final String resheduleWithInterestRecalculationNotSupportedErrorCode = "loan.reschedule.interestrecalculation.error.code";
    public static final String allCommandParamName = "all";
    public static final String approveCommandParamName = "approve";
    public static final String pendingCommandParamName = "pending";
    public static final String rejectCommandParamName = "reject";
    
    // reject action request parameters
    public static final String rejectedOnDateParam = "rejectedOnDate";

    // approve action request parameters
    public static final String approvedOnDateParam = "approvedOnDate";

    public static final Set<String> APPROVE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName, dateFormatParamName,
            approvedOnDateParam));
    
    public static final Set<String> commandParams = new HashSet<>(Arrays.asList(allCommandParamName, approveCommandParamName, pendingCommandParamName, rejectCommandParamName));
}
