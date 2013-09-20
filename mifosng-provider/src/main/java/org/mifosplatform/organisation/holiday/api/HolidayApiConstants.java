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
    public static final String name = "name";
    public static final String fromDate = "fromDate";
    public static final String toDate = "toDate";
    public static final String description = "description";
    public static final String offices = "offices";
    public static final String officeId = "officeId";
    public static final String repaymentsRescheduledTo = "repaymentsRescheduledTo";
    public static final String processed = "processed";

    public static final Set<String> HOLIDAY_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(localeParamName,
            dateFormatParamName, name, fromDate, toDate, description, offices, repaymentsRescheduledTo));

    public static final Set<String> HOLIDAY_REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(localeParamName,
            dateFormatParamName, name, fromDate, toDate, repaymentsRescheduledTo));
}
