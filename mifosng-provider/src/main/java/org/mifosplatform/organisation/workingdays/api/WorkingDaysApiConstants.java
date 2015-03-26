/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WorkingDaysApiConstants {

    public static final String WORKING_DAYS_RESOURCE_NAME = "workingdays";

    // request parameters
    public static final String recurrence = "recurrence";

    public static final String repayment_rescheduling_enum = "repaymentRescheduleType";

    public static final String idParamName = "id";

    public static final String rescheduleRepaymentTemplate = "rescheduleRepaymentTemplate";
    public static final String localeParamName = "locale";



    public static final Set<String> WORKING_DAYS_CREATE_OR_UPDATE_REQUEST_DATA_PARAMETERS =new HashSet<>(Arrays.asList(
            recurrence,repayment_rescheduling_enum,localeParamName
    ));
    public static final Set<String> WORKING_DAYS_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(idParamName,
            recurrence,repayment_rescheduling_enum
    ));

    public static final Set<String> WORKING_DAYS_TEMPLATE_PARAMETERS = new HashSet<>(Arrays.asList(rescheduleRepaymentTemplate));
}
