/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

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