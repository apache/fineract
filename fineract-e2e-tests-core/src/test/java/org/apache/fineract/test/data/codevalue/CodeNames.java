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
package org.apache.fineract.test.data.codevalue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodeNames {

    FINANCIAL_INSTRUMENT("financial_instrument"), //
    TRANSACTION_TYPE("transaction_type"), //
    BANKRUPTCY_TAG("bankruptcy_tag"), //
    PENDING_FRAUD_TAG("pending_fraud_tag"), //
    PENDING_DECEASED_TAG("pending_deceased_tag"), //
    HARDSHIP_TAG("hardship_tag"), //
    ACTIVE_DUTY_TAG("active_duty_tag"), //
    ADDRESS_TYPE("ADDRESS_TYPE"), //
    COUNTRY("COUNTRY"), //
    STATE("STATE"), //
    CHARGE_OFF("ChargeOffReasons"), //
    CUSTOMER_IDENTIFIER("Customer Identifier"), //
    GENDER("Gender"), //
    CLIENT_TYPE("ClientType"), //
    CLIENT_CLASSIFICATION("ClientClassification"), //
    FAMILY_MEMBER_RELATIONSHIP("RELATIONSHIP"), //
    FAMILY_MEMBER_PROFESSION("PROFESSION"), //
    FAMILY_MARITAL_STATUS("MARITAL STATUS"), //
    CONSTITUTION("Constitution"), //
    LOAN_RESCHEDULE_REASON("LoanRescheduleReason"), //
    WRITE_OFF_REASON("WriteOffReasons"), //
    BUYDOWN_FEE_TRANSACTION_CLASSIFICATION("buydown_fee_transaction_classification"), //
    CAPITALIZED_INCOME_TRANSACTION_CLASSIFICATION("capitalized_income_transaction_classification"); //

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
