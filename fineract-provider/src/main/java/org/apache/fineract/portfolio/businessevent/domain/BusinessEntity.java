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
package org.apache.fineract.portfolio.businessevent.domain;

public enum BusinessEntity {

    LOAN("loan"), //
    LOAN_TRANSACTION("loan_transaction"), //
    LOAN_CHARGE("loan_charge"), //
    LOAN_ADJUSTED_TRANSACTION("loan_adjusted_transaction"), SAVING("saving"), //
    CLIENT("client"), //
    SAVINGS_TRANSACTION("Savings Transaction"), //
    GROUP("group"), //
    SHARE_ACCOUNT("share_account"), //
    SHARE_PRODUCT("share_product"), //
    DEPOSIT_ACCOUNT("deposit_account"), //
    LOAN_PRODUCT("loan_product");

    private final String value;

    BusinessEntity(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
