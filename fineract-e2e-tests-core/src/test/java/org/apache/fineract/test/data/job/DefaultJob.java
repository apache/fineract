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
package org.apache.fineract.test.data.job;

public enum DefaultJob implements Job {

    ADD_ACCRUAL_TRANSACTIONS("Add Accrual Transactions", "LA_AATR"), //
    ADD_PERIODIC_ACCRUAL_TRANSACTIONS("Add Periodic Accrual Transactions", "ACC_APTR"), //
    INCREASE_BUSINESS_DAY("Increase Business Date by 1 day", "BDT_INC1"), //
    LOAN_DELINQUENCY_CLASSIFICATION("Loan Delinquency Classification", "LA_DECL"), //
    LOAN_COB("Loan COB", "LA_ECOB"), //
    ;

    private final String customName;
    private final String shortName;

    DefaultJob(String customName, String shortName) {
        this.customName = customName;
        this.shortName = shortName;
    }

    @Override
    public String getName() {
        return customName;
    }

    @Override
    public String getShortName() {
        return shortName;
    }
}
