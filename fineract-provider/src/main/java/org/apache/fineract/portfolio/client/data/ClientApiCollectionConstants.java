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
package org.apache.fineract.portfolio.client.data;

import org.apache.fineract.portfolio.client.api.ClientApiConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ClientApiCollectionConstants extends ClientApiConstants{

    protected static final Set<String> CLIENT_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(familyMembers,address,localeParamName, dateFormatParamName, groupIdParamName, accountNoParamName, externalIdParamName,
                    mobileNoParamName, emailAddressParamName, firstnameParamName, middlenameParamName, lastnameParamName, fullnameParamName, officeIdParamName,
                    activeParamName, activationDateParamName, staffIdParamName, submittedOnDateParamName, savingsProductIdParamName,
                    dateOfBirthParamName, genderIdParamName, clientTypeIdParamName, clientClassificationIdParamName,
                    clientNonPersonDetailsParamName, displaynameParamName, legalFormIdParamName, datatables, isStaffParamName));

    protected static final Set<String> CLIENT_NON_PERSON_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(familyMembers,address,localeParamName, dateFormatParamName, incorpNumberParamName, remarksParamName, incorpValidityTillParamName,
                    constitutionIdParamName, mainBusinessLineIdParamName, datatables));

    protected static final Set<String> CLIENT_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, accountNoParamName, externalIdParamName, mobileNoParamName, emailAddressParamName, firstnameParamName, middlenameParamName,

            lastnameParamName, fullnameParamName, activeParamName, activationDateParamName, staffIdParamName, savingsProductIdParamName,
            dateOfBirthParamName, genderIdParamName, clientTypeIdParamName, clientClassificationIdParamName, submittedOnDateParamName, clientNonPersonDetailsParamName, displaynameParamName, legalFormIdParamName, isStaffParamName));

    protected static final Set<String> CLIENT_NON_PERSON_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, incorpNumberParamName, remarksParamName, incorpValidityTillParamName,
            constitutionIdParamName, mainBusinessLineIdParamName));


    /**
     * These parameters will match the class level parameters of
     * {@link ClientData}. Where possible, we try to get response parameters to
     * match those of request parameters.
     */

    protected static final Set<String> ACTIVATION_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, activationDateParamName));
    protected static final Set<String> REACTIVATION_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, reactivationDateParamName));

    protected static final Set<String> CLIENT_CLOSE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, closureDateParamName, closureReasonIdParamName));

    protected static final Set<String> CLIENT_REJECT_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, rejectionDateParamName, rejectionReasonIdParamName));

    protected static final Set<String> CLIENT_WITHDRAW_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, withdrawalDateParamName, withdrawalReasonIdParamName));

    protected static final Set<String> UNDOREJECTION_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, reopenedDateParamName));

    protected static final Set<String> UNDOWITHDRAWN_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, reopenedDateParamName));

    protected static final Set<String> CLIENT_CHARGES_ADD_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(chargeIdParamName, amountParamName, dueAsOfDateParamName, dateFormatParamName, localeParamName));

    protected static final Set<String> CLIENT_CHARGES_PAY_CHARGE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(amountParamName,
            transactionDateParamName, dateFormatParamName, localeParamName, paymentTypeIdParamName, transactionAccountNumberParamName,
            checkNumberParamName, routingCodeParamName, receiptNumberParamName, bankNumberParamName));


}
