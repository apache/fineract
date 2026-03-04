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
package org.apache.fineract.portfolio.workingcapitalloan;

public final class WorkingCapitalLoanConstants {

    private WorkingCapitalLoanConstants() {
        // Prevent instantiation
    }

    public static final String WCL_RESOURCE_NAME = "WORKINGCAPITALLOAN";

    // Common request parameters (locale, dateFormat, id)
    public static final String localeParameterName = "locale";
    public static final String dateFormatParameterName = "dateFormat";
    public static final String idParameterName = "id";

    // Working Capital Loan Application parameters
    public static final String clientIdParameterName = "clientId";
    public static final String productIdParameterName = "productId";
    public static final String fundIdParameterName = "fundId";
    public static final String accountNoParameterName = "accountNo";
    public static final String externalIdParameterName = "externalId";
    public static final String submittedOnDateParameterName = "submittedOnDate";
    public static final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
    public static final String submittedOnNoteParameterName = "submittedOnNote";
    public static final String totalPaymentParamName = "totalPayment";
    public static final String principalAmountParamName = "principalAmount";
}
