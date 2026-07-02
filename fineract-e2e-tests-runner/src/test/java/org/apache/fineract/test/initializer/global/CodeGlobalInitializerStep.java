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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostCodesRequest;
import org.apache.fineract.client.models.PutCodeValuesDataRequest;
import org.apache.fineract.test.data.codevalue.CodeNames;
import org.apache.fineract.test.helper.ParallelExecutionHelper;
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
    public static final String WORKING_CAPITAL_DISCOUNT_FEE_CLASSIFICATION_VALUE = "working_capital_loan_discount_fee_classification_value";

    private final FineractFeignClient fineractClient;
    private Map<String, List<String>> existingCodeAndCodeValues = new HashMap<>();

    @Override
    public void initialize() {
        fetchExistingCodesAndCodeValues();
        createCodeNames();
        createCodeValues();
    }

    private void fetchExistingCodesAndCodeValues() {
        List<GetCodesResponse> existingCodes = fineractClient.codes().retrieveAllCodes();
        existingCodes.forEach(code -> {
            List<GetCodeValuesDataResponse> existingCodeValues = fineractClient.codeValues()
                    .retrieveAllCodeValuesByCodeName(code.getName());
            existingCodeAndCodeValues.put(code.getName(), existingCodeValues.stream().map(GetCodeValuesDataResponse::getName).toList());
        });
    }

    private void createCodeValues() {
        List<Runnable> items = List.of(
                () -> createCodeValues(CodeNames.ADDRESS_TYPE.getValue(),
                        List.of(CODE_VALUE_ADDRESS_TYPE_RESIDENTIAL, CODE_VALUE_ADDRESS_TYPE_OFFICE)),
                () -> createCodeValues(CodeNames.COUNTRY.getValue(), List.of(CODE_VALUE_COUNTRY_GERMANY)),
                () -> createCodeValues(CodeNames.STATE.getValue(), List.of(CODE_VALUE_STATE_BERLIN)),
                () -> createCodeValues(CodeNames.FINANCIAL_INSTRUMENT.getValue(),
                        List.of(CODE_VALUE_FINANCIAL_INSTRUMENT_DEBIT, CODE_VALUE_FINANCIAL_INSTRUMENT_CREDIT)),
                () -> createCodeValues(CodeNames.CHARGE_OFF.getValue(),
                        List.of(CODE_VALUE_CHARGE_OFF_REASON_FRAUD, CODE_VALUE_CHARGE_OFF_REASON_DELINQUENT,
                                CODE_VALUE_CHARGE_OFF_REASON_OTHER)),
                () -> createCodeValues(CodeNames.TRANSACTION_TYPE.getValue(), List.of(CODE_VALUE_TRANSACTION_TYPE_SCHEDULED_PAYMENT)),
                () -> createCodeValues(CodeNames.BANKRUPTCY_TAG.getValue(),
                        List.of(CODE_VALUE_BANKRUPTCY_TAG_PENDING, CODE_VALUE_BANKRUPTCY_TAG_BANKRUPTCY)),
                () -> createCodeValues(CodeNames.PENDING_FRAUD_TAG.getValue(),
                        List.of(CODE_VALUE_PENDING_FRAUD_TAG_PENDING, CODE_VALUE_PENDING_FRAUD_TAG_FRAUD)),
                () -> createCodeValues(CodeNames.PENDING_DECEASED_TAG.getValue(),
                        List.of(CODE_VALUE_PENDING_DECEASED_TAG_PENDING, CODE_VALUE_PENDING_DECEASED_TAG_DECEASED)),
                () -> createCodeValues(CodeNames.HARDSHIP_TAG.getValue(),
                        List.of(CODE_VALUE_HARDSHIP_TAG_ACTIVE, CODE_VALUE_HARDSHIP_TAG_INACTIVE)),
                () -> createCodeValues(CodeNames.ACTIVE_DUTY_TAG.getValue(),
                        List.of(CODE_VALUE_ACTIVE_DUTY_TAG_ACTIVE, CODE_VALUE_ACTIVE_DUTY_TAG_INACTIVE)),
                () -> {
                    // customer identifiers: update pre-existing values, then create new ones (sequential on same code)
                    updateCodeValues(CodeNames.CUSTOMER_IDENTIFIER.getValue(), List.of(CODE_VALUE_CUSTOMER_IDENTIFIERS_1,
                            CODE_VALUE_CUSTOMER_IDENTIFIERS_2, CODE_VALUE_CUSTOMER_IDENTIFIERS_3, CODE_VALUE_CUSTOMER_IDENTIFIERS_4));
                    createCodeValues(CodeNames.CUSTOMER_IDENTIFIER.getValue(), List.of(CODE_VALUE_CUSTOMER_IDENTIFIERS_5,
                            CODE_VALUE_CUSTOMER_IDENTIFIERS_6, CODE_VALUE_CUSTOMER_IDENTIFIERS_7, CODE_VALUE_CUSTOMER_IDENTIFIERS_8));
                }, () -> createCodeValues(CodeNames.GENDER.getValue(), List.of(CODE_VALUE_GENDER_FEMALE, CODE_VALUE_GENDER_MALE)),
                () -> createCodeValues(CodeNames.CLIENT_TYPE.getValue(),
                        List.of(CODE_VALUE_CLIENT_TYPE_CORPORATE, CODE_VALUE_CLIENT_TYPE_LEGAL, CODE_VALUE_CLIENT_TYPE_NON_LEGAL)),
                () -> createCodeValues(CodeNames.CLIENT_CLASSIFICATION.getValue(),
                        List.of(CODE_VALUE_CLIENT_CLASSIFICATION_LAWYER, CODE_VALUE_CLIENT_CLASSIFICATION_DIRECTOR,
                                CODE_VALUE_CLIENT_CLASSIFICATION_NONE)),
                () -> createCodeValues(CodeNames.FAMILY_MEMBER_RELATIONSHIP.getValue(),
                        List.of(CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_SPOUSE, CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_FATHER,
                                CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_MOTHER, CODE_VALUE_FAMILY_MEMBER_RELATIONSHIP_CHILD)),
                () -> createCodeValues(CodeNames.FAMILY_MEMBER_PROFESSION.getValue(),
                        List.of(CODE_VALUE_FAMILY_MEMBER_PROFESSION_EMPLOYEE, CODE_VALUE_FAMILY_MEMBER_PROFESSION_SELF_EMPLOYED)),
                () -> createCodeValues(CodeNames.FAMILY_MARITAL_STATUS.getValue(),
                        List.of(CODE_VALUE_FAMILY_MARITAL_STATUS_MARRIED, CODE_VALUE_FAMILY_MARITAL_STATUS_SINGLE,
                                CODE_VALUE_FAMILY_MARITAL_STATUS_WIDOWED)),
                () -> createCodeValues(CodeNames.CONSTITUTION.getValue(), List.of(CODE_VALUE_CONSTITUTION_TEST)),
                () -> createCodeValues(CodeNames.LOAN_RESCHEDULE_REASON.getValue(), List.of(CODE_VALUE_RESCHEDULE_REASON_TEST)),
                () -> createCodeValues(CodeNames.WRITE_OFF_REASON.getValue(),
                        List.of(CODE_VALUE_WRITE_OFF_REASON_TEST_1, CODE_VALUE_WRITE_OFF_REASON_TEST_2,
                                CODE_VALUE_WRITE_OFF_REASON_TEST_3)),
                () -> createCodeValues(CodeNames.BUYDOWN_FEE_TRANSACTION_CLASSIFICATION.getValue(),
                        List.of(BUYDOWN_FEE_TRANSACTION_CLASSIFICATION_VALUE)),
                () -> createCodeValues(CodeNames.CAPITALIZED_INCOME_TRANSACTION_CLASSIFICATION.getValue(),
                        List.of(CAPITALIZED_INCOME_TRANSACTION_CLASSIFICATION_VALUE)),
                () -> createCodeValues(CodeNames.WORKING_CAPITAL_DISCOUNT_FEE_CLASSIFICATION.getValue(),
                        List.of(WORKING_CAPITAL_DISCOUNT_FEE_CLASSIFICATION_VALUE)));
        ParallelExecutionHelper.runInParallel(items);
    }

    public void createCodeValues(String codeName, List<String> codeValueNames) {

        codeValueNames.forEach(codeValueName -> {
            if (existingCodeAndCodeValues.get(codeName) != null && existingCodeAndCodeValues.get(codeName).contains(codeValueName)) {
                log.debug("Code value '{}' already exists, skipping creation", codeValueName);
                return;
            }
            Integer position = codeValueNames.indexOf(codeValueName);
            PostCodeValuesDataRequest postCodeValuesDataRequest = new PostCodeValuesDataRequest();
            postCodeValuesDataRequest.isActive(true);
            postCodeValuesDataRequest.name(codeValueName);
            postCodeValuesDataRequest.position(position);

            try {
                executeVoid(() -> fineractClient.codeValues().createCodeValueByCodeName(codeName, postCodeValuesDataRequest, Map.of()));
                log.debug("Code value '{}' created successfully", codeValueName);
            } catch (CallFailedRuntimeException e) {
                if (e.getStatus() == 403 && e.getDeveloperMessage() != null && e.getDeveloperMessage().contains("already exists")) {
                    log.debug("Code value '{}' already exists, skipping creation", codeValueName);
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

            executeVoid(() -> fineractClient.codeValues().updateCodeValueByCodeName(codeName, (long) position, putCodeValuesDataRequest,
                    Map.of()));
        });
    }

    private void createCodeNames() {
        List.of(CodeNames.FINANCIAL_INSTRUMENT.getValue(), CodeNames.TRANSACTION_TYPE.getValue(), CodeNames.BANKRUPTCY_TAG.getValue(),
                CodeNames.PENDING_FRAUD_TAG.getValue(), CodeNames.PENDING_DECEASED_TAG.getValue(), CodeNames.HARDSHIP_TAG.getValue(),
                CodeNames.ACTIVE_DUTY_TAG.getValue(), CodeNames.WORKING_CAPITAL_DISCOUNT_FEE_CLASSIFICATION.getValue()).parallelStream()
                .forEach(codeName -> {
                    if (existingCodeAndCodeValues.get(codeName) == null) {
                        executeVoid(() -> fineractClient.codes().createCode(new PostCodesRequest().name(codeName), Map.of()));
                    } else {
                        log.debug("Code '{}' already exists, skipping creation", codeName);
                    }
                });
    }
}
