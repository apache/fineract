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
package org.apache.fineract.test.data;

import lombok.Getter;

@Getter
public enum ChargeProductType {

    LOAN_PERCENTAGE_LATE_FEE("% Late fee"), //
    LOAN_PERCENTAGE_PROCESSING_FEE("% Processing fee"), //
    LOAN_FIXED_LATE_FEE("Fixed Late fee"), //
    LOAN_FIXED_RETURNED_PAYMENT_FEE("Fixed Returned payment fee"), //
    LOAN_SNOOZE_FEE("Snooze fee"), //
    LOAN_NSF_FEE("NSF fee"), //
    LOAN_DISBURSEMENT_PERCENTAGE_FEE("Disbursement percentage fee"), //
    LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE("Tranche Disbursement percentage fee"), //
    LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST("Installment percentage amount + interest fee"), //
    LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST("% Late fee amount+interest"), //
    CLIENT_TEST_CHARGE_FEE("Fixed fee for Client"), //
    LOAN_DISBURSEMENT_CHARGE("Disbursement Charge"), //
    CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT("Tranche Disbursement Charge Amount"), //
    CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT("Tranche Disbursement Charge Percent"), //
    LOAN_INSTALLMENT_FEE_FLAT("Installment flat fee"), //
    LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT("Installment percentage amount fee"), //
    LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST("Installment percentage interest fee"), //
    LOAN_DISBURSEMENT_PERCENTAGE_AMOUNT_PLUS_INTEREST_FEE("Disbursement percentage amount + interest fee"), //
    WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE("Working Capital Loan Fee"), //
    WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY("Working Capital Loan Penalty"), //
    WORKING_CAPITAL_DISBURSEMENT_FEE("Working Capital Disbursement Fee"), //
    WORKING_CAPITAL_DISBURSEMENT_PENALTY("Working Capital Disbursement Penalty"), //
    WORKING_CAPITAL_DISBURSEMENT_FEE_PERCENTAGE("Working Capital Disbursement Fee Percentage"); //

    public final String name;

    ChargeProductType(String name) {
        this.name = name;
    }

}
