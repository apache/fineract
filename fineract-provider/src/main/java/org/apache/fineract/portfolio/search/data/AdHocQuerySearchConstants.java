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
package org.apache.fineract.portfolio.search.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface AdHocQuerySearchConstants {

    public static final String AD_HOC_SEARCH_QUERY_RESOURCE_NAME = "adHocQuery";

    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";
    public static final String entitiesParamName = "entities";
    public static final String loanStatusParamName = "loanStatus";
    public static final String loanProductsParamName = "loanProducts";
    public static final String officesParamName = "offices";
    public static final String loanDateOptionParamName = "loanDateOption";
    public static final String loanFromDateParamName = "loanFromDate";
    public static final String loanToDateParamName = "loanToDate";
    public static final String includeOutStandingAmountPercentageParamName = "includeOutStandingAmountPercentage";
    public static final String outStandingAmountPercentageConditionParamName = "outStandingAmountPercentageCondition";
    public static final String minOutStandingAmountPercentageParamName = "minOutStandingAmountPercentage";
    public static final String maxOutStandingAmountPercentageParamName = "maxOutStandingAmountPercentage";
    public static final String outStandingAmountPercentageParamName = "outStandingAmountPercentage";
    public static final String includeOutstandingAmountParamName = "includeOutstandingAmount";
    public static final String outstandingAmountConditionParamName = "outstandingAmountCondition";
    public static final String minOutstandingAmountParamName = "minOutstandingAmount";
    public static final String maxOutstandingAmountParamName = "maxOutstandingAmount";
    public static final String outstandingAmountParamName = "outstandingAmount";

    public static final Set<String> AD_HOC_SEARCH_QUERY_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(entitiesParamName, loanStatusParamName,
            loanProductsParamName, officesParamName, loanDateOptionParamName, loanFromDateParamName, loanToDateParamName,
            includeOutStandingAmountPercentageParamName, outStandingAmountPercentageConditionParamName,
            minOutStandingAmountPercentageParamName, maxOutStandingAmountPercentageParamName, outStandingAmountPercentageParamName,
            includeOutstandingAmountParamName, outstandingAmountConditionParamName, minOutstandingAmountParamName,
            maxOutstandingAmountParamName, outstandingAmountParamName, localeParamName, dateFormatParamName));

    public static final Set<String> AD_HOC_SEARCH_QUERY_CONDITIONS = new HashSet<>(
            Arrays.asList("between", "<=", ">=", "<", ">", "="));

    public static final Object[] loanDateOptions = { "approvalDate", "createdDate", "disbursalDate" };
    public static final Object[] entityTypeOptions = { "clients", "groups", "loans", "clientIdentifiers" };
    public static final Object[] loanStatusOptions = { "all", "active", "overpaid", "arrears", "closed", "writeoff" };

}
