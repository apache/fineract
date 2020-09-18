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
import java.util.List;

public interface AdHocQuerySearchConstants {

    String AD_HOC_SEARCH_QUERY_RESOURCE_NAME = "adHocQuery";

    String localeParamName = "locale";
    String dateFormatParamName = "dateFormat";
    String entitiesParamName = "entities";
    String loanStatusParamName = "loanStatus";
    String loanProductsParamName = "loanProducts";
    String officesParamName = "offices";
    String loanDateOptionParamName = "loanDateOption";
    String loanFromDateParamName = "loanFromDate";
    String loanToDateParamName = "loanToDate";
    String includeOutStandingAmountPercentageParamName = "includeOutStandingAmountPercentage";
    String outStandingAmountPercentageConditionParamName = "outStandingAmountPercentageCondition";
    String minOutStandingAmountPercentageParamName = "minOutStandingAmountPercentage";
    String maxOutStandingAmountPercentageParamName = "maxOutStandingAmountPercentage";
    String outStandingAmountPercentageParamName = "outStandingAmountPercentage";
    String includeOutstandingAmountParamName = "includeOutstandingAmount";
    String outstandingAmountConditionParamName = "outstandingAmountCondition";
    String minOutstandingAmountParamName = "minOutstandingAmount";
    String maxOutstandingAmountParamName = "maxOutstandingAmount";
    String outstandingAmountParamName = "outstandingAmount";

    String approvalDateOption = "approvalDate";
    String createDateOption = "createdDate";
    String disbursalDateOption = "disbursalDate";

    String allLoanStatusOption = "all";
    String activeLoanStatusOption = "active";
    String overpaidLoanStatusOption = "overpaid";
    String arrearsLoanStatusOption = "arrears";
    String closedLoanStatusOption = "closed";
    String writeoffLoanStatusOption = "writeoff";

    List<Object> entityTypeOptions = List.copyOf(Arrays.asList("clients", "groups", "loans", "clientIdentifiers"));

}
