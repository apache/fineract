/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InterestRateChartSlabApiConstants {

    public static final String INTERESTRATE_CHART_SLAB_RESOURCE_NAME = "chartslab";

    // actions

    // command

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // interest rate chart Slabs parameters
    public static final String idParamName = "id";
    public static final String descriptionParamName = "description";
    public static final String periodTypeParamName = "periodType";
    public static final String fromPeriodParamName = "fromPeriod";
    public static final String toPeriodParamName = "toPeriod";
    public static final String amountRangeFromParamName = "amountRangeFrom";
    public static final String amountRangeToParamName = "amountRangeTo";
    public static final String annualInterestRateParamName = "annualInterestRate";
    public static final String currencyCodeParamName = "currencyCode";
    public static final String incentivesParamName = "incentives";

    // associations

    public static final Set<String> INTERESTRATE_CHART_SLAB_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            localeParamName, idParamName, descriptionParamName, periodTypeParamName, fromPeriodParamName, toPeriodParamName,
            amountRangeFromParamName, amountRangeToParamName, annualInterestRateParamName, currencyCodeParamName, incentivesParamName));

    public static final Set<String> INTERESTRATE_CHART_SLAB_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            localeParamName, idParamName, descriptionParamName, periodTypeParamName, fromPeriodParamName, toPeriodParamName,
            amountRangeFromParamName, amountRangeToParamName, annualInterestRateParamName, currencyCodeParamName, incentivesParamName));

    public static final Set<String> INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            idParamName, descriptionParamName, periodTypeParamName, fromPeriodParamName, toPeriodParamName, amountRangeFromParamName,
            amountRangeToParamName, annualInterestRateParamName, currencyCodeParamName, incentivesParamName));
}