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
package org.apache.fineract.portfolio.collectionsheet;

public final class CollectionSheetConstants {

    private CollectionSheetConstants() {

    }

    public static final String COLLECTIONSHEET_RESOURCE_NAME = "collectionsheet";

    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    public static final String transactionDateParamName = "transactionDate";
    public static final String actualDisbursementDateParamName = "actualDisbursementDate";
    public static final String bulkRepaymentTransactionsParamName = "bulkRepaymentTransactions";
    public static final String bulkDisbursementTransactionsParamName = "bulkDisbursementTransactions";
    public static final String bulkSavingsDueTransactionsParamName = "bulkSavingsDueTransactions";
    public static final String noteParamName = "note";
    public static final String calendarIdParamName = "calendarId";
    public static final String officeIdParamName = "officeId";
    public static final String staffIdParamName = "staffId";
    public static final String isTransactionDateOnNonMeetingDateParamName = "isTransactionDateOnNonMeetingDate";

    // attendance parameters
    public static final String clientsAttendanceParamName = "clientsAttendance";
    public static final String clientIdParamName = "clientId";
    public static final String attendanceTypeParamName = "attendanceType";

    public static final String loanIdParamName = "loanId";
    public static final String savingsIdParamName = "savingsId";
    public static final String transactionAmountParamName = "transactionAmount";
    public static final String depositAccountTypeParamName = "depositAccountType";
}
