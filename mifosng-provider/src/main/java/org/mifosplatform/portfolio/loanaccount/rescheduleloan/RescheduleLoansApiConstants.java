/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RescheduleLoansApiConstants {

    public final static String ENTITY_NAME = "RESCHEDULELOAN";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // create action request parameters
    public static final String loanIdParamName = "loanId";
    public static final String graceOnPrincipalParamName = "graceOnPrincipal";
    public static final String graceOnInterestParamName = "graceOnInterest";
    public static final String extraTermsParamName = "extraTerms";
    public static final String rescheduleFromDateParamName = "rescheduleFromDate";
    public static final String recalculateInterestParamName = "recalculateInterest";
    public static final String newInterestRateParamName = "newInterestRate";
    public static final String rescheduleReasonIdParamName = "rescheduleReasonId";
    public static final String rescheduleReasonCommentParamName = "rescheduleReasonComment";
    public static final String submittedOnDateParamName = "submittedOnDate";
    public static final String adjustedDueDateParamName = "adjustedDueDate";

    public static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName, dateFormatParamName,
            graceOnPrincipalParamName, graceOnInterestParamName, extraTermsParamName, rescheduleFromDateParamName,
            newInterestRateParamName, rescheduleReasonIdParamName, rescheduleReasonCommentParamName, submittedOnDateParamName,
            loanIdParamName, adjustedDueDateParamName, recalculateInterestParamName));

    // reject action request parameters
    public static final String rejectedOnDateParam = "rejectedOnDate";

    public static final Set<String> REJECT_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName, dateFormatParamName,
            rejectedOnDateParam));

    // approve action request parameters
    public static final String approvedOnDateParam = "approvedOnDate";

    public static final Set<String> APPROVE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName, dateFormatParamName,
            approvedOnDateParam));
}
