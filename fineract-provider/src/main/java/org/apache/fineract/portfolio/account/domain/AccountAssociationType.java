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
package org.apache.fineract.portfolio.account.domain;

/**
 * Enum representation of loan status states.
 */
public enum AccountAssociationType {

    INVALID(0, "accountAssociationType.invalid"), //
    LINKED_ACCOUNT_ASSOCIATION(1, "accountAssociationType.loan.account.association"), //
    GUARANTOR_ACCOUNT_ASSOCIATION(2, "accountAssociationType.guarantor.account.association"); //

    private final Integer value;
    private final String code;

    public static AccountAssociationType fromInt(final Integer statusValue) {

        AccountAssociationType enumeration = AccountAssociationType.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION;
            break;
            case 2:
                enumeration = AccountAssociationType.GUARANTOR_ACCOUNT_ASSOCIATION;
            break;

        }
        return enumeration;
    }

    private AccountAssociationType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final AccountAssociationType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isLinkedAccountAssociation() {
        return this.value.equals(AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
    }

    public boolean isGuarantorAccountAssociation() {
        return this.value.equals(AccountAssociationType.GUARANTOR_ACCOUNT_ASSOCIATION.getValue());
    }

}