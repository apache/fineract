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
}