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

public enum CobBusinessStep {

    APPLY_CHARGE_TO_OVERDUE_LOANS("APPLY_CHARGE_TO_OVERDUE_LOANS"), //
    LOAN_DELINQUENCY_CLASSIFICATION("LOAN_DELINQUENCY_CLASSIFICATION"), //
    CHECK_LOAN_REPAYMENT_DUE("CHECK_LOAN_REPAYMENT_DUE"), //
    CHECK_LOAN_REPAYMENT_OVERDUE("CHECK_LOAN_REPAYMENT_OVERDUE"), //
    UPDATE_LOAN_ARREARS_AGING("UPDATE_LOAN_ARREARS_AGING"), //
    ADD_PERIODIC_ACCRUAL_ENTRIES("ADD_PERIODIC_ACCRUAL_ENTRIES"), //
    EXTERNAL_ASSET_OWNER_TRANSFER("EXTERNAL_ASSET_OWNER_TRANSFER"), //
    CHECK_DUE_INSTALLMENTS("CHECK_DUE_INSTALLMENTS"), //
    ACCRUAL_ACTIVITY_POSTING("ACCRUAL_ACTIVITY_POSTING"), //
    LOAN_INTEREST_RECALCULATION("LOAN_INTEREST_RECALCULATION");//

    public final String value;

    CobBusinessStep(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
