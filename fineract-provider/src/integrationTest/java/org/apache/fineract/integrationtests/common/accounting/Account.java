/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.accounting;

public class Account {

    public enum AccountType {
        ASSET("1"), INCOME("4"), EXPENSE("5"), LIABILITY("2"), EQUITY("3");

        private final String accountValue;

        AccountType(final String accountValue) {
            this.accountValue = accountValue;
        }

        @Override
        public String toString() {
            return this.accountValue;
        }
    }

    private final AccountType accountType;
    private final Integer accountID;

    public Account(final Integer accountID, final AccountType accountType) {
        this.accountID = accountID;
        this.accountType = accountType;
    }

    public AccountType getAccountType() {
        return this.accountType;
    }

    public Integer getAccountID() {
        return this.accountID;
    }
}
