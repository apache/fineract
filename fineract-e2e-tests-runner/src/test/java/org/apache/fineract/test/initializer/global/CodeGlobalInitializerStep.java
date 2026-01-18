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

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostCodesRequest;
import org.apache.fineract.client.models.PutCodeValuesDataRequest;
import org.apache.fineract.test.data.codevalue.CodeNames;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CodeGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String CODE_VALUE_ADDRESS_TYPE_RESIDENTIAL = "Residential address";
    public static final String CODE_VALUE_ADDRESS_TYPE_OFFICE = "Office address";
    public static final String CODE_VALUE_COUNTRY_GERMANY = "Germany";
    public static final String CODE_VALUE_STATE_BERLIN = "Berlin";
    public static final String CODE_VALUE_FINANCIAL_INSTRUMENT_DEBIT = "debit_card";
    public static final String CODE_VALUE_FINANCIAL_INSTRUMENT_CREDIT = "credit_card";
    public static final String CODE_VALUE_CHARGE_OFF_REASON_FRAUD = "Fraud";
    public static final String CODE_VALUE_CHARGE_OFF_REASON_DELINQUENT = "Delinquent";
    public static final String CODE_VALUE_CHARGE_OFF_REASON_OTHER = "Other";
    public static final String CODE_VALUE_TRANSACTION_TYPE_SCHEDULED_PAYMENT = "scheduled_payment";
    public static final String CODE_VALUE_BANKRUPTCY_TAG_PENDING = "pending_bankruptcy";
    public static final String CODE_VALUE_BANKRUPTCY_TAG_BANKRUPTCY = "bankruptcy";
    public static final String CODE_VALUE_PENDING_FRAUD_TAG_PENDING = "pending_fraud";
    public static final String CODE_VALUE_PENDING_FRAUD_TAG_FRAUD = "fraud";
    public static final String CODE_VALUE_PENDING_DECEASED_TAG_PENDING = "pending_deceased";
    public static final String CODE_VALUE_PENDING_DECEASED_TAG_DECEASED = "deceased";
    public static final String CODE_VALUE_HARDSHIP_TAG_ACTIVE = "active";
    public static final String CODE_VALUE_HARDSHIP_TAG_INACTIVE = "inactive";
    public static final String CODE_VALUE_ACTIVE_DUTY_TAG_ACTIVE = "active";
    public static final String CODE_VALUE_ACTIVE_DUTY_TAG_INACTIVE = "inactive";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_1 = "Passport";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_2 = "Id";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_3 = "Drivers License";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_4 = "Any Other Id Type";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_5 = "SSN";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_6 = "TIN";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_7 = "ITIN";
    public static final String CODE_VALUE_CUSTOMER_IDENTIFIERS_8 = "EIN";
    public static final String CODE_VALUE_GENDER_FEMALE = "Female";
    public static final String CODE_VALUE_GENDER_MALE = "Male";
    public static final String CODE_VALUE_CLIENT_TYPE_CORPORATE = "Corporate";
    public static final String CODE_VALUE_CLIENT_TYPE_LEGAL = "Legal";
    public static final String CODE_VALUE_CLIENT_TYPE_NON_LEGAL = "Non-legal";
    public static final String CODE_VALUE_CLIENT_CLASSIFICATION_LAWYER = "Lawyer";
    public static final String CODE_VALUE_CLIENT_CLASSIFICATION_DIRECTOR = "Director";
    public static final String CODE_VALUE_CLIENT_CLASSIFICATION_NONE = "None";
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_SPOUSE = "Spouse";
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_FATHER = "Father";
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_MOTHER = "Mother";
    public static final String CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_CHILD = "Child";
    public static final String CODE_VALUE_FAMILY_MEMBER_PROFESSION_EMPLOYEE = "Employee";
    public static final String CODE_VALUE_FAMILY_MEMBER_PROFESSION_SELF_EMPLOYED = "Self-Employed";
    public static final String CODE_VALUE_FAMILY_MARITAL_STATUS_MARRIED = "Married";
    public static final String CODE_VALUE_FAMILY_MARITAL_STATUS_SINGLE = "Single";
    public static final String CODE_VALUE_FAMILY_MARITAL_STATUS_WIDOWED = "Widowed";
    public static final String CODE_VALUE_CONSTITUTION_TEST = "Test";
    public static final String CODE_VALUE_RESCHEDULE_REASON_TEST = "Test";
    public static final String CODE_VALUE_WRITE_OFF_REASON_TEST_1 = "Bad Debt";
    public static final String CODE_VALUE_WRITE_OFF_REASON_TEST_2 = "Forgiven";
    public static final String CODE_VALUE_WRITE_OFF_REASON_TEST_3 = "Test";
    public static final String BUYDOWN_FEE_TRANSACTION_CLASSIFICATION_VALUE = "buydown_fee_transaction_classification_value";
    public static final String CAPITALIZED_INCOME_TRANSACTION_CLASSIFICATION_VALUE = "capitalized_income_transaction_classification_value";

    private final FineractFeignClient fineractClient;

    @Override
    public void initialize() {
        createCodeNames();
        createCodeValues();
    }

    private void createCodeValues() {
        // address type
        List<String> addressNames = new ArrayList<>();
        addressNames.add(CODE_VALUE_ADDRESS_TYPE_RESIDENTIAL);
        addressNames.add(CODE_VALUE_ADDRESS_TYPE_OFFICE);
        createCodeValues(CodeNames.ADDRESS_TYPE.getValue(), addressNames);

        // Country
        List<String> countryNames = new ArrayList<>();
        countryNames.add(CODE_VALUE_COUNTRY_GERMANY);
        createCodeValues(CodeNames.COUNTRY.getValue(), countryNames);

        // State
        List<String> stateNames = new ArrayList<>();
        stateNames.add(CODE_VALUE_STATE_BERLIN);
        createCodeValues(CodeNames.STATE.getValue(), stateNames);

        // financial instrument
        List<String> financialInstrumentNames = new ArrayList<>();
        financialInstrumentNames.add(CODE_VALUE_FINANCIAL_INSTRUMENT_DEBIT);
        financialInstrumentNames.add(CODE_VALUE_FINANCIAL_INSTRUMENT_CREDIT);
        createCodeValues(CodeNames.FINANCIAL_INSTRUMENT.getValue(), financialInstrumentNames);

        List<String> chargeOffReasonNames = new ArrayList<>();
        chargeOffReasonNames.add(CODE_VALUE_CHARGE_OFF_REASON_FRAUD);
        chargeOffReasonNames.add(CODE_VALUE_CHARGE_OFF_REASON_DELINQUENT);
        chargeOffReasonNames.add(CODE_VALUE_CHARGE_OFF_REASON_OTHER);
        createCodeValues(CodeNames.CHARGE_OFF.getValue(), chargeOffReasonNames);

        // transaction type
        List<String> transactionTypeNames = new ArrayList<>();
        transactionTypeNames.add(CODE_VALUE_TRANSACTION_TYPE_SCHEDULED_PAYMENT);
        createCodeValues(CodeNames.TRANSACTION_TYPE.getValue(), transactionTypeNames);

        // bankruptcy tag
        List<String> bankruptcyTagNames = new ArrayList<>();
        bankruptcyTagNames.add(CODE_VALUE_BANKRUPTCY_TAG_PENDING);
        bankruptcyTagNames.add(CODE_VALUE_BANKRUPTCY_TAG_BANKRUPTCY);
        createCodeValues(CodeNames.BANKRUPTCY_TAG.getValue(), bankruptcyTagNames);

        // pending fraud tag
        List<String> pendingFraudTagNames = new ArrayList<>();
        pendingFraudTagNames.add(CODE_VALUE_PENDING_FRAUD_TAG_PENDING);
        pendingFraudTagNames.add(CODE_VALUE_PENDING_FRAUD_TAG_FRAUD);
        createCodeValues(CodeNames.PENDING_FRAUD_TAG.getValue(), pendingFraudTagNames);

        // pending deceased tag
        List<String> pendingDeceasedTagNames = new ArrayList<>();
        pendingDeceasedTagNames.add(CODE_VALUE_PENDING_DECEASED_TAG_PENDING);
        pendingDeceasedTagNames.add(CODE_VALUE_PENDING_DECEASED_TAG_DECEASED);
        createCodeValues(CodeNames.PENDING_DECEASED_TAG.getValue(), pendingDeceasedTagNames);

        // hardship tag
        List<String> hardshipTagNames = new ArrayList<>();
        hardshipTagNames.add(CODE_VALUE_HARDSHIP_TAG_ACTIVE);
        hardshipTagNames.add(CODE_VALUE_HARDSHIP_TAG_INACTIVE);
        createCodeValues(CodeNames.HARDSHIP_TAG.getValue(), hardshipTagNames);

        // active duty tag
        List<String> activeDutyTagNames = new ArrayList<>();
        activeDutyTagNames.add(CODE_VALUE_ACTIVE_DUTY_TAG_ACTIVE);
        activeDutyTagNames.add(CODE_VALUE_ACTIVE_DUTY_TAG_INACTIVE);
        createCodeValues(CodeNames.ACTIVE_DUTY_TAG.getValue(), activeDutyTagNames);

        // customer identifiers put/post
        List<String> customerIdentifierNamesPut = new ArrayList<>();
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_1);
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_2);
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_3);
        customerIdentifierNamesPut.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_4);
        updateCodeValues(CodeNames.CUSTOMER_IDENTIFIER.getValue(), customerIdentifierNamesPut);

        List<String> customerIdentifierNamesPost = new ArrayList<>();
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_5);
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_6);
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_7);
        customerIdentifierNamesPost.add(CODE_VALUE_CUSTOMER_IDENTIFIERS_8);
        createCodeValues(CodeNames.CUSTOMER_IDENTIFIER.getValue(), customerIdentifierNamesPost);

        // gender
        List<String> genderNames = new ArrayList<>();
        genderNames.add(CODE_VALUE_GENDER_FEMALE);
        genderNames.add(CODE_VALUE_GENDER_MALE);
        createCodeValues(CodeNames.GENDER.getValue(), genderNames);

        // client type
        List<String> clientTypeNames = new ArrayList<>();
        clientTypeNames.add(CODE_VALUE_CLIENT_TYPE_CORPORATE);
        clientTypeNames.add(CODE_VALUE_CLIENT_TYPE_LEGAL);
        clientTypeNames.add(CODE_VALUE_CLIENT_TYPE_NON_LEGAL);
        createCodeValues(CodeNames.CLIENT_TYPE.getValue(), clientTypeNames);

        // client classification
        List<String> clientClassificationNames = new ArrayList<>();
        clientClassificationNames.add(CODE_VALUE_CLIENT_CLASSIFICATION_LAWYER);
        clientClassificationNames.add(CODE_VALUE_CLIENT_CLASSIFICATION_DIRECTOR);
        clientClassificationNames.add(CODE_VALUE_CLIENT_CLASSIFICATION_NONE);
        createCodeValues(CodeNames.CLIENT_CLASSIFICATION.getValue(), clientClassificationNames);

        // add family member - relationship
        List<String> familyMemberRelationshipNames = new ArrayList<>();
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_SPOUSE);
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_FATHER);
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_MOTHER);
        familyMemberRelationshipNames.add(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_CHILD);
        createCodeValues(CodeNames.FAMILY_MEMBER_RELATIONSHIP.getValue(), familyMemberRelationshipNames);

        // add family member - profession
        List<String> familyMemberProfessionNames = new ArrayList<>();
        familyMemberProfessionNames.add(CODE_VALUE_FAMILY_MEMBER_PROFESSION_EMPLOYEE);
        familyMemberProfessionNames.add(CODE_VALUE_FAMILY_MEMBER_PROFESSION_SELF_EMPLOYED);
        createCodeValues(CodeNames.FAMILY_MEMBER_PROFESSION.getValue(), familyMemberProfessionNames);

        // add family member - marital status
        List<String> familyMemberMaritalStatusNames = new ArrayList<>();
        familyMemberMaritalStatusNames.add(CODE_VALUE_FAMILY_MARITAL_STATUS_MARRIED);
        familyMemberMaritalStatusNames.add(CODE_VALUE_FAMILY_MARITAL_STATUS_SINGLE);
        familyMemberMaritalStatusNames.add(CODE_VALUE_FAMILY_MARITAL_STATUS_WIDOWED);
        createCodeValues(CodeNames.FAMILY_MARITAL_STATUS.getValue(), familyMemberMaritalStatusNames);

        // add constitution (for client creation as Entity)
        List<String> constitutionNames = new ArrayList<>();
        constitutionNames.add(CODE_VALUE_CONSTITUTION_TEST);
        createCodeValues(CodeNames.CONSTITUTION.getValue(), constitutionNames);

        // add LoanRescheduleReason
        List<String> rescheduleReasonNames = new ArrayList<>();
        rescheduleReasonNames.add(CODE_VALUE_RESCHEDULE_REASON_TEST);
        createCodeValues(CodeNames.LOAN_RESCHEDULE_REASON.getValue(), rescheduleReasonNames);

        // add Write-off reasons
        List<String> writeOffReasonNames = new ArrayList<>();
        writeOffReasonNames.add(CODE_VALUE_WRITE_OFF_REASON_TEST_1);
        writeOffReasonNames.add(CODE_VALUE_WRITE_OFF_REASON_TEST_2);
        writeOffReasonNames.add(CODE_VALUE_WRITE_OFF_REASON_TEST_3);
        createCodeValues(CodeNames.WRITE_OFF_REASON.getValue(), writeOffReasonNames);

        // Add buy down fee transaction classification
        List<String> buydownFeeTransactionClassificationName = new ArrayList<>();
        buydownFeeTransactionClassificationName.add(BUYDOWN_FEE_TRANSACTION_CLASSIFICATION_VALUE);
        createCodeValues(CodeNames.BUYDOWN_FEE_TRANSACTION_CLASSIFICATION.getValue(), buydownFeeTransactionClassificationName);

        // Add capitalized income transaction classification
        List<String> capitalizedIncomeTransactionClassificationName = new ArrayList<>();
        capitalizedIncomeTransactionClassificationName.add(CAPITALIZED_INCOME_TRANSACTION_CLASSIFICATION_VALUE);
        createCodeValues(CodeNames.CAPITALIZED_INCOME_TRANSACTION_CLASSIFICATION.getValue(),
                capitalizedIncomeTransactionClassificationName);
    }

    public void createCodeValues(String codeName, List<String> codeValueNames) {
        codeValueNames.forEach(name -> {
            Integer position = codeValueNames.indexOf(name);
            PostCodeValuesDataRequest postCodeValuesDataRequest = new PostCodeValuesDataRequest();
            postCodeValuesDataRequest.isActive(true);
            postCodeValuesDataRequest.name(name);
            postCodeValuesDataRequest.position(position);

            try {
                executeVoid(() -> fineractClient.codeValues().createCodeValue1(codeName, postCodeValuesDataRequest, Map.of()));
                log.debug("Code value '{}' created successfully", name);
            } catch (CallFailedRuntimeException e) {
                if (e.getStatus() == 403 && e.getDeveloperMessage() != null && e.getDeveloperMessage().contains("already exists")) {
                    log.debug("Code value '{}' already exists, skipping creation", name);
                    return;
                }
                throw e;
            }
        });
    }

    public void updateCodeValues(String codeName, List<String> codeValueNames) {
        codeValueNames.forEach(name -> {
            int position = codeValueNames.indexOf(name) + 1;
            PutCodeValuesDataRequest putCodeValuesDataRequest = new PutCodeValuesDataRequest();
            putCodeValuesDataRequest.isActive(false);
            putCodeValuesDataRequest.name(name);
            putCodeValuesDataRequest.position(position);

            executeVoid(() -> fineractClient.codeValues().updateCodeValue1(codeName, (long) position, putCodeValuesDataRequest, Map.of()));
        });
    }

    private void createCodeNames() {
        List<String> codesNameList = new ArrayList<>();
        codesNameList.add(CodeNames.FINANCIAL_INSTRUMENT.getValue());
        codesNameList.add(CodeNames.TRANSACTION_TYPE.getValue());
        codesNameList.add(CodeNames.BANKRUPTCY_TAG.getValue());
        codesNameList.add(CodeNames.PENDING_FRAUD_TAG.getValue());
        codesNameList.add(CodeNames.PENDING_DECEASED_TAG.getValue());
        codesNameList.add(CodeNames.HARDSHIP_TAG.getValue());
        codesNameList.add(CodeNames.ACTIVE_DUTY_TAG.getValue());

        codesNameList.forEach(codeName -> {
            try {
                fineractClient.codes().retrieveCodeByName(codeName);
                // Code already exists, skip creation
            } catch (Exception e) {
                // Code doesn't exist, create it
                PostCodesRequest postCodesRequest = new PostCodesRequest();
                executeVoid(() -> fineractClient.codes().createCode(postCodesRequest.name(codeName), Map.of()));
            }
        });
    }
}
