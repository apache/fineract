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
package org.apache.fineract.portfolio.interestratechart;

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
    public static final String isPrimaryGroupingByAmountParamName = "isPrimaryGroupingByAmount";

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
            dateFormatParamName, nameParamName, descriptionParamName, fromDateParamName, endDateParamName, productIdParamName, chartSlabs,
            isPrimaryGroupingByAmountParamName));

    public static final Set<String> INTERESTRATE_CHART_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, idParamName, nameParamName, descriptionParamName, fromDateParamName, endDateParamName, chartSlabs,
            deleteParamName, isPrimaryGroupingByAmountParamName));

    public static final Set<String> INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, idParamName, nameParamName, descriptionParamName, fromDateParamName, endDateParamName, chartSlabs,
            isPrimaryGroupingByAmountParamName));
}