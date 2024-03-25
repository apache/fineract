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

public enum ChargeProductType {

    LOAN_PERCENTAGE_LATE_FEE(1L), LOAN_PERCENTAGE_PROCESSING_FEE(2L), LOAN_FIXED_LATE_FEE(3L), LOAN_FIXED_RETURNED_PAYMENT_FEE(
            4L), LOAN_SNOOZE_FEE(5L), LOAN_NSF_FEE(6L), LOAN_DISBURSEMENT_PERCENTAGE_FEE(
                    7L), LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE(8L), LOAN_INSTALLMENT_PERCENTAGE_FEE(
                            9L), LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST(10L), CLIENT_TEST_CHARGE_FEE(11L);

    public final Long value;

    ChargeProductType(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
