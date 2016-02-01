/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HolidayApiConstants {

    public static final String HOLIDAY_RESOURCE_NAME = "holiday";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // request parameters
    public static final String idParamName = "id";
    public static final String nameParamName = "name";
    public static final String fromDateParamName = "fromDate";
    public static final String toDateParamName = "toDate";
    public static final String descriptionParamName = "description";
    public static final String officesParamName = "offices";
    public static final String officeIdParamName = "officeId";
    public static final String repaymentsRescheduledToParamName = "repaymentsRescheduledTo";
    public static final String processed = "processed";
    public static final String status = "status";

    public static final Set<String> HOLIDAY_CREATE_OR_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, nameParamName, fromDateParamName, toDateParamName, descriptionParamName, officesParamName,
            repaymentsRescheduledToParamName));

    public static final Set<String> HOLIDAY_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(idParamName, nameParamName,
            fromDateParamName, descriptionParamName, toDateParamName, repaymentsRescheduledToParamName, localeParamName,
            dateFormatParamName, status));
}
