/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InterestRateChartApiConstants {

    public static final String INTERESTRATE_CHART_RESOURCE_NAME = "interestchart";

    // actions
    public static String summitalAction = ".summital";

    // command
    public static String COMMAND_UNDO_TRANSACTION = "undo";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // interest rate chart parameters
    public static final String idParamName = "id";
    public static final String nameParamName = "name";
    public static final String descriptionParamName = "description";
    public static final String fromDateParamName = "fromDate";
    public static final String endDateParamName = "endDate";
    public static final String productIdParamName = "productId";
    public static final String productNameParamName = "productName";

    // interest rate chart Slabs parameters
//    public static final String periodTypeParamName = "periodType";
//    public static final String fromPeriodParamName = "fromPeriod";
//    public static final String toPeriodParamName = "toPeriod";
//    public static final String amountRangeFromParamName = "amountRangeFrom";
//    public static final String amountRangeToParamName = "amountRangeTo";
//    public static final String annualInterestRateParamName = "annualInterestRate";
//    public static final String interestRateForFemaleParamName = "interestRateForFemale";
//    public static final String interestRateForChildrenParamName = "interestRateForChildren";
//    public static final String interestRateForSeniorCitizenParamName = "interestRateForSeniorCitizen";

    // associations
    public static final String chartSlabs = "chartSlabs";

    // to delete chart Slabs from chart
    public static final String deleteParamName = "delete";

    public static final Set<String> INTERESTRATE_CHART_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, nameParamName, descriptionParamName, fromDateParamName, endDateParamName, productIdParamName,
            chartSlabs));

    public static final Set<String> INTERESTRATE_CHART_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, idParamName, nameParamName, descriptionParamName, fromDateParamName, endDateParamName, chartSlabs,
            deleteParamName));

    public static final Set<String> INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, idParamName, nameParamName, descriptionParamName, fromDateParamName, endDateParamName, chartSlabs));
}