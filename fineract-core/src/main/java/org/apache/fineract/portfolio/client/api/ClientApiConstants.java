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
package org.apache.fineract.portfolio.client.api;

@SuppressWarnings({ "HideUtilityClassConstructor" })
public class ClientApiConstants {

    public static final String CLIENT_RESOURCE_NAME = "client";
    public static final String CLIENT_CHARGES_RESOURCE_NAME = "CLIENTCHARGE";

    // Client Charge Action Names
    public static final String CLIENT_CHARGE_ACTION_CREATE = "CREATE";
    public static final String CLIENT_CHARGE_ACTION_DELETE = "DELETE";
    public static final String CLIENT_CHARGE_ACTION_WAIVE = "WAIVE";
    public static final String CLIENT_CHARGE_ACTION_PAY = "PAY";

    // Client charge associations and query parameters
    public static final String CLIENT_CHARGE_QUERY_PARAM_STATUS = "chargeStatus";
    public static final String CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL = "all";
    public static final String CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE = "active";
    public static final String CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE = "inactive";
    public static final String CLIENT_CHARGE_ASSOCIATIONS_TRANSACTIONS = "transactions";

    // Client transaction action names
    public static final String CLIENT_TRANSACTION_ACTION_UNDO = "UNDOTRANSACTION";

    // Commands
    public static final String CLIENT_CHARGE_COMMAND_WAIVE_CHARGE = "waive";
    public static final String CLIENT_CHARGE_COMMAND_PAY_CHARGE = "paycharge";
    public static final String CLIENT_TRANSACTION_COMMAND_UNDO = "undo";

    public static final String CLIENT_CLOSURE_REASON = "ClientClosureReason";
    public static final String CLIENT_REJECT_REASON = "ClientRejectReason";
    public static final String CLIENT_WITHDRAW_REASON = "ClientWithdrawReason";

    public static final String GENDER = "Gender";
    public static final String CLIENT_TYPE = "ClientType";
    public static final String CLIENT_CLASSIFICATION = "ClientClassification";

    public static final String CLIENT_NON_PERSON_CONSTITUTION = "Constitution";
    public static final String CLIENT_NON_PERSON_MAIN_BUSINESS_LINE = "Main Business Line";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";
    public static final String address = "address";
    public static final String familyMembers = "familyMembers";

    // request parameters
    public static final String idParamName = "id";
    public static final String groupIdParamName = "groupId";
    public static final String accountNoParamName = "accountNo";
    public static final String externalIdParamName = "externalId";
    public static final String mobileNoParamName = "mobileNo";
    public static final String emailAddressParamName = "emailAddress";
    public static final String firstnameParamName = "firstname";
    public static final String middlenameParamName = "middlename";
    public static final String lastnameParamName = "lastname";
    public static final String fullnameParamName = "fullname";
    public static final String displaynameParamName = "displayname";
    public static final String officeIdParamName = "officeId";
    public static final String activeParamName = "active";
    public static final String activationDateParamName = "activationDate";
    public static final String reactivationDateParamName = "reactivationDate";
    public static final String staffIdParamName = "staffId";
    public static final String isStaffParamName = "isStaff";
    public static final String closureDateParamName = "closureDate";
    public static final String closureReasonIdParamName = "closureReasonId";
    public static final String reopenedDateParamName = "reopenedDate";

    public static final String rejectionDateParamName = "rejectionDate";
    public static final String rejectionReasonIdParamName = "rejectionReasonId";
    public static final String withdrawalDateParamName = "withdrawalDate";
    public static final String withdrawalReasonIdParamName = "withdrawalReasonId";

    public static final String submittedOnDateParamName = "submittedOnDate";
    public static final String savingsProductIdParamName = "savingsProductId";
    public static final String savingsAccountIdParamName = "savingsAccountId";
    public static final String dateOfBirthParamName = "dateOfBirth";
    public static final String genderIdParamName = "genderId";
    public static final String clientTypeIdParamName = "clientTypeId";
    public static final String clientClassificationIdParamName = "clientClassificationId";
    public static final String legalFormIdParamName = "legalFormId";
    // request parameters for payment details
    public static final String paymentTypeIdParamName = "paymentTypeId";
    public static final String transactionAccountNumberParamName = "accountNumber";
    public static final String checkNumberParamName = "checkNumber";
    public static final String routingCodeParamName = "routingCode";
    public static final String receiptNumberParamName = "receiptNumber";
    public static final String bankNumberParamName = "bankNumber";

    // request parameters for client non person
    public static final String clientNonPersonDetailsParamName = "clientNonPersonDetails";
    public static final String incorpNumberParamName = "incorpNumber";
    public static final String remarksParamName = "remarks";
    public static final String incorpValidityTillParamName = "incorpValidityTillDate";
    public static final String constitutionIdParamName = "constitutionId";
    public static final String mainBusinessLineIdParamName = "mainBusinessLineId";

    // response parameters
    public static final String statusParamName = "status";

    // client charges response parameters
    public static final String chargeIdParamName = "chargeId";
    public static final String clientIdParamName = "clientId";
    public static final String chargesParamName = "charges";
    public static final String dueAsOfDateParamName = "dueDate";
    public static final String transactionDateParamName = "transactionDate";
    public static final String amountParamName = "amount";

    // client transactions response parameters
    public static final String transactionAmountParamName = "transactionAmount";

    // template related part of response
    public static final String datatables = "datatables";
}
