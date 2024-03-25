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
package org.apache.fineract.test.initializer.global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostCodesRequest;
import org.apache.fineract.client.models.PutCodeValuesDataRequest;
import org.apache.fineract.client.services.CodeValuesApi;
import org.apache.fineract.client.services.CodesApi;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CodeGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String CODE_NAME_FINANCIAL_INSTRUMENT = "financial_instrument";
    public static final String CODE_NAME_TRANSACTION_TYPE = "transaction_type";
    public static final String CODE_NAME_BANKRUPTCY_TAG = "bankruptcy_tag";
    public static final String CODE_NAME_PENDING_FRAUD_TAG = "pending_fraud_tag";
    public static final String CODE_NAME_PENDING_DECEASED_TAG = "pending_deceased_tag";
    public static final String CODE_NAME_HARDSHIP_TAG = "hardship_tag";
    public static final String CODE_NAME_ACTIVE_DUTY_TAG = "active_duty_tag";
    public static final Long CODE_VALUE_ADDRESS_TYPE_ID = 29L;
    public static final String CODE_VALUE_ADDRESS_TYPE_RESIDENTIAL = "Residential address";
    public static final String CODE_VALUE_ADDRESS_TYPE_OFFICE = "Office address";
    public static final Long CODE_VALUE_COUNTRY_ID = 28L;
    public static final String CODE_VALUE_COUNTRY_GERMANY = "Germany";
    public static final Long CODE_VALUE_STATE_ID = 27L;
    public static final String CODE_VALUE_STATE_BERLIN = "Berlin";
    public static final Long CODE_VALUE_FINANCIAL_INSTRUMENT_ID = 39L;
    public static final String CODE_VALUE_FINANCIAL_INSTRUMENT_DEBIT = "debit_card";
    public static final String CODE_VALUE_FINANCIAL_INSTRUMENT_CREDIT = "credit_card";
    public static final Long CODE_VALUE_TRANSACTION_TYPE_ID = 40L;
    public static final String CODE_VALUE_TRANSACTION_TYPE_SCHEDULED_PAYMENT = "scheduled_payment";
    public static final Long CODE_VALUE_BANKRUPTCY_TAG_ID = 41L;
    public static final String CODE_VALUE_BANKRUPTCY_TAG_PENDING = "pending_bankruptcy";
    public static final String CODE_VALUE_BANKRUPTCY_TAG_BANKRUPTCY = "bankruptcy";
    public static final Long CODE_VALUE_PENDING_FRAUD_TAG_ID = 42L;
    public static final String CODE_VALUE_PENDING_FRAUD_TAG_PENDING = "pending_fraud";
    public static final String CODE_VALUE_PENDING_FRAUD_TAG_FRAUD = "fraud";
    public static final Long CODE_VALUE_PENDING_DECEASED_TAG_ID = 43L;
    public static final String CODE_VALUE_PENDING_DECEASED_TAG_PENDING = "pending_deceased";
    public static final String CODE_VALUE_PENDING_DECEASED_TAG_DECEASED = "deceased";
    public static final Long CODE_VALUE_HARDSHIP_TAG_ID = 44L;
    public static final String CODE_VALUE_HARDSHIP_TAG_ACTIVE = "active";
    public static final String CODE_VALUE_HARDSHIP_TAG_INACTIVE = "inactive";
    public static final Long CODE_VALUE_ACTIVE_DUTY_TAG_ID = 45L;
    public static final String CODE_VALUE_ACTIVE_DUTY_TAG_ACTIVE = "active";
    public static final String CODE_VALUE_ACTIVE_DUTY_TAG_INACTIVE = "inactive";
    public static final Long CODE_VALUE_CUSTOMER_IDENTIFIERS_ID = 1L;
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_1 = "Passport";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_2 = "Id";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_3 = "Drivers License";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_4 = "Any Other Id Type";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_5 = "SSN";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_6 = "TIN";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_7 = "ITIN";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_8 = "EIN";
    public static final Long CODE_VALUE_GENDER_ID = 4L;
    public static final String CODE_VALUE_GENDER_FEMALE = "Female";
    public static final String CODE_VALUE_GENDER_MALE = "Male";
    public static final Long CODE_VALUE_CLIENT_TYPE_ID = 16L;
    public static final String CODE_VALUE_CLIENT_TYPE_CORPORATE = "Corporate";
    public static final String CODE_VALUE_CLIENT_TYPE_LEGAL = "Legal";
    public static final String CODE_VALUE_CLIENT_TYPE_NON_LEGAL = "Non-legal";
    public static final Long CODE_VALUE_CLIENT_CLASSIFICATION_ID = 17L;
    public static final String CODE_VALUE_CLIENT_CLASSIFICATION_LAWYER = "Lawyer";
    public static final String CODE_VALUE_CLIENT_CLASSIFICATION_DIRECTOR = "Director";
    public static final String CODE_VALUE_CLIENT_CLASSIFICATION_NONE = "None";
    public static final Long CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_ID = 31L;
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_SPOUSE = "Spouse";
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_FATHER = "Father";
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_MOTHER = "Mother";
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_CHILD = "Child";
    public static final Long CODE_VALUE_FAMILY_MEMBER_PROFESSION_ID = 32L;
    public static final String CODE_VALUE_FAMILY_MEMBER_PROFESSION_EMPLOYEE = "Employee";
    public static final String CODE_VALUE_FAMILY_MEMBER_PROFESSION_SELF_EMPLOYED = "Self-Employed";
    public static final Long CODE_VALUE_FAMILY_MARITAL_STATUS_ID = 30L;
    public static final String CODE_VALUE_FAMILY_MARITAL_STATUS_MARRIED = "Married";
    public static final String CODE_VALUE_FAMILY_MARITAL_STATUS_SINGLE = "Single";
    public static final String CODE_VALUE_FAMILY_MARITAL_STATUS_WIDOWED = "Widowed";
    public static final Long CODE_VALUE_CONSTITUTION_ID = 24L;
    public static final String CODE_VALUE_CONSTITUTION_TEST = "Test";

    public static final Long CODE_VALUE_RESCHEDULE_REASON_ID = 23L;
    public static final String CODE_VALUE_RESCHEDULE_REASON_TEST = "Test";

    private final CodesApi codesApi;
    private final CodeValuesApi codeValuesApi;

    @Override
    public void initialize() throws Exception {
        createCodeNames();
        createCodeValues();
    }

    private void createCodeValues() {
        // address type
        List<String> addressNames = new ArrayList<>();
        addressNames.add(CODE_VALUE_ADDRESS_TYPE_RESIDENTIAL);
        addressNames.add(CODE_VALUE_ADDRESS_TYPE_OFFICE);
        createCodeValues(CODE_VALUE_ADDRESS_TYPE_ID, addressNames);

        // Country
        List<String> countryNames = new ArrayList<>();
        countryNames.add(CODE_VALUE_COUNTRY_GERMANY);
        createCodeValues(CODE_VALUE_COUNTRY_ID, countryNames);

        // State
        List<String> stateNames = new ArrayList<>();
        stateNames.add(CODE_VALUE_STATE_BERLIN);
        createCodeValues(CODE_VALUE_STATE_ID, stateNames);

        // financial instrument
        List<String> financialInstrumentNames = new ArrayList<>();
        financialInstrumentNames.add(CODE_VALUE_FINANCIAL_INSTRUMENT_DEBIT);
        financialInstrumentNames.add(CODE_VALUE_FINANCIAL_INSTRUMENT_CREDIT);
        createCodeValues(CODE_VALUE_FINANCIAL_INSTRUMENT_ID, financialInstrumentNames);

        // transaction type
        List<String> transactionTypeNames = new ArrayList<>();
        transactionTypeNames.add(CODE_VALUE_TRANSACTION_TYPE_SCHEDULED_PAYMENT);
        createCodeValues(CODE_VALUE_TRANSACTION_TYPE_ID, transactionTypeNames);

        // bankruptcy tag
        List<String> bankruptcyTagNames = new ArrayList<>();
        bankruptcyTagNames.add(CODE_VALUE_BANKRUPTCY_TAG_PENDING);
        bankruptcyTagNames.add(CODE_VALUE_BANKRUPTCY_TAG_BANKRUPTCY);
        createCodeValues(CODE_VALUE_BANKRUPTCY_TAG_ID, bankruptcyTagNames);

        // pending fraud tag
        List<String> pendingFraudTagNames = new ArrayList<>();
        pendingFraudTagNames.add(CODE_VALUE_PENDING_FRAUD_TAG_PENDING);
        pendingFraudTagNames.add(CODE_VALUE_PENDING_FRAUD_TAG_FRAUD);
        createCodeValues(CODE_VALUE_PENDING_FRAUD_TAG_ID, pendingFraudTagNames);

        // pending deceased tag
        List<String> pendingDeceasedTagNames = new ArrayList<>();
        pendingDeceasedTagNames.add(CODE_VALUE_PENDING_DECEASED_TAG_PENDING);
        pendingDeceasedTagNames.add(CODE_VALUE_PENDING_DECEASED_TAG_DECEASED);
        createCodeValues(CODE_VALUE_PENDING_DECEASED_TAG_ID, pendingDeceasedTagNames);

        // hardship tag
        List<String> hardshipTagNames = new ArrayList<>();
        hardshipTagNames.add(CODE_VALUE_HARDSHIP_TAG_ACTIVE);
        hardshipTagNames.add(CODE_VALUE_HARDSHIP_TAG_INACTIVE);
        createCodeValues(CODE_VALUE_HARDSHIP_TAG_ID, hardshipTagNames);

        // active duty tag
        List<String> activeDutyTagNames = new ArrayList<>();
        activeDutyTagNames.add(CODE_VALUE_ACTIVE_DUTY_TAG_ACTIVE);
        activeDutyTagNames.add(CODE_VALUE_ACTIVE_DUTY_TAG_INACTIVE);
        createCodeValues(CODE_VALUE_ACTIVE_DUTY_TAG_ID, activeDutyTagNames);

        // customer identifiers put/post
        List<String> customerIdentifierNamesPut = new ArrayList<>();
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_1);
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_2);
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_3);
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_4);
        updateCodeValues(CODE_VALUE_CUSTOMER_IDENTIFIERS_ID, customerIdentifierNamesPut);

        List<String> customerIdentifierNamesPost = new ArrayList<>();
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_5);
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_6);
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_7);
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_8);
        createCodeValues(CODE_VALUE_CUSTOMER_IDENTIFIERS_ID, customerIdentifierNamesPost);

        // gender
        List<String> genderNames = new ArrayList<>();
        genderNames.add(CODE_VALUE_GENDER_FEMALE);
        genderNames.add(CODE_VALUE_GENDER_MALE);
        createCodeValues(CODE_VALUE_GENDER_ID, genderNames);

        // client type
        List<String> clientTypeNames = new ArrayList<>();
        clientTypeNames.add(CODE_VALUE_CLIENT_TYPE_CORPORATE);
        clientTypeNames.add(CODE_VALUE_CLIENT_TYPE_LEGAL);
        clientTypeNames.add(CODE_VALUE_CLIENT_TYPE_NON_LEGAL);
        createCodeValues(CODE_VALUE_CLIENT_TYPE_ID, clientTypeNames);

        // client classification
        List<String> clientClassificationNames = new ArrayList<>();
        clientClassificationNames.add(CODE_VALUE_CLIENT_CLASSIFICATION_LAWYER);
        clientClassificationNames.add(CODE_VALUE_CLIENT_CLASSIFICATION_DIRECTOR);
        clientClassificationNames.add(CODE_VALUE_CLIENT_CLASSIFICATION_NONE);
        createCodeValues(CODE_VALUE_CLIENT_CLASSIFICATION_ID, clientClassificationNames);

        // add family member - relationship
        List<String> familyMemberRelationshipNames = new ArrayList<>();
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_SPOUSE);
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_FATHER);
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_MOTHER);
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_CHILD);
        createCodeValues(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_ID, familyMemberRelationshipNames);

        // add family member - profession
        List<String> familyMemberProfessionNames = new ArrayList<>();
        familyMemberProfessionNames.add(CODE_VALUE_FAMILY_MEMBER_PROFESSION_EMPLOYEE);
        familyMemberProfessionNames.add(CODE_VALUE_FAMILY_MEMBER_PROFESSION_SELF_EMPLOYED);
        createCodeValues(CODE_VALUE_FAMILY_MEMBER_PROFESSION_ID, familyMemberProfessionNames);

        // add family member - marital status
        List<String> familyMemberMaritalStatusNames = new ArrayList<>();
        familyMemberMaritalStatusNames.add(CODE_VALUE_FAMILY_MARITAL_STATUS_MARRIED);
        familyMemberMaritalStatusNames.add(CODE_VALUE_FAMILY_MARITAL_STATUS_SINGLE);
        familyMemberMaritalStatusNames.add(CODE_VALUE_FAMILY_MARITAL_STATUS_WIDOWED);
        createCodeValues(CODE_VALUE_FAMILY_MARITAL_STATUS_ID, familyMemberMaritalStatusNames);

        // add constitution (for client creation as Entity)
        List<String> constitutionNames = new ArrayList<>();
        constitutionNames.add(CODE_VALUE_CONSTITUTION_TEST);
        createCodeValues(CODE_VALUE_CONSTITUTION_ID, constitutionNames);

        // add LoanRescheduleReason
        List<String> rescheduleReasonNames = new ArrayList<>();
        rescheduleReasonNames.add(CODE_VALUE_RESCHEDULE_REASON_TEST);
        createCodeValues(CODE_VALUE_RESCHEDULE_REASON_ID, rescheduleReasonNames);
    }

    public void createCodeValues(Long codeId, List<String> codeValueNames) {
        codeValueNames.forEach(name -> {
            Integer position = codeValueNames.indexOf(name);
            PostCodeValuesDataRequest postCodeValuesDataRequest = new PostCodeValuesDataRequest();
            postCodeValuesDataRequest.isActive(true);
            postCodeValuesDataRequest.name(name);
            postCodeValuesDataRequest.position(position);

            try {
                codeValuesApi.createCodeValue(codeId, postCodeValuesDataRequest).execute();
            } catch (IOException e) {
                throw new RuntimeException("Error while creating code value", e);
            }
        });
    }

    public void updateCodeValues(Long codeId, List<String> codeValueNames) {
        codeValueNames.forEach(name -> {
            int position = codeValueNames.indexOf(name) + 1;
            PutCodeValuesDataRequest putCodeValuesDataRequest = new PutCodeValuesDataRequest();
            putCodeValuesDataRequest.isActive(false);
            putCodeValuesDataRequest.name(name);
            putCodeValuesDataRequest.position(position);

            try {
                codeValuesApi.updateCodeValue(codeId, (long) position, putCodeValuesDataRequest).execute();
            } catch (IOException e) {
                throw new RuntimeException("Error while updating code value", e);
            }
        });
    }

    private void createCodeNames() {
        List<String> codesNameList = new ArrayList<>();
        codesNameList.add(CODE_NAME_FINANCIAL_INSTRUMENT);
        codesNameList.add(CODE_NAME_TRANSACTION_TYPE);
        codesNameList.add(CODE_NAME_BANKRUPTCY_TAG);
        codesNameList.add(CODE_NAME_PENDING_FRAUD_TAG);
        codesNameList.add(CODE_NAME_PENDING_DECEASED_TAG);
        codesNameList.add(CODE_NAME_HARDSHIP_TAG);
        codesNameList.add(CODE_NAME_ACTIVE_DUTY_TAG);

        codesNameList.forEach(codeName -> {
            PostCodesRequest postCodesRequest = new PostCodesRequest();
            try {
                codesApi.createCode(postCodesRequest.name(codeName)).execute();
            } catch (IOException e) {
                throw new RuntimeException("Error while creating code", e);
            }
        });
    }
}
